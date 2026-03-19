package mh.backend.authservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mh.backend.authservice.config.OAuthProviderProperties;
import mh.backend.authservice.dto.response.AuthResponse;
import mh.backend.authservice.dto.response.OAuthTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SsoService {

    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final OAuthProviderProperties oauthProviderProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Map<String, Instant> validStates = new ConcurrentHashMap<>();

    public SsoService(OAuthProviderProperties oauthProviderProperties) {
        this.oauthProviderProperties = oauthProviderProperties;
        this.restClient = RestClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    public String buildAuthorizationUrl() {
        validateEnabledAndConfigured();
        cleanupExpiredStates();

        String state = UUID.randomUUID().toString();
        validStates.put(state, Instant.now());

        return UriComponentsBuilder.fromUriString(oauthProviderProperties.getAuthorizationEndpoint())
                .queryParam("client_id", oauthProviderProperties.getClientId())
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", oauthProviderProperties.getRedirectUri())
                .queryParam("response_mode", "query")
                .queryParam("scope", String.join(" ", oauthProviderProperties.getScopes()))
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    public AuthResponse exchangeCodeForToken(String code, String state) {
        validateEnabledAndConfigured();
        validateState(state);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", oauthProviderProperties.getClientId());
        form.add("client_secret", oauthProviderProperties.getClientSecret());
        form.add("redirect_uri", oauthProviderProperties.getRedirectUri());

        OAuthTokenResponse tokenResponse = restClient.post()
                .uri(oauthProviderProperties.getTokenEndpoint())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(OAuthTokenResponse.class);

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new IllegalStateException("OAuth provider did not return an access token.");
        }

        return AuthResponse.builder()
                .token(tokenResponse.getAccessToken())
                .email(extractEmailFromIdToken(tokenResponse.getIdToken()))
                .tokenType(tokenResponse.getTokenType())
                .expiresIn(tokenResponse.getExpiresIn())
                .scopes(splitScopes(tokenResponse.getScope()))
                .build();
    }

    private void validateEnabledAndConfigured() {
        if (!oauthProviderProperties.isEnabled()) {
            throw new IllegalStateException("SSO is disabled. Set oauth2.provider.enabled=true to enable it.");
        }
        if (isBlank(oauthProviderProperties.getClientId())
                || isBlank(oauthProviderProperties.getClientSecret())
                || isBlank(oauthProviderProperties.getAuthorizationEndpoint())
                || isBlank(oauthProviderProperties.getTokenEndpoint())
                || isBlank(oauthProviderProperties.getRedirectUri())) {
            throw new IllegalStateException("OAuth provider configuration is incomplete.");
        }
        if (oauthProviderProperties.getAuthorizationEndpoint().contains("{")
                || oauthProviderProperties.getAuthorizationEndpoint().contains("}")
                || oauthProviderProperties.getTokenEndpoint().contains("{")
                || oauthProviderProperties.getTokenEndpoint().contains("}")) {
            throw new IllegalStateException("OAuth endpoint URL must be a concrete URL. Do not keep placeholders like {tenant-id}.");
        }
        if (oauthProviderProperties.getScopes() == null || oauthProviderProperties.getScopes().isEmpty()) {
            throw new IllegalStateException("OAuth scopes are missing.");
        }
    }

    private void validateState(String state) {
        cleanupExpiredStates();
        Instant issuedAt = validStates.remove(state);
        if (issuedAt == null) {
            throw new IllegalArgumentException("Invalid or expired OAuth state.");
        }
    }

    private void cleanupExpiredStates() {
        Instant now = Instant.now();
        validStates.entrySet().removeIf(entry -> Duration.between(entry.getValue(), now).compareTo(STATE_TTL) > 0);
    }

    private String extractEmailFromIdToken(String idToken) {
        if (isBlank(idToken)) {
            return null;
        }
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode payload = objectMapper.readTree(payloadJson);
            List<String> candidateClaims = Arrays.asList("preferred_username", "email", "upn", "unique_name");

            for (String claim : candidateClaims) {
                JsonNode claimNode = payload.get(claim);
                String value = claimNode == null ? null : claimNode.asText();
                if (!isBlank(value)) {
                    return value;
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private List<String> splitScopes(String scope) {
        if (isBlank(scope)) {
            return oauthProviderProperties.getScopes();
        }
        return Arrays.stream(scope.split("\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
