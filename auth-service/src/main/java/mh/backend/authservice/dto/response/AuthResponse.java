package mh.backend.authservice.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String email;
    private String tokenType;
    private Long expiresIn;
    private List<String> scopes;
}
