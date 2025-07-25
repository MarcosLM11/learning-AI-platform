package com.marcos.studyasistant.usersservice.dto;

import java.util.UUID;

public record UserResponseDto (
        UUID id,
        String username,
        String password,
        String email,
        String name,
        String surname,
        String phone,
        String role
) {}
