package org.erick.file_flow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI fileFlowOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("File Flow")
                .description("API para gerenciamento de jobs e documentos, incluindo criacao, consulta e geracao de URL de upload.")
                .version("v1"));
    }
}
