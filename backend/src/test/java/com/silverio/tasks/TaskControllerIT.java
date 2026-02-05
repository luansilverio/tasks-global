package com.silverio.tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
    .withDatabaseName("tasksdb")
    .withUsername("postgres")
    .withPassword("postgres");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;

  @BeforeEach
  void setup() {
    // Flyway roda automaticamente no startup
  }

  @Test
  void deveCriarEListarTarefa() throws Exception {
    var payload = Map.of(
      "title", "Minha tarefa",
      "description", "Detalhe",
      "dueDate", LocalDateTime.now().plusDays(2).toString(),
      "priority", "HIGH"
    );

    var createdJson = mvc.perform(post("/api/tarefas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(payload)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").exists())
      .andExpect(jsonPath("$.status").value("TODO"))
      .andReturn().getResponse().getContentAsString();

    var created = om.readTree(createdJson);
    var id = created.get("id").asText();

    mvc.perform(get("/api/tarefas"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$[0].id").value(id));
  }

  @Test
  void naoDeveCriarSemTituloOuData() throws Exception {
    var payload = Map.of(
      "title", "",
      "dueDate", ""
    );

    mvc.perform(post("/api/tarefas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(payload)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.erro").value("Validação falhou"));
  }

  @Test
  void deveMoverStatusComPut() throws Exception {
    var payload = Map.of(
      "title", "Mover status",
      "description", "x",
      "dueDate", LocalDateTime.now().plusDays(1).toString(),
      "priority", "MEDIUM"
    );

    var createdJson = mvc.perform(post("/api/tarefas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(payload)))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    var id = om.readTree(createdJson).get("id").asText();

    mvc.perform(put("/api/tarefas/" + id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(Map.of("status", "DOING"))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("DOING"));

    mvc.perform(get("/api/tarefas").param("status", "DOING"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(id));
  }

  @Test
  void tarefaDeletadaLogicamenteNaoDeveAparecerEmConsultas() throws Exception {
    var payload = Map.of(
      "title", "Excluir",
      "description", "x",
      "dueDate", LocalDateTime.now().plusDays(1).toString(),
      "priority", "LOW"
    );

    var createdJson = mvc.perform(post("/api/tarefas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(payload)))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();

    var id = om.readTree(createdJson).get("id").asText();

    mvc.perform(delete("/api/tarefas/" + id))
      .andExpect(status().isOk());

    mvc.perform(get("/api/tarefas"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$").isEmpty());

    mvc.perform(put("/api/tarefas/" + id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(Map.of("title", "nao deve achar"))))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.mensagem").value("Tarefa não encontrada"));
  }
}
