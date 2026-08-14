package vn.demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.demo.document.RefreshToken;

import java.util.List;
import java.util.Optional;

/**
 * RefreshToken Repository Interface
 * 
 * Manages refresh token persistence for secure token rotation and revocation.
 * Supports cleanup of expired tokens and user session management.
 */
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    /**
     * Find refresh token by token string
     * @param token refresh token value
     * @return Optional refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a user
     * @param userId user ID
     * @return list of user's refresh tokens
     */
    List<RefreshToken> findByUserId(String userId);

    /**
     * Delete all refresh tokens for a user (logout all sessions)
     * @param userId user ID
     */
    void deleteByUserId(String userId);
}