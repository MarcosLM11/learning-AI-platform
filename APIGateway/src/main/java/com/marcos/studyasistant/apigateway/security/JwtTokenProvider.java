package com.marcos.studyasistant.apigateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {


    private final SecretKey secretKey;
    private final long jwtExpirationMs;;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration}") long jwtExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
    }

    //Generar token JWT
    public String generateToken(String username, String userId, String role) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String getClaimFromToken(String token, String claimKey) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get(claimKey, String.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error al extraer claim {} del token: {}", claimKey, e.getMessage());
            return null;
        }
    }

    // Validar token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expirado: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT no soportado: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformado: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.error("Error de seguridad en token JWT: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("Token JWT vacío: {}", e.getMessage());
            return false;
        }
    }
}