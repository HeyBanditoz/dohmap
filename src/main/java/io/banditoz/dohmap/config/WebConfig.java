package io.banditoz.dohmap.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    private final String allowedOrigin;
    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    public WebConfig(@Value("${dohmap.origin:}") String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigin.isEmpty()) {
            log.warn("No origin configured.");
            registry.addMapping("/**");
        } else {
            registry.addMapping("/api/**")
                    .allowedOrigins(allowedOrigin)
                    .allowedMethods("PUT", "POST", "DELETE");
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/", "classpath:/resources/",
                        "classpath:/static/", "classpath:/public/");
    }
}
