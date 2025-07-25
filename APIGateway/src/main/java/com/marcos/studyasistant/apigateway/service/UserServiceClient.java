package com.marcos.studyasistant.apigateway.service;

import com.marcos.studyasistant.apigateway.dto.RegisterRequestDto;
import com.marcos.studyasistant.apigateway.dto.UserDto;
import com.marcos.studyasistant.apigateway.dto.UserInfo;
import com.marcos.studyasistant.apigateway.exception.UserAlreadyExistsException;
import com.marcos.studyasistant.apigateway.exception.UserCreationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<UserInfo> validateCredentials(String email, String password) {
        log.info("Validando credenciales para usuario: {}", email);

        return webClientBuilder.build()
                .get()
                .uri("lb://users-service/api/v1/users")  // Llamada via load balancer
                .retrieve()
                .bodyToFlux(UserDto.class)
                .filter(user -> user.email().equals(email))
                .next()  // Obtener el primer (y único) usuario que coincida
                .flatMap(user -> {
                    // NOTA: En producción, la contraseña debería estar hasheada
                    if (isValidPassword(password, user.password())) {
                        log.info("Credenciales válidas para usuario: {}", email);
                        return Mono.just(new UserInfo(
                                user.id().toString(),
                                user.email(),
                                user.name()
                        ));
                    } else {
                        log.warn("Contraseña inválida para usuario: {}", email);
                        return Mono.empty();
                    }
                })
                .doOnError(error -> log.error("Error validando credenciales: {}", error.getMessage()));
    }

    public Mono<UserInfo> createUser(RegisterRequestDto registerRequest) {
        log.info("Creando usuario via UserService: {}", registerRequest.email());

        return webClientBuilder.build()
                .post()
                .uri("lb://users-service/api/v1/users")
                .bodyValue(registerRequest)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> {
                            if (response.statusCode().value() == 409) {
                                log.warn("Ya existe un usuario registrado con ese email: {}", registerRequest.email());
                                return Mono.error(new UserAlreadyExistsException("Ya existe un usuario con ese email: " + registerRequest.email()));
                            }
                            return Mono.error(new UserCreationException("Error en los datos del usuario"));
                        }
                )
                .bodyToMono(UserDto.class)
                .map(userDto -> {
                    log.info("Usuario creado exitosamente: {}", userDto.email());
                    return new UserInfo(
                            userDto.id().toString(),
                            userDto.email(),
                            userDto.name()
                    );
                })
                .doOnError( error ->
                    log.error("Error creando usuario {}: {}", registerRequest.email(), error.getMessage())
                );
    }

    private boolean isValidPassword(String inputPassword, String storedPassword) {
        // TEMPORAL: Comparación directa (en producción usar BCrypt)
        return inputPassword.equals(storedPassword);
    }
}
