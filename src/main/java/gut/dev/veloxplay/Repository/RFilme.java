package gut.dev.veloxplay.Repository;

import gut.dev.veloxplay.Enum.Genero;
import gut.dev.veloxplay.Model.Filme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RFilme extends JpaRepository<Filme, Long> {
    boolean existsByTituloIgnoreCase(String titulo);
    Optional<Filme> findByTituloIgnoreCase(String titulo);
    List<Filme> findByGenero(Genero genero);
    List<Filme> findByDestaqueTrue();
}
