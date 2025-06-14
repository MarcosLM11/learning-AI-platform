package com.marcos.studyasistant.usersservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class UserConfig {

    @Value("${variable-de-prueba}")
    private String variableDePrueba;

    public String getVariableDePrueba() {
        return variableDePrueba;
    }
}
