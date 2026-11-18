package gut.dev.veloxplay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import gut.dev.veloxplay.Config.AccountBootstrap;
import gut.dev.veloxplay.Repository.RUser;
import gut.dev.veloxplay.Repository.RDemoWorkspace;
import gut.dev.veloxplay.Repository.RDemoEntry;
import gut.dev.veloxplay.Config.DemoCleanup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import javax.sql.DataSource;
import gut.dev.veloxplay.Enum.Role;
import gut.dev.veloxplay.Model.User;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthAndCatalogTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired RUser users;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired RDemoWorkspace workspaces;
    @Autowired RDemoEntry demoEntries;
    @Autowired DataSource dataSource;
    @Autowired jakarta.persistence.EntityManager entityManager;

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"demo@veloxplay.com\",\"senha\":\"errada123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void catalogIsPublicAndAdminRoutesAreProtected() throws Exception {
        mockMvc.perform(get("/filmes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").exists())
                .andExpect(jsonPath("$[0].senha").doesNotExist());

        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/filmes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"X\",\"descricao\":\"Y\",\"genero\":\"ACAO\",\"urlVideo\":\"http://x\",\"urlCapa\":\"http://y\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanLoginRefreshAndManageOwnFavorites() throws Exception {
        JsonNode tokens = login("demo@veloxplay.com", "Demo@123");
        String access = tokens.get("accessToken").asText();
        String refresh = tokens.get("refreshToken").asText();
        long userId = tokens.get("user").get("id").asLong();

        assertThat(tokens.get("user").get("senha")).isNull();

        MvcResult catalog = mockMvc.perform(get("/filmes"))
                .andExpect(status().isOk())
                .andReturn();
        long filmeId = mapper.readTree(catalog.getResponse().getContentAsString()).get(0).get("id").asLong();

        mockMvc.perform(post("/users/" + userId + "/favoritos/" + filmeId)
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(filmeId));

        mockMvc.perform(get("/users/" + userId + "/favoritos")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(filmeId));

        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/admin/stats")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isForbidden());

        MvcResult refreshed = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();
        String newRefresh = mapper.readTree(refreshed.getResponse().getContentAsString()).get("refreshToken").asText();
        assertThat(newRefresh).isNotEqualTo(refresh);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/users/" + userId + "/favoritos/" + filmeId)
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanReadUsersWithoutPasswords() throws Exception {
        JsonNode tokens = login("owner@veloxplay.test", "OwnerTest@123");
        String access = tokens.get("accessToken").asText();

        mockMvc.perform(get("/users").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].senha").doesNotExist());

        mockMvc.perform(get("/admin/stats").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filmes").isNumber());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        register("Duplicado", "duplicado@example.test");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Outro\",\"email\":\"duplicado@example.test\",\"senha\":\"Senha1234\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void publicModeratorHasNoAccessToRealAdministration() throws Exception {
        JsonNode moderator = login("moderador@veloxplay.com", "Moderador@123");
        assertThat(moderator.path("user").path("role").asText()).isEqualTo("MODERATOR");
        String access = moderator.path("accessToken").asText();
        long userId = users.findByEmailIgnoreCase("demo@veloxplay.com").orElseThrow().getId();
        for (String path : new String[]{"/users", "/admin/stats"}) {
            mockMvc.perform(get(path).header("Authorization", bearer(access))).andExpect(status().isForbidden());
        }
        mockMvc.perform(delete("/users/" + userId).header("Authorization", bearer(access)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/users").header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Outro\",\"email\":\"other@example.test\",\"senha\":\"Senha1234\"}"))
                .andExpect(status().isForbidden());
        assertMovieWritesForbidden(access);
        mockMvc.perform(get("/demo/stats").header("Authorization", bearer(access))).andExpect(status().isOk());
    }

    @Test
    void ordinaryUserCannotDeleteUsersOrEscalatePrivileges() throws Exception {
        JsonNode registered = register("Usuario real", "real@example.test");
        String access = registered.path("accessToken").asText();
        long id = registered.path("user").path("id").asLong();
        long other = users.findByEmailIgnoreCase("demo@veloxplay.com").orElseThrow().getId();
        mockMvc.perform(delete("/users/" + other).header("Authorization", bearer(access)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/users/" + id).header("Authorization", bearer(access))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Usuario real\",\"email\":\"real@example.test\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/demo/filmes").header("Authorization", bearer(access))).andExpect(status().isForbidden());
        assertMovieWritesForbidden(access);
        mockMvc.perform(get("/users/" + other + "/favoritos").header("Authorization", bearer(access)))
                .andExpect(status().isForbidden());
        assertThat(users.findByEmailIgnoreCase("real@example.test").orElseThrow().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void demoAccountsCannotBeChangedOrDeletedEvenByAdministrator() throws Exception {
        String admin = login("owner@veloxplay.test", "OwnerTest@123").path("accessToken").asText();
        for (String email : new String[]{"demo@veloxplay.com", "moderador@veloxplay.com"}) {
            long id = users.findByEmailIgnoreCase(email).orElseThrow().getId();
            mockMvc.perform(delete("/users/" + id).header("Authorization", bearer(admin)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(put("/users/" + id).header("Authorization", bearer(admin))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nome\":\"Invadido\",\"email\":\"attacker@example.test\",\"senha\":\"Other1234\",\"role\":\"ADMIN\"}"))
                    .andExpect(status().isForbidden());
        }
        JsonNode demo = login("demo@veloxplay.com", "Demo@123");
        mockMvc.perform(put("/users/" + demo.path("user").path("id").asLong())
                        .header("Authorization", bearer(demo.path("accessToken").asText()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Mudou\",\"email\":\"changed@example.test\",\"senha\":\"Other1234\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void reservedEmailsCannotBeRegistered() throws Exception {
        for (String email : new String[]{"admin@veloxplay.com", "moderador@veloxplay.com", "demo@veloxplay.com", "owner@veloxplay.test"}) {
            mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nome\":\"Tentativa\",\"email\":\"" + email + "\",\"senha\":\"Senha1234\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void sandboxCrudIsIsolatedAcrossSessionsAndNeverPublishesChanges() throws Exception {
        String a = login("moderador@veloxplay.com", "Moderador@123").path("accessToken").asText();
        String b = login("moderador@veloxplay.com", "Moderador@123").path("accessToken").asText();
        String catalogBefore = mockMvc.perform(get("/filmes")).andReturn().getResponse().getContentAsString();
        long realUsers = users.count();
        JsonNode created = body(mockMvc.perform(post("/demo/filmes").header("Authorization", bearer(a))
                .contentType(MediaType.APPLICATION_JSON).content(movie("Somente na sessao A")))
                .andExpect(status().isCreated()).andReturn());
        long id = created.path("id").asLong();
        mockMvc.perform(get("/demo/filmes/" + id).header("Authorization", bearer(b))).andExpect(status().isNotFound());
        mockMvc.perform(put("/demo/filmes/" + id).header("Authorization", bearer(b))
                .contentType(MediaType.APPLICATION_JSON).content(movie("Invasao"))).andExpect(status().isNotFound());
        mockMvc.perform(delete("/demo/filmes/" + id).header("Authorization", bearer(b))).andExpect(status().isNotFound());
        mockMvc.perform(put("/demo/filmes/" + id).header("Authorization", bearer(a))
                .contentType(MediaType.APPLICATION_JSON).content(movie("Editado")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.titulo").value("Editado"));
        mockMvc.perform(delete("/demo/filmes/" + id).header("Authorization", bearer(a))).andExpect(status().isNoContent());
        JsonNode fake = body(mockMvc.perform(post("/demo/users").header("Authorization", bearer(a))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Ficticio\",\"email\":\"fake@example.invalid\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isCreated()).andReturn());
        mockMvc.perform(put("/demo/users/" + fake.path("id").asLong()).header("Authorization", bearer(a))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Editado\",\"email\":\"fake@example.invalid\",\"role\":\"USER\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/demo/users/" + fake.path("id").asLong()).header("Authorization", bearer(a)))
                .andExpect(status().isNoContent());
        JsonNode sampleUsers = body(mockMvc.perform(get("/demo/users").header("Authorization", bearer(a)))
                .andExpect(status().isOk()).andReturn());
        assertThat(sampleUsers.toString()).doesNotContain("owner@veloxplay.test", "demo@veloxplay.com", "senha");
        assertThat(users.count()).isEqualTo(realUsers);
        mockMvc.perform(get("/filmes")).andExpect(status().isOk()).andExpect(result ->
                assertThat(result.getResponse().getContentAsString()).isEqualTo(catalogBefore));
        mockMvc.perform(delete("/demo/reset").header("Authorization", bearer(a))).andExpect(status().isNoContent());
        mockMvc.perform(get("/demo/users").header("Authorization", bearer(a))).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void expiredSandboxRejectsAccessAndInvalidRequestsAreNotSaved() throws Exception {
        String token = login("moderador@veloxplay.com", "Moderador@123").path("accessToken").asText();
        mockMvc.perform(post("/demo/filmes").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{}" )).andExpect(status().isBadRequest());
        mockMvc.perform(get("/demo/filmes").header("Authorization", bearer(token))).andExpect(status().isOk());
        var workspace = workspaces.findAll().get(0);
        workspace.setExpiresAt(Instant.now().minusSeconds(1));
        workspaces.saveAndFlush(workspace);
        mockMvc.perform(get("/demo/filmes").header("Authorization", bearer(token))).andExpect(status().isUnauthorized());
    }

    @Test
    void administratorCanDeleteRealUserWithTokensAndMovieWithFavorites() throws Exception {
        JsonNode user = register("Excluir teste", "delete@example.test");
        long userId = user.path("user").path("id").asLong();
        String userToken = user.path("accessToken").asText();
        String admin = login("owner@veloxplay.test", "OwnerTest@123").path("accessToken").asText();
        JsonNode film = body(mockMvc.perform(post("/filmes").header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content(movie("Filme removivel")))
                .andExpect(status().isCreated()).andReturn());
        long filmId = film.path("id").asLong();
        mockMvc.perform(put("/filmes/" + filmId).header("Authorization", bearer(admin))
                .contentType(MediaType.APPLICATION_JSON).content(movie("Filme editado"))).andExpect(status().isOk());
        mockMvc.perform(post("/users/" + userId + "/favoritos/" + filmId).header("Authorization", bearer(userToken)))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/filmes/" + filmId).header("Authorization", bearer(admin))).andExpect(status().isNoContent());
        mockMvc.perform(delete("/users/" + userId).header("Authorization", bearer(admin))).andExpect(status().isNoContent());
        users.flush();
        assertThat(users.findById(userId)).isEmpty();
        assertThat(jdbc.queryForObject("select count(*) from refresh_tokens where user_id = ?", Long.class, userId)).isZero();
        mockMvc.perform(get("/auth/me").header("Authorization", bearer(userToken))).andExpect(status().isUnauthorized());
    }

    @Test
    void legacyPublicAdminIsDemotedAndSeedingWorksOnNonemptyDatabase() {
        User legacy = new User();
        legacy.setNome("Admin antigo");
        legacy.setEmail("admin@veloxplay.com");
        legacy.setSenha(encoder.encode("Admin@123"));
        legacy.setRole(Role.ADMIN);
        users.saveAndFlush(legacy);
        new AccountBootstrap(users, encoder, false, "moderador@veloxplay.com", "Moderador@123",
                "demo@veloxplay.com", "Demo@123", "", "").run();
        assertThat(legacy.getRole()).isEqualTo(Role.MODERATOR);
        assertThat(legacy.isDemoAccount()).isTrue();
        new AccountBootstrap(users, encoder, true, "newmod@example.test", "ModTest@123",
                "newdemo@example.test", "DemoTest@123", "private@example.test", "PrivateTest@123").run();
        assertThat(users.findByEmailIgnoreCase("newmod@example.test").orElseThrow().getRole()).isEqualTo(Role.MODERATOR);
        assertThat(users.findByEmailIgnoreCase("newdemo@example.test").orElseThrow().isDemoAccount()).isTrue();
        assertThat(users.findByEmailIgnoreCase("private@example.test").orElseThrow().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void demoQuotaAndCleanupAffectOnlyIsolatedData() throws Exception {
        String a = login("moderador@veloxplay.com", "Moderador@123").path("accessToken").asText();
        String b = login("moderador@veloxplay.com", "Moderador@123").path("accessToken").asText();
        mockMvc.perform(get("/demo/users").header("Authorization", bearer(a))).andExpect(status().isOk());
        mockMvc.perform(get("/demo/users").header("Authorization", bearer(b))).andExpect(status().isOk());
        long realUsers = users.count();
        for (int i = 0; i < 18; i++) {
            mockMvc.perform(post("/demo/users").header("Authorization", bearer(a))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nome\":\"Exemplo\",\"email\":\"fake@example.invalid\",\"role\":\"USER\"}"))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/demo/users").header("Authorization", bearer(a))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Excedeu\",\"email\":\"fake@example.invalid\",\"role\":\"USER\"}"))
                .andExpect(status().isTooManyRequests());
        var expired = workspaces.findAll().stream()
                .filter(w -> demoEntries.countByWorkspaceIdAndKind(w.getId(), "USER") == 20).findFirst().orElseThrow();
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        workspaces.saveAndFlush(expired);
        new DemoCleanup(demoEntries, workspaces).cleanup();
        workspaces.flush();
        assertThat(workspaces.count()).isEqualTo(1);
        assertThat(demoEntries.findByWorkspaceIdAndKindOrderById(expired.getId(), "USER")).isEmpty();
        assertThat(users.count()).isEqualTo(realUsers);
        mockMvc.perform(get("/demo/users").header("Authorization", bearer(b))).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void postgresUpgradeAcceptsModeratorWithOldRoleConstraint() throws Exception {
        try (var connection = dataSource.getConnection()) {
            org.junit.jupiter.api.Assumptions.assumeTrue(connection.getMetaData().getDatabaseProductName().equals("PostgreSQL"));
        }
        jdbc.update("update users set role = 'USER' where role = 'MODERATOR'");
        jdbc.execute("alter table users drop constraint if exists users_role_check");
        jdbc.execute("alter table users add constraint users_role_check check (role in ('USER', 'ADMIN'))");
        ResourceDatabasePopulator upgrade = new ResourceDatabasePopulator(new ClassPathResource("schema-postgresql.sql"));
        upgrade.setSeparator("@@");
        upgrade.execute(dataSource);
        long demoId = users.findByEmailIgnoreCase("demo@veloxplay.com").orElseThrow().getId();
        assertThat(jdbc.update("update users set role = 'MODERATOR' where id = ?", demoId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select role from users where id = ?", String.class, demoId)).isEqualTo("MODERATOR");
    }

    @Test
    void postgresUpgradeBackfillsLegacyColumnsWithoutDeletingRows() throws Exception {
        try (var connection = dataSource.getConnection()) {
            org.junit.jupiter.api.Assumptions.assumeTrue(connection.getMetaData().getDatabaseProductName().equals("PostgreSQL"));
        }
        long usersBefore = jdbc.queryForObject("select count(*) from users", Long.class);
        long filmsBefore = jdbc.queryForObject("select count(*) from filmes", Long.class);
        jdbc.execute("alter table users drop column role");
        jdbc.execute("alter table filmes drop column destaque");
        ResourceDatabasePopulator upgrade = new ResourceDatabasePopulator(new ClassPathResource("schema-postgresql.sql"));
        upgrade.setSeparator("@@");
        upgrade.execute(dataSource);
        upgrade.execute(dataSource); // Restarting the application must also succeed.
        assertThat(jdbc.queryForObject("select count(*) from users where role = 'USER'", Long.class)).isEqualTo(usersBefore);
        assertThat(jdbc.queryForObject("select count(*) from filmes where destaque = false", Long.class)).isEqualTo(filmsBefore);
        assertThat(jdbc.queryForObject("select count(*) from users", Long.class)).isEqualTo(usersBefore);
        assertThat(jdbc.queryForObject("select count(*) from filmes", Long.class)).isEqualTo(filmsBefore);
    }

    @Test
    void movieBackdropIsPersistedIndependentlyAndCanBeCleared() throws Exception {
        String access = login("owner@veloxplay.test", "OwnerTest@123").path("accessToken").asText();
        var request = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(movie("Fundo independente"));
        request.put("urlBackdrop", "/assets/backdrops/exemplo.jpg");
        JsonNode created = body(mockMvc.perform(post("/filmes").header("Authorization", bearer(access))
                .contentType(MediaType.APPLICATION_JSON).content(request.toString()))
                .andExpect(status().isCreated()).andReturn());
        long id = created.path("id").asLong();
        assertThat(created.path("urlCapa").asText()).isEqualTo("/assets/test.png");
        assertThat(created.path("urlBackdrop").asText()).isEqualTo("/assets/backdrops/exemplo.jpg");
        assertThat(jdbc.queryForObject("select url_backdrop from filmes where id = ?", String.class, id))
                .isEqualTo("/assets/backdrops/exemplo.jpg");
        request.put("urlBackdrop", "/assets/backdrops/novo.jpg");
        mockMvc.perform(put("/filmes/" + id).header("Authorization", bearer(access))
                .contentType(MediaType.APPLICATION_JSON).content(request.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.urlBackdrop").value("/assets/backdrops/novo.jpg"));
        request.put("urlBackdrop", " ");
        mockMvc.perform(put("/filmes/" + id).header("Authorization", bearer(access))
                .contentType(MediaType.APPLICATION_JSON).content(request.toString())).andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();
        mockMvc.perform(get("/filmes/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.urlBackdrop").isEmpty())
                .andExpect(jsonPath("$.urlCapa").value("/assets/test.png"));
        assertThat(jdbc.queryForObject("select url_backdrop from filmes where id = ?", String.class, id)).isNull();
    }

    private void assertMovieWritesForbidden(String access) throws Exception {
        mockMvc.perform(post("/filmes").header("Authorization", bearer(access))
                .contentType(MediaType.APPLICATION_JSON).content(movie("Bloqueado"))).andExpect(status().isForbidden());
        mockMvc.perform(put("/filmes/1").header("Authorization", bearer(access))
                .contentType(MediaType.APPLICATION_JSON).content(movie("Bloqueado"))).andExpect(status().isForbidden());
        mockMvc.perform(delete("/filmes/1").header("Authorization", bearer(access))).andExpect(status().isForbidden());
    }

    private String movie(String title) {
        return "{\"titulo\":\"" + title + "\",\"descricao\":\"Teste\",\"genero\":\"ACAO\",\"urlVideo\":\"https://example.invalid/video.mp4\",\"urlCapa\":\"/assets/test.png\"}";
    }

    private String bearer(String token) { return "Bearer " + token; }
    private JsonNode body(MvcResult result) throws Exception { return mapper.readTree(result.getResponse().getContentAsString()); }
    private JsonNode register(String name, String email) throws Exception {
        return body(mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"" + name + "\",\"email\":\"" + email + "\",\"senha\":\"Senha1234\"}"))
                .andExpect(status().isCreated()).andReturn());
    }

    private JsonNode login(String email, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString());
    }
}
