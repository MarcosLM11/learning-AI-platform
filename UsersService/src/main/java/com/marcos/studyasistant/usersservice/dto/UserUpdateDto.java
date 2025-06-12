package com.marcos.studyasistant.usersservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UserUpdateDto (
        String username,
        String email,
        String name,
        String surname,
        String phone
) {}
