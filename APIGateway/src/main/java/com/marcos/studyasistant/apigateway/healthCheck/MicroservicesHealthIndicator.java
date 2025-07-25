package com.marcos.studyasistant.apigateway.healthCheck;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class MicroservicesHealthIndicator implements HealthIndicator {

    private final DiscoveryClient discoveryClient;

    public MicroservicesHealthIndicator(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();

        try {
            List<String> availableServices = discoveryClient.getServices();
            List<String> requiredServices = Arrays.asList(
                    "user-service", "documents-service", "ai-processing-service"
            );

            boolean allRequiredServicesUp = true;

            for (String service : requiredServices) {
                if (availableServices.contains(service)) {
                    int instanceCount = discoveryClient.getInstances(service).size();
                    details.put(service, Map.of(
                            "status", "UP",
                            "instances", instanceCount
                    ));
                } else {
                    details.put(service, Map.of("status", "DOWN"));
                    allRequiredServicesUp = false;
                }
            }

            details.put("discovery-client", "UP");
            details.put("total-available-services", availableServices.size());

            return allRequiredServicesUp ?
                    Health.up().withDetails(details).build() :
                    Health.down().withDetails(details).build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("discovery-client", "DOWN")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}