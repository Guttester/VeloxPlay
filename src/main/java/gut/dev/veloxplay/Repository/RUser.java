package gut.dev.veloxplay.Repository;

import gut.dev.veloxplay.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RUser extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNomeIgnoreCase(String nome);

    @Query(value = "select count(*) from user_favoritos", nativeQuery = true)
    long countFavoritos();

    java.util.List<User> findDistinctByFavoritosId(Long filmeId);
}
