package com.marcos.studyasistant.apigateway.security;

import com.marcos.studyasistant.apigateway.dto.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        log.info("=== FILTRO JWT REACTIVE === {} {}", method, path);

        // Rutas que NO necesitan autenticación
        if (isPublicPath(path)) {
            log.debug("Ruta pública permitida: {}", path);
            return chain.filter(exchange);
        }

        // Obtener token del header Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("Authorization header: {}", authHeader != null ? "Bearer ***" : "null");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Token JWT faltante para ruta protegida: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        log.info("Token extraído, longitud: {}", token.length());

        // Validar token
        boolean isValid = jwtTokenProvider.validateToken(token);
        log.info("Token válido: {}", isValid);

        if (!isValid) {
            log.warn("Token JWT inválido para ruta: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Token válido - obtener información del usuario
        String username = jwtTokenProvider.getClaimFromToken(token, "username");
        String userId = jwtTokenProvider.getClaimFromToken(token, "userId");
        String role = jwtTokenProvider.getClaimFromToken(token, "role");

        log.info("Usuario autenticado: {} (ID: {}, Rol: {}) accediendo a: {}", username, userId, role, path);

        // Verificar autorización por rol (básica)
        if (!isAuthorizedForPath(path, method, role)) {
            log.warn("Usuario {} con rol {} no autorizado para: {} {}",
                    username, role, method, path);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // Agregar información del usuario al contexto
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(originalRequest -> originalRequest
                        .header("X-User-Id", userId)
                        .header("X-Username", username)
                        .header("X-User-Role", role))
                .build();

        log.info("Headers de usuario añadidos a la petición");

        // Continuar con la cadena de filtros
        return chain.filter(mutatedExchange);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") ||          // Rutas de autenticación
                path.startsWith("/actuator/") ||          // Health checks
                path.equals("/") ||                       // Root
                path.startsWith("/favicon.ico");          // Favicon
    }

    private boolean isAuthorizedForPath(String path, String method, String roleString) {
        if (roleString == null) {
            return false;
        }
        Role role = Role.fromString(roleString);
        if (role == Role.ADMIN) {
            return true;
        }
        if (role == Role.USER) {
            return !path.contains("/admin/");  // Bloquear rutas admin
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -100; // Ejecutar antes que otros filtros
    }
}
