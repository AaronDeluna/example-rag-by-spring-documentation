package ru.mirent.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * mvn spring-boot:run -pl webflux-mcp-server
 */
@SpringBootApplication
public class WebFluxServerApp {
    public static void main(String[] args) {
        SpringApplication.run(WebFluxServerApp.class, args);
    }
}