package mh.backend.tenant;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

public class TenantSchemaBootstrap {

    private final DataSource dataSource;
    private final TenantProperties tenantProperties;

    public TenantSchemaBootstrap(DataSource dataSource, TenantProperties tenantProperties) {
        this.dataSource = dataSource;
        this.tenantProperties = tenantProperties;
    }

    public void bootstrap() {
        if (!tenantProperties.isBootstrapEnabled()) {
            return;
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("tenant-bootstrap/001_public_registry.sql"),
                new ClassPathResource("tenant-bootstrap/002_tenant_template.sql"),
                new ClassPathResource("tenant-bootstrap/003_provision_tenant_schema.sql"),
                new ClassPathResource("tenant-bootstrap/004_seed_local_tenant.sql")
        );
        populator.execute(dataSource);
    }
}
