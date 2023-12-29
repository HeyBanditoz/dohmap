package io.banditoz.dohmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@PropertySource("classpath:application.yml")
public class DohmapApplication {
    public static void main(String[] args) {
        SpringApplication.run(DohmapApplication.class, args);
    }
}
