package com.marcos.studyasistant.apigateway.security;

import com.marcos.studyasistant.apigateway.dto.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        log.info("🔐 JWT Filter: {} {} - Gateway Route Processing", method, path);

        if (shouldSkipAuthentication(request, path)) {
            log.debug("✅ Skipping authentication for path: {}", path);
            return chain.filter(exchange);
        }

        if (isPublicPath(path)) {
            log.debug("✅ Public path allowed: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("Authorization header: {}", authHeader != null ? "Bearer ***" : "null");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("❌ Missing JWT token for protected route: {}", path);
            return handleUnauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        log.info("Token extracted, length: {}", token.length());

        // Validar token
        boolean isValid = jwtTokenProvider.validateToken(token);
        log.info("Token válido: {}", isValid);

        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("❌ Invalid JWT token for route: {}", path);
            return handleUnauthorized(exchange, "Invalid JWT token");
        }

        // Token válido - obtener información del usuario
        String username = jwtTokenProvider.getClaimFromToken(token, "username");
        String userId = jwtTokenProvider.getClaimFromToken(token, "userId");
        String role = jwtTokenProvider.getClaimFromToken(token, "role");

        log.info("✅ User authenticated: {} (ID: {}, Role: {}) accessing: {}",
                username, userId, role, path);

        // Verificar autorización por rol (básica)
        if (!isAuthorizedForPath(path, method, role)) {
            log.warn("❌ User {} with role {} not authorized for: {} {}",
                    username, role, method, path);
            return handleForbidden(exchange, "Insufficient permissions");
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(originalRequest -> originalRequest
                        .header("X-User-Id", userId)
                        .header("X-Username", username)
                        .header("X-User-Role", role)
                        .header("X-Authenticated", "true")
                        .header("X-Auth-Method", "JWT"))
                .build();

        log.debug("✅ User headers added to request for downstream services");
        return chain.filter(mutatedExchange);

    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") ||          // Rutas de autenticación
                path.startsWith("/actuator/") ||         // Health checks
                path.equals("/") ||                      // Root
                path.startsWith("/favicon.ico") ||       // Favicon
                path.startsWith("/static/") ||           // Recursos estáticos
                path.startsWith("/css/") ||             // CSS
                path.startsWith("/images/");            // Imágenes
    }

    private boolean shouldSkipAuthentication(ServerHttpRequest request, String path) {
        String skipAuthHeader = request.getHeaders().getFirst("X-Skip-Auth");
        if ("true".equals(skipAuthHeader)) {
            log.debug("Found X-Skip-Auth header, skipping authentication");
            return true;
        }

        String internalRequestHeader = request.getHeaders().getFirst("X-Internal-Request");
        if ("true".equals(internalRequestHeader)) {
            log.debug("Internal request detected, skipping authentication");
            return true;
        }

        return false;
    }

    private boolean isAuthorizedForPath(String path, String method, String roleString) {
        if (roleString == null) {
            return false;
        }
        try {
            Role role = Role.fromString(roleString);
            // ADMIN tiene acceso total
            if (role == Role.ADMIN) {
                log.debug("Admin user granted access to: {}", path);
                return true;
            }
            // USER tiene acceso limitado
            if (role == Role.USER) {
                boolean isAdminPath = path.contains("/admin/");
                if (isAdminPath) {
                    log.debug("User denied access to admin path: {}", path);
                    return false;
                }
                log.debug("User granted access to: {}", path);
                return true;
            }
        } catch (Exception e) {
            log.error("Error parsing role {}: {}", roleString, e.getMessage());
        }
        return false;
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message, java.time.Instant.now());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());

        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleForbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String body = String.format("{\"error\":\"Forbidden\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message, java.time.Instant.now());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
