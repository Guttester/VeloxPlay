# VeloxPlay backend

## PostgreSQL e execução

A aplicação usa PostgreSQL. H2 é dependência exclusiva dos testes: não existe fallback
para banco local. O banco `veloxplay` precisa existir no servidor PostgreSQL.

Configure as variáveis de `.env.example` na execução da IDE, no terminal, na
hospedagem ou em um arquivo `.env` na pasta do backend. A aplicação carrega esse
arquivo automaticamente quando iniciada dessa pasta; variáveis de ambiente têm
prioridade. Use sintaxe Java `.properties`, sem aspas em torno dos valores.
O `.env` está ignorado pelo Git e não é incluído no JAR. São obrigatórios
`SPRING_DATASOURCE_PASSWORD` e `JWT_SECRET` (chave privada com pelo menos 32 bytes).
URL padrão: `jdbc:postgresql://localhost:5432/veloxplay`; usuário padrão: `postgres`.

Um `.env` local com uma chave JWT aleatória foi preparado neste checkout. Preencha
nele `SPRING_DATASOURCE_PASSWORD` e ajuste URL/usuário ao PostgreSQL do seu pgAdmin.
Não publique nem compartilhe o arquivo. Na hospedagem, use variáveis de ambiente.

Exemplo no PowerShell (preencha os valores no seu ambiente):

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/veloxplay'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
# Configure SPRING_DATASOURCE_PASSWORD e JWT_SECRET na sua execução privada.
mvn spring-boot:run
```

A porta HTTP padrão é 8080. Os dados antigos em `data/veloxplay.mv.db` não são
apagados nem importados: o catálogo e as contas demo são preparados no PostgreSQL.
O usuário do banco deve poder criar/alterar tabelas enquanto `ddl-auto=update` for
usado. `schema-postgresql.sql` cria/preenche `role=USER` e `destaque=false` nas
tabelas antigas e amplia o CHECK de `role`, antes de o Hibernate e o bootstrap
executarem. Não apaga registros existentes nem promove usuários antigos a ADMIN.

## Contas e permissões

| Perfil | Permissões |
| --- | --- |
| Visitante | `GET /filmes` e `GET /filmes/{id}` |
| USER | Catálogo, perfil próprio e favoritos próprios; não exclui usuários nem altera filmes |
| MODERATOR | Funções de USER e CRUD isolado em `/demo`; sem acesso a usuários reais e administração real |
| ADMIN | Administração real de filmes e usuários; também pode testar `/demo` |

As contas públicas padrão são `demo@veloxplay.com / Demo@123` (USER) e
`moderador@veloxplay.com / Moderador@123` (MODERATOR). São persistidas no banco.
O bootstrap cria cada conta que estiver ausente, inclusive em banco já populado.
Contas demo não podem ser editadas ou excluídas pela API, nem por ADMIN. Seus
favoritos continuam funcionando e são compartilhados por quem usa a mesma conta.

A antiga conta pública `admin@veloxplay.com` perde ADMIN na inicialização,
mesmo com `APP_SEED_ENABLED=false`. Se o e-mail do novo moderador estiver livre,
ela é renomeada e recebe a senha configurada para o moderador. Se já houver
moderador, a conta antiga permanece protegida com MODERATOR, sem privilégios reais.
E-mails de sistema são reservados contra cadastro e renomeação via API.

Para criar o administrador privado, configure `ADMIN_EMAIL` e `ADMIN_PASSWORD`
(12–72 caracteres). Não há senha privada padrão. Não use os e-mails públicos.
Uma conta já existente com esse e-mail deve ser ADMIN; o bootstrap não promove
uma conta comum nem redefine a senha de um administrador existente.
O administrador não pode excluir a si próprio por essa rota.

`APP_SEED_ENABLED=false` impede a criação de contas demo ausentes e do catálogo;
não remove dados existentes nem desativa as proteções das contas já cadastradas.
O catálogo inicial é criado apenas quando a tabela de filmes está vazia.

## CRUD demonstrativo

Faça login como MODERATOR e envie `Authorization: Bearer <accessToken>`:

| Métodos | Rota | Dados |
| --- | --- | --- |
| GET, POST | `/demo/filmes` | Lista/cria filmes fictícios |
| GET, PUT, DELETE | `/demo/filmes/{id}` | Consulta/edita/exclui filme fictício |
| GET, POST | `/demo/users` | Lista/cria usuários fictícios |
| GET, PUT, DELETE | `/demo/users/{id}` | Consulta/edita/exclui usuário fictício |
| GET | `/demo/stats` | Contagens da demonstração |
| DELETE | `/demo/reset` | Restaura apenas a demonstração da sessão atual |

Filmes usam o mesmo corpo de `POST /filmes`. Usuários fictícios recebem
`{"nome":"Exemplo","email":"exemplo@example.invalid","role":"USER"}`.
Não recebem senha e nunca podem fazer login; até um role ADMIN nesse registro
é apenas dado fictício, sem autoridade no sistema real.

Cada access token possui um identificador aleatório assinado (`jti`). A área demo
é associada a esse identificador, não ao e-mail compartilhado. Dois logins da
mesma conta recebem áreas diferentes. Copiar o mesmo token compartilha a mesma
área. Renovar o token inicia uma nova demonstração.

A área começa com até 40 cópias de filmes públicos e dois usuários fictícios.
Não copia usuários reais, senhas, e-mails privados ou favoritos. Os registros ficam
somente em `demo_workspaces` e `demo_entries`; não aparecem no catálogo público.
IDs de outras sessões retornam 404. Não há publicação nem promoção de dados demo.

O limite é de 50 filmes e 20 usuários por área, e 200 áreas simultâneas. A duração
é o menor valor entre `DEMO_TTL_MINUTES` (padrão 60, de 1 a 1440) e a expiração
do access token (padrão 15 minutos). Áreas expiradas são rejeitadas; a limpeza
automática roda a cada 10 minutos e remove apenas dados demo expirados. Desative
com `DEMO_CLEANUP_ENABLED=false` se preferir limpeza operacional própria.

## Integração do frontend

O frontend apresenta a conta pública de moderador e reconhece `MODERATOR`.
O componente de `/moderador` usa `/demo/filmes` e `/demo/users`, calcula as contagens
a partir das listas e permite restaurar a área com `/demo/reset`. ADMIN continua
usando `/filmes`, `/users` e `/admin/stats` no painel real de `/admin`.
Esconder botões não é a proteção: o backend retorna 403 nas operações proibidas.

## Verificação

`mvn test` executa os testes em H2 em memória, sem tocar o PostgreSQL real.
Os testes cobrem privilégios, proteção das contas demo, favoritos, isolamento
entre sessões, CRUD real do administrador, migração da conta antiga e tokens.
