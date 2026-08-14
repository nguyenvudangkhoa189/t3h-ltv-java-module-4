package vn.demo.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Refresh Token Request DTO
 * 
 * Validates refresh token input for token rotation endpoint.
 * Ensures proper token format for secure session management.
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}