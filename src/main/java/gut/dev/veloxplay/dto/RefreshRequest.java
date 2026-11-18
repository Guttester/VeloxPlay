package gut.dev.veloxplay.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequest {
    @NotBlank(message = "Refresh token ausente")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
