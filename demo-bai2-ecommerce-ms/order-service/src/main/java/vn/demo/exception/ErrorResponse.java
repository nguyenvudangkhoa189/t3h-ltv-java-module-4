package vn.demo.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard Error Response DTO
 * 
 * Consistent error response format for all API endpoints.
 * Includes timestamp, status code, error details, and optional field validation errors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    
    private int status;
    
    private String code;
    
    private String message;
    
    private String path;
    
    private Map<String, String> fieldErrors;
}