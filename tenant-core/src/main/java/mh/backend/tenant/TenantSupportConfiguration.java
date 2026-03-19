package mh.backend.tenant;

import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.context.spi.TenantSchemaMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TenantProperties.class)
public class TenantSupportConfiguration {

    @Bean
    public FilterRegistrationBean<TenantRequestFilter> tenantRequestFilter(TenantProperties tenantProperties) {
        FilterRegistrationBean<TenantRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TenantRequestFilter(tenantProperties));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    public TenantSchemaRegistry tenantSchemaRegistry(JdbcTemplate jdbcTemplate, TenantProperties tenantProperties) {
        return new TenantSchemaRegistry(jdbcTemplate, tenantProperties);
    }

    @Bean
    @ConditionalOnBean(TenantSchemaRegistry.class)
    public CurrentTenantIdentifierResolver<String> currentTenantIdentifierResolver(TenantProperties tenantProperties) {
        return new RequestTenantIdentifierResolver(tenantProperties);
    }

    @Bean
    @ConditionalOnBean(TenantSchemaRegistry.class)
    public TenantSchemaMapper<String> tenantSchemaMapper(TenantSchemaRegistry tenantSchemaRegistry) {
        return new LookupTenantSchemaMapper(tenantSchemaRegistry);
    }

    @Bean
    @ConditionalOnBean(TenantSchemaRegistry.class)
    public HibernatePropertiesCustomizer tenantHibernatePropertiesCustomizer(
            CurrentTenantIdentifierResolver<String> tenantIdentifierResolver,
            TenantSchemaMapper<String> tenantSchemaMapper,
            TenantProperties tenantProperties
    ) {
        return properties -> {
            properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
            properties.put(MultiTenancySettings.MULTI_TENANT_SCHEMA_MAPPER, tenantSchemaMapper);
            properties.put(MultiTenancySettings.TENANT_IDENTIFIER_TO_USE_FOR_ANY_KEY,
                    tenantProperties.getBootstrapTenantId());
        };
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    public TenantSchemaBootstrap tenantSchemaBootstrap(DataSource dataSource, TenantProperties tenantProperties) {
        return new TenantSchemaBootstrap(dataSource, tenantProperties);
    }

    @Bean
    @ConditionalOnBean(TenantSchemaBootstrap.class)
    public ApplicationRunner tenantSchemaBootstrapRunner(TenantSchemaBootstrap tenantSchemaBootstrap) {
        return args -> tenantSchemaBootstrap.bootstrap();
    }
}
