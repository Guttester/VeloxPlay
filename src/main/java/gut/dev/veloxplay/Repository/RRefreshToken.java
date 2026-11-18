package gut.dev.veloxplay.Repository;

import gut.dev.veloxplay.Model.RefreshToken;
import gut.dev.veloxplay.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface RRefreshToken extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    void deleteByUser(User user);
}
