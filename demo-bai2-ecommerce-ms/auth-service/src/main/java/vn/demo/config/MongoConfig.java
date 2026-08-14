package vn.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB Configuration
 * 
 * Enables MongoDB auditing for automatic timestamp management on entities.
 * Supports @CreatedDate and @LastModifiedDate annotations for user documents.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}