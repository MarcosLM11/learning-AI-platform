package com.marcos.studyasistant.usersservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record UserPasswordUpdateDto(
        @NotBlank @NotEmpty String currentPassword,
        @NotBlank @NotEmpty String newPassword
) {
}
