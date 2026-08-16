package br.com.fiap.ecocompliance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String schemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("EcoCompliance API")
                        .description("API RESTful para governança e compliance ambiental (ESG). " +
                                "Gerencia empresas, licenças ambientais, auditorias, emissões de carbono, " +
                                "compensações e agendamentos de redução de carbono.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FIAP — Sprint Java")
                                .email("admin@ecocompliance.com")))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Informe o token JWT obtido em POST /auth/login")));
    }
}
