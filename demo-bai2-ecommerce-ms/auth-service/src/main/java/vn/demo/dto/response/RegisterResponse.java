package vn.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Register Response DTO
 * 
 * Returns user information after successful registration.
 * Does not include tokens - user must login separately after registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private String id;
    
    private String email;
    
    private String fullName;
    
    private List<String> roles;
    
    private String message;
}