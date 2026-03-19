package mh.backend.userservice.service;

import mh.backend.tenant.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TenantUserDirectoryService {

    private final JdbcTemplate jdbcTemplate;

    public TenantUserDirectoryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void sync(String previousEmail, String currentEmail, String status) {
        String tenantId = TenantContext.requireTenantId();

        if (previousEmail != null && !previousEmail.equalsIgnoreCase(currentEmail)) {
            deleteByEmail(tenantId, previousEmail);
        }

        jdbcTemplate.update(
                """
                insert into public.tenant_user_directory (tenant_id, email, status)
                values (cast(? as uuid), ?, ?)
                on conflict (tenant_id, email)
                do update set status = excluded.status
                """,
                tenantId,
                currentEmail,
                status
        );
    }

    public void delete(String email) {
        deleteByEmail(TenantContext.requireTenantId(), email);
    }

    private void deleteByEmail(String tenantId, String email) {
        jdbcTemplate.update(
                """
                delete from public.tenant_user_directory
                where tenant_id = cast(? as uuid)
                  and email = ?
                """,
                tenantId,
                email
        );
    }
}
