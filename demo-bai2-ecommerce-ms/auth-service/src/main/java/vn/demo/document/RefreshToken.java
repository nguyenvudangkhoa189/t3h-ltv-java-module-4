package vn.demo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * RefreshToken Document Entity
 * 
 * Stores refresh tokens in MongoDB for token rotation and revocation.
 * Enables secure logout by blacklisting tokens and supports long-term
 * authentication sessions with proper token lifecycle management.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    @Indexed
    private String userId;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @Builder.Default
    private boolean revoked = false;

    /**
     * Check if refresh token is still valid
     * @return true if not expired and not revoked
     */
    public boolean isValid() {
        return !revoked && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Mark token as revoked (for logout)
     */
    public void revoke() {
        this.revoked = true;
    }
}