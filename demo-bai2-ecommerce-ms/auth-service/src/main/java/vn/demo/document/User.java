package vn.demo.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Document Entity
 * 
 * Represents user data in MongoDB auth_db.users collection.
 * Contains authentication and authorization information including
 * BCrypt hashed passwords and role-based access control.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password; // BCrypt hashed

    private String fullName;

    @Builder.Default
    private List<String> roles = List.of("ROLE_USER");

    @Builder.Default
    private String status = "ACTIVE";

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Check if user has admin privileges
     * @return true if user has ROLE_ADMIN
     */
    public boolean isAdmin() {
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    /**
     * Check if user account is active
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}