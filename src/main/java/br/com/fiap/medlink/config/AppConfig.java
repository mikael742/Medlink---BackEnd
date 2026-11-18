package br.com.fiap.medlink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    /** Relógio da aplicação (fuso de Brasília) */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Medlink API")
                .version("1.0.0")
                .description("Plataforma inteligente de aproveitamento de consultas canceladas — "
                        + "Global Solution 2026.2 (CitizenTech)"));
    }
}
