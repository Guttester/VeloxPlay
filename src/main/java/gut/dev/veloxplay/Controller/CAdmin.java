package gut.dev.veloxplay.Controller;

import gut.dev.veloxplay.Repository.RFilme;
import gut.dev.veloxplay.Repository.RUser;
import gut.dev.veloxplay.dto.AdminStatsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class CAdmin {
    private final RFilme filmes;
    private final RUser users;

    public CAdmin(RFilme filmes, RUser users) {
        this.filmes = filmes;
        this.users = users;
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return new AdminStatsResponse(filmes.count(), users.count(), users.countFavoritos());
    }
}
