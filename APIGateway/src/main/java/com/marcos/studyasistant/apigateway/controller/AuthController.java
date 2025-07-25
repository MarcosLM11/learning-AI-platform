package com.marcos.studyasistant.apigateway.controller;

import com.marcos.studyasistant.apigateway.dto.LoginRequestDto;
import com.marcos.studyasistant.apigateway.dto.LoginResponseDto;
import com.marcos.studyasistant.apigateway.dto.RegisterRequestDto;
import com.marcos.studyasistant.apigateway.exception.UserAlreadyExistsException;
import com.marcos.studyasistant.apigateway.exception.UserCreationException;
import com.marcos.studyasistant.apigateway.security.JwtTokenProvider;
import com.marcos.studyasistant.apigateway.service.UserServiceClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceClient userServiceClient;

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequest) {
        log.info("Intento de login para usuario: {}", loginRequest.email());

        // Validar credenciales con el UserService
        return userServiceClient.validateCredentials(loginRequest.email(), loginRequest.password())
                .map(userInfo -> {
                    // Credenciales válidas - generar token JWT
                    String token = jwtTokenProvider.generateToken(
                            userInfo.email(),
                            userInfo.id()
                    );

                    LoginResponseDto response = LoginResponseDto.builder()
                            .token(token)
                            .type("Bearer")
                            .userId(userInfo.id())
                            .email(userInfo.email())
                            .name(userInfo.name())
                            .build();

                    log.info("Login exitoso para usuario: {}", loginRequest.email());
                    return ResponseEntity.ok(response);
                })
                .switchIfEmpty(Mono.fromCallable(() -> {
                    // Credenciales inválidas
                    log.warn("Credenciales inválidas para usuario: {}", loginRequest.email());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(LoginResponseDto.builder()
                                    .build());
                }))
                .onErrorResume(error -> {
                    // Error interno del servidor
                    log.error("Error durante el login para usuario {}: {}",
                            loginRequest.email(), error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(LoginResponseDto.builder().build()));
                });
    }

    @GetMapping("/validate")
    public Mono<ResponseEntity<String>> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                return Mono.just(ResponseEntity.ok("Token válido para usuario: " + username));
            }
        }

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Token inválido"));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<LoginResponseDto>> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        log.info("Intento de registro para usuario: {}", registerRequest.email());



        return userServiceClient.createUser(registerRequest)
                .map(userInfo -> {
                    // Usuario creado exitosamente - generar token JWT
                    String token = jwtTokenProvider.generateToken(
                            userInfo.email(),
                            userInfo.id()
                    );

                    LoginResponseDto response = LoginResponseDto.builder()
                            .token(token)
                            .type("Bearer")
                            .userId(userInfo.id())
                            .email(userInfo.email())
                            .name(userInfo.name())
                            .build();

                    log.info("Registro y auto-login exitoso para: {}", registerRequest.email());
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .onErrorResume(UserAlreadyExistsException.class, error -> {
                    log.warn("Intento de registrar usuario ya existente: {}", registerRequest.email());
                    return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
                })
                .onErrorResume(UserCreationException.class, error -> {
                    log.warn("Error de validación en UserService: {}", error.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .onErrorResume(error -> {
                    log.warn("Error interno durante el registro: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}
