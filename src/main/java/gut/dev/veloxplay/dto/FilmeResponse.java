package gut.dev.veloxplay.dto;

import gut.dev.veloxplay.Enum.Genero;
import gut.dev.veloxplay.Model.Filme;

import java.util.List;

public class FilmeResponse {
    private Long id;
    private String titulo;
    private String descricao;
    private Genero genero;
    private String urlVideo;
    private String urlCapa;
    private String urlBackdrop;
    private Integer ano;
    private Integer duracaoMinutos;
    private String classificacao;
    private Double avaliacao;
    private boolean destaque;

    public static FilmeResponse from(Filme filme) {
        FilmeResponse dto = new FilmeResponse();
        dto.id = filme.getId();
        dto.titulo = filme.getTitulo();
        dto.descricao = filme.getDescricao();
        dto.genero = filme.getGenero();
        dto.urlVideo = filme.getUrlVideo();
        dto.urlCapa = filme.getUrlCapa();
        dto.urlBackdrop = filme.getUrlBackdrop();
        dto.ano = filme.getAno();
        dto.duracaoMinutos = filme.getDuracaoMinutos();
        dto.classificacao = filme.getClassificacao();
        dto.avaliacao = filme.getAvaliacao();
        dto.destaque = filme.isDestaque();
        return dto;
    }

    public static List<FilmeResponse> list(List<Filme> filmes) {
        return filmes.stream().map(FilmeResponse::from).toList();
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public Genero getGenero() { return genero; }
    public String getUrlVideo() { return urlVideo; }
    public String getUrlCapa() { return urlCapa; }
    public String getUrlBackdrop() { return urlBackdrop; }
    public Integer getAno() { return ano; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public String getClassificacao() { return classificacao; }
    public Double getAvaliacao() { return avaliacao; }
    public boolean isDestaque() { return destaque; }
}
