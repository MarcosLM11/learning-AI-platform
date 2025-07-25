package com.marcos.studyasistant.apigateway.dto;

public record UserDto(
        String id,
        String email,
        String name,
        String password,
        String role
) {}
