package gut.dev.veloxplay.Config;

import gut.dev.veloxplay.Enum.Genero;
import gut.dev.veloxplay.Model.Filme;
import gut.dev.veloxplay.Repository.RFilme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    // Public demo streams (Google sample bucket / Blender open movies).
    private static final String BBB = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
    private static final String Sintel = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4";
    private static final String Tears = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4";
    private static final String Elephant = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4";
    private static final String ForBigger = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";
    private static final String Escape = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4";
    private static final String Fun = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4";
    private static final String Joyrides = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4";
    private static final String Meltdown = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4";

    @Bean
    CommandLineRunner seedCatalog(
            RFilme filmes,
            @Value("${app.seed.enabled:true}") boolean enabled
    ) {
        return args -> {
            if (!enabled) return;

            if (filmes.count() == 0) {
                filmes.save(filme("Interstellar",
                        "Uma equipe viaja além do sistema solar em busca de um novo lar para a humanidade, enquanto o tempo e o amor distorcem cada escolha.",
                        Genero.FICCAO, Tears, "/assets/urlCapa/Interstellar.png",
                        2014, 169, "10", 8.7, true));
                filmes.save(filme("Matrix Revolutions",
                        "A guerra entre humanos e máquinas chega ao desfecho. Neo enfrenta o destino da Matrix e o preço da liberdade.",
                        Genero.ACAO, Elephant, "/assets/urlCapa/Matrix_revolutions.jpg",
                        2003, 129, "14", 6.8, false));
                filmes.save(filme("Titanic",
                        "No transatlântico condenado, um romance impossível atravessa classes sociais enquanto o oceano espera.",
                        Genero.ROMANCE, BBB, "/assets/urlCapa/titanic.jpg",
                        1997, 195, "12", 7.9, false));
                filmes.save(filme("O Regresso",
                        "Abandonado e dado como morto, um homem atravessa o inverno selvagem em busca de justiça e sobrevivência.",
                        Genero.AVENTURA, Sintel, "/assets/urlCapa/o_regresso.png",
                        2015, 156, "16", 8.0, false));
                filmes.save(filme("Corra!",
                        "Um jovem visita a família da namorada e descobre que o convite esconde um pesadelo social e corporal.",
                        Genero.TERROR, Meltdown, "/assets/urlCapa/Corra!.png",
                        2017, 104, "16", 7.8, false));
                filmes.save(filme("Harry Potter e a Pedra Filosofal",
                        "Um menino descobre que é bruxo e entra para um mundo de magia, amizade e um segredo antigo escondido na escola.",
                        Genero.AVENTURA, Fun, "/assets/urlCapa/harry.jpg",
                        2001, 152, "L", 7.6, false));
                filmes.save(filme("A Garota no Trem",
                        "Uma passageira pensa testemunhar um crime no caminho de sempre — e a memória falha exatamente quando mais precisa dela.",
                        Genero.SUSPENSE, Escape, "/assets/urlCapa/garota_do_train.jpg",
                        2016, 112, "16", 6.5, false));
                filmes.save(filme("O Lado Bom da Vida",
                        "Dois estranhos se reconhecem no meio do caos emocional e tentam reconstruir a vida um no outro.",
                        Genero.DRAMA, Joyrides, "/assets/urlCapa/Lado_bom_da_vida.jpg",
                        2012, 122, "12", 7.7, false));
                filmes.save(filme("Antes do Amanhecer",
                        "Dois viajantes passam uma noite em Viena conversando como se o mundo pudesse caber numa conversa.",
                        Genero.ROMANCE, ForBigger, "/assets/urlCapa/Antes_do_amanhacer.jpg",
                        1995, 101, "12", 8.1, false));
                log.info("Seeded movie catalog ({} titles)", filmes.count());
            }
        };
    }

    private Filme filme(String titulo, String descricao, Genero genero, String video, String capa,
                        int ano, int duracao, String classificacao, double avaliacao, boolean destaque) {
        Filme f = new Filme();
        f.setTitulo(titulo);
        f.setDescricao(descricao);
        f.setGenero(genero);
        f.setUrlVideo(video);
        f.setUrlCapa(capa);
        f.setUrlBackdrop(null);
        f.setAno(ano);
        f.setDuracaoMinutos(duracao);
        f.setClassificacao(classificacao);
        f.setAvaliacao(avaliacao);
        f.setDestaque(destaque);
        return f;
    }
}
