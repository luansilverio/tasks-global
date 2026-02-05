package com.silverio.tasks.task.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silverio.tasks.shared.exception.GlobalExceptionHandler;
import com.silverio.tasks.shared.exception.NotFoundException;
import com.silverio.tasks.task.application.service.TaskService;
import com.silverio.tasks.task.domain.model.Task;
import com.silverio.tasks.task.domain.model.TaskId;
import com.silverio.tasks.task.domain.model.TaskPriority;
import com.silverio.tasks.task.domain.model.TaskStatus;
import com.silverio.tasks.task.interfaces.rest.request.CreateTaskRequest;
import com.silverio.tasks.task.interfaces.rest.request.UpdateTaskRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class) // <-- se seu handler tiver outro pacote/nome, ajuste aqui
class TaskControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @MockBean TaskService service;

  // ---------- POST /api/tarefas ----------
  @Test
  void post_deve_criar_e_retornar_200_com_corpo() throws Exception {
    var req = new CreateTaskRequest(
        "Título",
        "Descrição",
        LocalDateTime.of(2026, 2, 12, 0, 0),
        TaskPriority.HIGH
    );

    var task = task(UUID.randomUUID(), "Título", "Descrição", TaskStatus.TODO, TaskPriority.HIGH, req.dueDate());
    when(service.create(eq("Título"), eq("Descrição"), eq(req.dueDate()), eq(TaskPriority.HIGH))).thenReturn(task);

    mvc.perform(post("/api/tarefas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(req)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(task.getId().value().toString()))
      .andExpect(jsonPath("$.title").value("Título"))
      .andExpect(jsonPath("$.description").value("Descrição"))
      .andExpect(jsonPath("$.status").value("TODO"))
      .andExpect(jsonPath("$.priority").value("HIGH"));

    verify(service).create("Título", "Descrição", req.dueDate(), TaskPriority.HIGH);
  }

  @Test
  void post_deve_retornar_400_quando_json_invalido() throws Exception {
    // JSON quebrado (vai disparar HttpMessageNotReadableException)
    var badJson = "{ \"title\": \"X\", ";

    mvc.perform(post("/api/tarefas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(badJson))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.mensagem").exists()); // sua resposta amigável do handler

    verify(service, never()).create(any(), any(), any(), any());
  }

  // ---------- GET /api/tarefas ----------
  @Test
  void get_deve_listar_sem_filtro() throws Exception {
    var t1 = task(UUID.randomUUID(), "A", null, TaskStatus.TODO, TaskPriority.LOW, LocalDateTime.now());
    var t2 = task(UUID.randomUUID(), "B", null, TaskStatus.DOING, TaskPriority.MEDIUM, LocalDateTime.now());

    when(service.list(null)).thenReturn(List.of(t1, t2));

    mvc.perform(get("/api/tarefas"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2))
      .andExpect(jsonPath("$[0].title").value("A"))
      .andExpect(jsonPath("$[1].title").value("B"));

    verify(service).list(null);
  }

  @Test
  void get_deve_listar_com_filtro_status() throws Exception {
    var t1 = task(UUID.randomUUID(), "A", null, TaskStatus.TODO, TaskPriority.LOW, LocalDateTime.now());
    when(service.list(TaskStatus.TODO)).thenReturn(List.of(t1));

    mvc.perform(get("/api/tarefas").param("status", "TODO"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(1))
      .andExpect(jsonPath("$[0].status").value("TODO"));

    verify(service).list(TaskStatus.TODO);
  }

  @Test
  void get_deve_retornar_400_quando_status_invalido() throws Exception {
    mvc.perform(get("/api/tarefas").param("status", "QUALQUER"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.mensagem").exists());

    verify(service, never()).list(any());
  }

  // ---------- PUT /api/tarefas/{id} ----------
  @Test
  void put_deve_atualizar_e_retornar_200() throws Exception {
    var id = UUID.randomUUID();

    var req = new UpdateTaskRequest(
        "Novo",
        "Desc",
        TaskStatus.DOING,
        TaskPriority.HIGH,
        LocalDateTime.of(2026, 2, 12, 0, 0)
    );

    var updated = task(id, "Novo", "Desc", TaskStatus.DOING, TaskPriority.HIGH, req.dueDate());
    when(service.update(eq(id), eq("Novo"), eq("Desc"), eq(TaskStatus.DOING), eq(TaskPriority.HIGH), eq(req.dueDate())))
        .thenReturn(updated);

    mvc.perform(put("/api/tarefas/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(req)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(id.toString()))
      .andExpect(jsonPath("$.title").value("Novo"))
      .andExpect(jsonPath("$.status").value("DOING"));

    verify(service).update(id, "Novo", "Desc", TaskStatus.DOING, TaskPriority.HIGH, req.dueDate());
  }

  @Test
  void put_deve_retornar_404_quando_nao_encontrada() throws Exception {
    var id = UUID.randomUUID();

    var req = new UpdateTaskRequest(
        "Novo",
        null,
        null,
        null,
        null
    );

    when(service.update(eq(id), any(), any(), any(), any(), any()))
      .thenThrow(new NotFoundException("Tarefa não encontrada"));

    mvc.perform(put("/api/tarefas/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(req)))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.mensagem").value("Tarefa não encontrada"));

    verify(service).update(eq(id), any(), any(), any(), any(), any());
  }

  // ---------- DELETE lógico /api/tarefas/{id} ----------
  @Test
  void delete_logico_deve_retornar_200() throws Exception {
    var id = UUID.randomUUID();

    mvc.perform(delete("/api/tarefas/{id}", id))
      .andExpect(status().isOk());

    verify(service).deleteLogical(id);
  }

  @Test
  void delete_logico_deve_retornar_404_quando_nao_encontrada() throws Exception {
    var id = UUID.randomUUID();
    doThrow(new NotFoundException("Tarefa não encontrada")).when(service).deleteLogical(id);

    mvc.perform(delete("/api/tarefas/{id}", id))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.mensagem").value("Tarefa não encontrada"));

    verify(service).deleteLogical(id);
  }

  // ---------- DELETE físico /api/tarefas/{id}/hard ----------
  @Test
  void delete_fisico_deve_retornar_200() throws Exception {
    var id = UUID.randomUUID();

    mvc.perform(delete("/api/tarefas/{id}/hard", id))
      .andExpect(status().isOk());

    verify(service).deletePhysical(id);
  }

  // ---------- helpers ----------
  private static Task task(UUID uuid, String title, String description,
                           TaskStatus status, TaskPriority priority, LocalDateTime dueDate) {
    var t = new Task();
    t.setId(new TaskId(uuid));
    t.setTitle(title);
    t.setDescription(description);
    t.moveTo(status);
    t.setPriority(priority);
    t.setDueDate(dueDate);
    return t;
  }
}
