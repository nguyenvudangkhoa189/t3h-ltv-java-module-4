package vn.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Login Response DTO
 * 
 * Returns authentication tokens and user information after successful login.
 * Contains JWT access token for API requests and refresh token for session management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    
    private String refreshToken;
    
    private String tokenType;
    
    private UserResponse user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private String id;
        private String email;
        private String fullName;
        private List<String> roles;
    }
}