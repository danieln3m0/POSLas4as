package com.las4as.POSBackend.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class OpenApiConfiguration {

    @Bean
    public OpenAPI posBackendOpenApi() {

        var openApi = new OpenAPI();

        openApi
                .info(new Info()
                        .title("POSBackend API")
                        .description("Sistema de Gestión de Inventario de A&S Soluciones Generales E.I.R.L - API REST con enfoque Domain-Driven Design (DDD), pto el que lea esto en especial diego gaaaaaaaaaaaa")
                        .version("1.0.0")
                        .license(new License().name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation().
                        description("Documentación del Sistema POS")
                        .url("https://github.com/las4as/POSBackend"));

        final String securitySchemeName = "bearerAuth";

        openApi.addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));

        return openApi;
    }
}