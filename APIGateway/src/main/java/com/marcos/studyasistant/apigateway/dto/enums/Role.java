package com.marcos.studyasistant.apigateway.dto.enums;

public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    MODERATOR("ROLE_MODERATOR");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

    public static Role fromString(String roleString) {
        for (Role role : Role.values()) {
            if (role.name().equals(roleString)) {
                return role;
            }
        }
        return USER; // Por defecto
    }
}
