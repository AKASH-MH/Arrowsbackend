package mh.backend.tenant;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TenantSchemaRegistry {

    private final JdbcTemplate jdbcTemplate;
    private final TenantProperties tenantProperties;
    private final Map<String, String> schemaCache = new ConcurrentHashMap<>();

    public TenantSchemaRegistry(JdbcTemplate jdbcTemplate, TenantProperties tenantProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.tenantProperties = tenantProperties;
    }

    public String resolveSchema(String tenantId) {
        if (tenantProperties.getBootstrapTenantId().equals(tenantId)) {
            return tenantProperties.getBootstrapSchema();
        }

        return schemaCache.computeIfAbsent(tenantId, this::loadSchema);
    }

    public void evict(String tenantId) {
        schemaCache.remove(tenantId);
    }

    private String loadSchema(String tenantId) {
        return Optional.ofNullable(jdbcTemplate.query(
                        """
                        select schema_name
                        from public.tenant
                        where tenant_id = cast(? as uuid)
                          and is_active = true
                        """,
                        rs -> rs.next() ? rs.getString("schema_name") : null,
                        tenantId
                ))
                .orElseThrow(() -> new IllegalArgumentException("Active tenant not found: " + tenantId));
    }
}
