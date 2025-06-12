package com.marcos.studyasistant.usersservice.dto;

public record UserPasswordUpdateDto(
        String currentPassword,
        String newPassword
) {
}
