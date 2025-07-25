package com.marcos.studyasistant.apigateway.healthCheck;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GatewayHealthIndicator implements HealthIndicator {

    private final DiscoveryClient discoveryClient;

    public GatewayHealthIndicator(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Health health() {
        try {
            List<String> services = discoveryClient.getServices();
            Map<String, Object> details = new HashMap<>();

            List<String> expectedServices = Arrays.asList(
                    "user-service", "documents-service", "ai-processing-service"
            );

            boolean allServicesUp = true;

            for (String expectedService : expectedServices) {
                if (services.contains(expectedService)) {
                    List<ServiceInstance> instances = discoveryClient.getInstances(expectedService);
                    details.put(expectedService, Map.of(
                            "status", "UP",
                            "instances", instances.size()
                    ));
                } else {
                    details.put(expectedService, Map.of(
                            "status", "DOWN",
                            "instances", 0
                    ));
                    allServicesUp = false;
                }
            }

            details.put("total-services", services.size());
            details.put("expected-services", expectedServices.size());

            return allServicesUp ?
                    Health.up().withDetails(details).build() :
                    Health.down().withDetails(details).build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}