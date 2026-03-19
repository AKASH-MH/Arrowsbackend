package mh.backend.tenant;

import org.hibernate.context.spi.TenantSchemaMapper;

public class LookupTenantSchemaMapper implements TenantSchemaMapper<String> {

    private final TenantSchemaRegistry tenantSchemaRegistry;

    public LookupTenantSchemaMapper(TenantSchemaRegistry tenantSchemaRegistry) {
        this.tenantSchemaRegistry = tenantSchemaRegistry;
    }

    @Override
    public String schemaName(String tenantId) {
        return tenantSchemaRegistry.resolveSchema(tenantId);
    }
}
