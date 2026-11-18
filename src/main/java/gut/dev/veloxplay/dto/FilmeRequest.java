package gut.dev.veloxplay.dto;

import gut.dev.veloxplay.Enum.Genero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FilmeRequest {
    @NotBlank
    @Size(max = 100)
    private String titulo;

    @NotBlank
    @Size(max = 500)
    private String descricao;

    @NotNull
    private Genero genero;

    @NotBlank
    @Size(max = 255)
    private String urlVideo;

    @NotBlank
    @Size(max = 255)
    private String urlCapa;

    @Size(max = 255)
    private String urlBackdrop;

    private Integer ano;
    private Integer duracaoMinutos;

    @Size(max = 8)
    private String classificacao;

    private Double avaliacao;
    private Boolean destaque;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Genero getGenero() { return genero; }
    public void setGenero(Genero genero) { this.genero = genero; }
    public String getUrlVideo() { return urlVideo; }
    public void setUrlVideo(String urlVideo) { this.urlVideo = urlVideo; }
    public String getUrlCapa() { return urlCapa; }
    public void setUrlCapa(String urlCapa) { this.urlCapa = urlCapa; }
    public String getUrlBackdrop() { return urlBackdrop; }
    public void setUrlBackdrop(String urlBackdrop) { this.urlBackdrop = urlBackdrop; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public String getClassificacao() { return classificacao; }
    public void setClassificacao(String classificacao) { this.classificacao = classificacao; }
    public Double getAvaliacao() { return avaliacao; }
    public void setAvaliacao(Double avaliacao) { this.avaliacao = avaliacao; }
    public Boolean getDestaque() { return destaque; }
    public void setDestaque(Boolean destaque) { this.destaque = destaque; }
}
