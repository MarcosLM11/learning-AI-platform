package com.marcos.studyasistant.usersservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UserRequestDto (
        String username,
        String password,
        String email,
        String name,
        String surname,
        String phone,
        String role
) {}
