package gut.dev.veloxplay.security;

import gut.dev.veloxplay.Model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AppUserDetails implements UserDetails {
    private final User user;
    private String sessionId;
    private java.time.Instant sessionExpiresAt;

    public AppUserDetails(User user) {
        this.user = user;
    }

    public User getUser() { return user; }
    public Long getId() { return user.getId(); }
    public String getSessionId() { return sessionId; }
    public java.time.Instant getSessionExpiresAt() { return sessionExpiresAt; }
    public void setSession(String id, java.time.Instant expiresAt) {
        this.sessionId = id;
        this.sessionExpiresAt = expiresAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() { return user.getSenha(); }

    @Override
    public String getUsername() { return user.getEmail(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
