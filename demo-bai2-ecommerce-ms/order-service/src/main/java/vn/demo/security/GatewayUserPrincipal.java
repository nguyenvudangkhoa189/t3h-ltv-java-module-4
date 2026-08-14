package vn.demo.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Principal gắn từ header Gateway — không chứa password. */
@Data
@AllArgsConstructor
public class GatewayUserPrincipal {

    private String userId;
    
    private String email;

    /**
     * Get display name for the user (email)
     * @return user email as display name
     */
    public String getName() {
        return email;
    }
}