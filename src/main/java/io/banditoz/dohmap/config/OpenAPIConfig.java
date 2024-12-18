package io.banditoz.dohmap.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OpenAPIConfig {
    private final String serverUrl;

    public OpenAPIConfig(@Value("${dohmap.url:}") String serverUrl) {
        this.serverUrl = serverUrl;
        if (serverUrl != null && !serverUrl.isBlank()) {
            LoggerFactory.getLogger(this.getClass()).info("Using {} as the URL for Swagger docs.", serverUrl);
        }
    }

    @Bean
    @ConditionalOnProperty("dohmap.url")
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl(serverUrl);
        return new OpenAPI().servers(List.of(server));
    }
}
