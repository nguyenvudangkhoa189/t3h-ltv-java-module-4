package vn.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import vn.demo.security.GatewayUserFilter;

/**
 * CONFIG — Order: tin {@link vn.demo.security.GatewayUserFilter}.
 * API /api/orders/** vẫn cần identity từ Gateway.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayUserFilter gatewayUserFilter;

    /**
     * Configure HTTP security with gateway authentication
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 1) Disable CSRF for stateless API
                .csrf(AbstractHttpConfigurer::disable)
                
                // 2) Configure session management as stateless
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // 3) Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                    // Allow health check access
                    .requestMatchers("/actuator/health").permitAll()
                    // Require authentication for all other requests
                    .anyRequest().authenticated()
                )
                
                // 4) Add gateway user filter before default authentication
                .addFilterBefore(gatewayUserFilter, UsernamePasswordAuthenticationFilter.class)
                
                .build();
    }
}