package mh.backend.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class RequestTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    private final TenantProperties tenantProperties;

    public RequestTenantIdentifierResolver(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return tenantProperties.getBootstrapTenantId();
        }
        return tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
