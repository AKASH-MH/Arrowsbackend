package mh.backend.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth2.provider")
public class OAuthProviderProperties {
    private boolean enabled = false;
    private String providerName = "MethodHub-OAuth2";
    private String clientId;
    private String clientSecret;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String redirectUri;
    private List<String> scopes = new ArrayList<>();
}
