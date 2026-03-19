package mh.backend.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class TenantRequestFilter extends OncePerRequestFilter {

    private final TenantProperties tenantProperties;

    public TenantRequestFilter(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        List<String> protectedPaths = tenantProperties.getProtectedPaths();
        if (protectedPaths == null || protectedPaths.isEmpty()) {
            return true;
        }

        return protectedPaths.stream().noneMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = request.getHeader(tenantProperties.getHeaderName());
        if (!StringUtils.hasText(tenantId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing required header: " + tenantProperties.getHeaderName());
            return;
        }

        String normalizedTenantId = tenantId.strip();
        try {
            UUID.fromString(normalizedTenantId);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid tenant id format in header: " + tenantProperties.getHeaderName());
            return;
        }

        try {
            TenantContext.setTenantId(normalizedTenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
