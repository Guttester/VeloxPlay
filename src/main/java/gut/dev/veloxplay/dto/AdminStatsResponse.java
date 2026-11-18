package gut.dev.veloxplay.dto;

public class AdminStatsResponse {
    private long filmes;
    private long users;
    private long favoritos;

    public AdminStatsResponse(long filmes, long users, long favoritos) {
        this.filmes = filmes;
        this.users = users;
        this.favoritos = favoritos;
    }

    public long getFilmes() { return filmes; }
    public long getUsers() { return users; }
    public long getFavoritos() { return favoritos; }
}
