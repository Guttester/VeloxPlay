package gut.dev.veloxplay.Model;

import gut.dev.veloxplay.Enum.Genero;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "filmes")
public class Filme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Genero genero;

    @Column(nullable = false, length = 255)
    private String urlVideo;

    @Column(nullable = false, length = 255)
    private String urlCapa;

    @Column(name = "url_backdrop", length = 255)
    private String urlBackdrop;

    private Integer ano;

    private Integer duracaoMinutos;

    @Column(length = 8)
    private String classificacao;

    private Double avaliacao;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean destaque = false;

    public Filme() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public boolean isDestaque() { return destaque; }
    public void setDestaque(boolean destaque) { this.destaque = destaque; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Filme filme)) return false;
        return id != null && id.equals(filme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
