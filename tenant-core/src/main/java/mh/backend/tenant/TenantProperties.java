package mh.backend.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "tenant")
public class TenantProperties {

    private String headerName = "X-Tenant-Id";
    private String bootstrapTenantId = "bootstrap";
    private String bootstrapSchema = "tenant_template";
    private boolean bootstrapEnabled = true;
    private List<String> protectedPaths = new ArrayList<>();

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getBootstrapTenantId() {
        return bootstrapTenantId;
    }

    public void setBootstrapTenantId(String bootstrapTenantId) {
        this.bootstrapTenantId = bootstrapTenantId;
    }

    public String getBootstrapSchema() {
        return bootstrapSchema;
    }

    public void setBootstrapSchema(String bootstrapSchema) {
        this.bootstrapSchema = bootstrapSchema;
    }

    public boolean isBootstrapEnabled() {
        return bootstrapEnabled;
    }

    public void setBootstrapEnabled(boolean bootstrapEnabled) {
        this.bootstrapEnabled = bootstrapEnabled;
    }

    public List<String> getProtectedPaths() {
        return protectedPaths;
    }

    public void setProtectedPaths(List<String> protectedPaths) {
        this.protectedPaths = protectedPaths;
    }
}
