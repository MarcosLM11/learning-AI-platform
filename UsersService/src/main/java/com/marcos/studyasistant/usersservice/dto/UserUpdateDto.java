package com.marcos.studyasistant.usersservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UserUpdateDto (
        @NotBlank @NotEmpty String username,
        @NotBlank @NotEmpty @Email String email,
        @NotBlank @NotEmpty String name,
        @NotBlank String surname,
        @NotBlank String phone
) {}
