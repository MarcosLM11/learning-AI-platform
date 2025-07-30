package com.marcos.studyasistant.apigateway.controller;

import com.marcos.studyasistant.apigateway.dto.LoginRequestDto;
import com.marcos.studyasistant.apigateway.dto.LoginResponseDto;
import com.marcos.studyasistant.apigateway.dto.RegisterRequestDto;
import com.marcos.studyasistant.apigateway.security.JwtTokenProvider;
import com.marcos.studyasistant.apigateway.service.AuthService;
import com.marcos.studyasistant.apigateway.service.UserServiceClient;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserServiceClient userServiceClient;
    private final AuthService authService;

    public AuthController(JwtTokenProvider jwtTokenProvider, UserServiceClient userServiceClient, AuthService authService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userServiceClient = userServiceClient;
        this.authService = authService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequest) {
        log.info("Intento de login para usuario: {}", loginRequest.email());
        return authService.login(loginRequest);
    }

    @GetMapping("/validate")
    public Mono<ResponseEntity<String>> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Intento de validar usuario: {}", authHeader);
        return authService.validateToken(authHeader);
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<LoginResponseDto>> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        log.info("Intento de registro para usuario: {}", registerRequest.email());
        return authService.register(registerRequest);
    }
}
