package com.marcos.studyasistant.apigateway.dto;

public record UserInfo(
        String id,
        String email,
        String name,
        String role
) {}
