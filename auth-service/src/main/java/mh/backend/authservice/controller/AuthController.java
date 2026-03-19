package mh.backend.authservice.controller;

import mh.backend.authservice.dto.request.LoginRequest;
import mh.backend.authservice.dto.response.AuthResponse;
import mh.backend.authservice.service.SsoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@RestController
public class AuthController {

    private final SsoService ssoService;

    public AuthController(SsoService ssoService) {
        this.ssoService = ssoService;
    }

    @PostMapping("/api/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        boolean validUser = "user@example.com".equalsIgnoreCase(request.getEmail())
                && "admin123".equals(request.getPassword());

        if (!validUser) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(AuthResponse.builder()
                .token("demo-token-for-" + request.getEmail())
                .email(request.getEmail())
                .build());
    }

    @GetMapping("/api/sso/authorize-url")
    public ResponseEntity<Map<String, String>> authorizeUrl() {
        try {
            String authorizationUrl = ssoService.buildAuthorizationUrl();
            return ResponseEntity.ok(Map.of("authorizationUrl", authorizationUrl));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/api/sso/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code,
                                      @RequestParam("state") String state) {
        try {
            return ResponseEntity.ok(ssoService.exchangeCodeForToken(code, state));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", ex.getMessage()));
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("error", "OAuth token exchange failed with provider response.",
                            "providerStatus", String.valueOf(ex.getStatusCode().value()),
                            "providerBody", ex.getResponseBodyAsString()));
        }
    }
}
