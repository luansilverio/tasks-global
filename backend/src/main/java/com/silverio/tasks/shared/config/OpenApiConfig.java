package com.silverio.tasks.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
      .info(new Info()
        .title("API de Tarefas (Kanban)")
        .description("API para gestão de tarefas com DDD, exclusão lógica e fluxo TODO/DOING/DONE.")
        .version("1.0.0")
        .contact(new Contact().name("Silvério")));
  }
}
