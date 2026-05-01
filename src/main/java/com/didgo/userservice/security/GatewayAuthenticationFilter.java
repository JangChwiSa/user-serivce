package com.didgo.userservice.security;

import com.didgo.userservice.common.api.ApiErrorResponse;
import com.didgo.userservice.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final ObjectMapper objectMapper;

    public GatewayAuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/")
                || isPublicAuthEndpoint(request)
                || isSwaggerEndpoint(uri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (userIdHeader == null || userIdHeader.isBlank()) {
            writeUnauthorized(response);
            return;
        }

        try {
            Long userId = Long.parseLong(userIdHeader);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    new AuthenticatedUser(userId),
                    null,
                    List.of()
            );
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (NumberFormatException exception) {
            writeUnauthorized(response);
        }
    }

    private boolean isPublicAuthEndpoint(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI();
        return "/api/auth/signup".equals(uri)
                || "/api/auth/login".equals(uri)
                || "/api/auth/reissue".equals(uri);
    }

    private boolean isSwaggerEndpoint(String uri) {
        return "/swagger-ui.html".equals(uri)
                || uri.startsWith("/swagger-ui/")
                || uri.startsWith("/v3/api-docs");
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(ErrorCode.INVALID_TOKEN.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                new ApiErrorResponse(ErrorCode.INVALID_TOKEN.code(), ErrorCode.INVALID_TOKEN.message())
        );
    }
}
