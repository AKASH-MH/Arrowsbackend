package mh.backend.apigateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@RestController
public class GatewayController {

    private final RestClient authClient;
    private final RestClient userClient;
    private final RestClient clientClient;

    public GatewayController(@Value("${services.auth.base-url}") String authBaseUrl,
                             @Value("${services.user.base-url}") String userBaseUrl,
                             @Value("${services.client.base-url}") String clientBaseUrl) {
        this.authClient = RestClient.builder().baseUrl(authBaseUrl).build();
        this.userClient = RestClient.builder().baseUrl(userBaseUrl).build();
        this.clientClient = RestClient.builder().baseUrl(clientBaseUrl).build();
    }

    @PostMapping("/api/login")
    public ResponseEntity<String> login(@RequestBody String body) {
        try {
            return authClient.post()
                    .uri("/api/login")
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @GetMapping("/api/sso/authorize")
    public ResponseEntity<Void> authorize() {
        try {
            Map<?, ?> response = authClient.get()
                    .uri("/api/sso/authorize-url")
                    .retrieve()
                    .body(Map.class);

            String authorizationUrl = response == null ? null : String.valueOf(response.get("authorizationUrl"));
            if (!StringUtils.hasText(authorizationUrl)) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", authorizationUrl)
                    .build();
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .header("X-SSO-Error", ex.getResponseBodyAsString())
                    .build();
        }
    }

    @GetMapping("/api/sso/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code,
                                           @RequestParam("state") String state) {
        try {
            return authClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/sso/callback")
                            .queryParam("code", code)
                            .queryParam("state", state)
                            .build())
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @PostMapping("/api/users")
    public ResponseEntity<String> createUser(@RequestBody String body,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestBodySpec request = userClient.post()
                    .uri("/api/users")
                    .contentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(authHeader)) {
                request.header("Authorization", authHeader);
            }
            applyTenantHeader(request, tenantHeader);

            return request.body(body).retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<String> getUserById(@PathVariable String id,
                                              @RequestHeader(value = "Authorization", required = false) String authHeader,
                                              @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestHeadersSpec<?> request = userClient.get().uri("/api/users/{id}", id);
            if (StringUtils.hasText(authHeader)) {
                request = request.header("Authorization", authHeader);
            }
            request = applyTenantHeader(request, tenantHeader);

            return request.retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @GetMapping("/api/users")
    public ResponseEntity<String> getUsers(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestHeadersSpec<?> request = userClient.get().uri("/api/users");
            if (StringUtils.hasText(authHeader)) {
                request = request.header("Authorization", authHeader);
            }
            request = applyTenantHeader(request, tenantHeader);

            return request.retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @PutMapping("/api/users/{id}")
    public ResponseEntity<String> updateUser(@PathVariable String id,
                                             @RequestBody String body,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestBodySpec request = userClient.put()
                    .uri("/api/users/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(authHeader)) {
                request.header("Authorization", authHeader);
            }
            applyTenantHeader(request, tenantHeader);

            return request.body(body).retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestHeadersSpec<?> request = userClient.delete().uri("/api/users/{id}", id);
            if (StringUtils.hasText(authHeader)) {
                request = request.header("Authorization", authHeader);
            }
            request = applyTenantHeader(request, tenantHeader);

            return request.retrieve().toBodilessEntity();
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    @GetMapping("/api/client/health")
    public ResponseEntity<String> getClientHealth() {
        try {
            return clientClient.get()
                    .uri("/health")
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @PostMapping("/api/org-units")
    public ResponseEntity<String> createOrgUnit(@RequestBody String body,
                                                @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestBodySpec request = clientClient.post()
                    .uri("/api/org-units")
                    .contentType(MediaType.APPLICATION_JSON);
            applyTenantHeader(request, tenantHeader);
            return request.body(body).retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @GetMapping("/api/org-units")
    public ResponseEntity<String> getOrgUnits(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestHeadersSpec<?> request = clientClient.get().uri("/api/org-units");
            request = applyTenantHeader(request, tenantHeader);
            return request.retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @GetMapping("/api/org-units/{id}")
    public ResponseEntity<String> getOrgUnit(@PathVariable String id,
                                             @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestHeadersSpec<?> request = clientClient.get().uri("/api/org-units/{id}", id);
            request = applyTenantHeader(request, tenantHeader);
            return request.retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @PutMapping("/api/org-units/{id}")
    public ResponseEntity<String> updateOrgUnit(@PathVariable String id,
                                                @RequestBody String body,
                                                @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestBodySpec request = clientClient.put()
                    .uri("/api/org-units/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON);
            applyTenantHeader(request, tenantHeader);
            return request.body(body).retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @DeleteMapping("/api/org-units/{id}")
    public ResponseEntity<Void> deleteOrgUnit(@PathVariable String id,
                                              @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestHeadersSpec<?> request = clientClient.delete().uri("/api/org-units/{id}", id);
            request = applyTenantHeader(request, tenantHeader);
            return request.retrieve().toBodilessEntity();
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    @PostMapping("/api/clients")
    public ResponseEntity<String> createClient(@RequestBody String body,
                                               @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestBodySpec request = clientClient.post()
                    .uri("/api/clients")
                    .contentType(MediaType.APPLICATION_JSON);
            applyTenantHeader(request, tenantHeader);
            return request.body(body).retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @GetMapping("/api/clients")
    public ResponseEntity<String> getClients(
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestHeadersSpec<?> request = clientClient.get().uri("/api/clients");
            request = applyTenantHeader(request, tenantHeader);
            return request.retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @GetMapping("/api/clients/{id}")
    public ResponseEntity<String> getClient(@PathVariable String id,
                                            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestHeadersSpec<?> request = clientClient.get().uri("/api/clients/{id}", id);
            request = applyTenantHeader(request, tenantHeader);
            return request.retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @PutMapping("/api/clients/{id}")
    public ResponseEntity<String> updateClient(@PathVariable String id,
                                               @RequestBody String body,
                                               @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestBodySpec request = clientClient.put()
                    .uri("/api/clients/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON);
            applyTenantHeader(request, tenantHeader);
            return request.body(body).retrieve().toEntity(String.class);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    @DeleteMapping("/api/clients/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable String id,
                                             @RequestHeader(value = "X-Tenant-Id", required = false) String tenantHeader) {
        try {
            RestClient.RequestHeadersSpec<?> request = clientClient.delete().uri("/api/clients/{id}", id);
            request = applyTenantHeader(request, tenantHeader);
            return request.retrieve().toBodilessEntity();
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    private RestClient.RequestHeadersSpec<?> applyTenantHeader(RestClient.RequestHeadersSpec<?> request,
                                                               String tenantHeader) {
        if (StringUtils.hasText(tenantHeader)) {
            return request.header("X-Tenant-Id", tenantHeader);
        }
        return request;
    }

    private void applyTenantHeader(RestClient.RequestBodySpec request, String tenantHeader) {
        if (StringUtils.hasText(tenantHeader)) {
            request.header("X-Tenant-Id", tenantHeader);
        }
    }
}
