package com.silverio.tasks.task.interfaces.rest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import com.silverio.tasks.task.application.service.TaskService;
import com.silverio.tasks.task.domain.model.Task;
import com.silverio.tasks.task.domain.model.TaskStatus;
import com.silverio.tasks.task.interfaces.rest.request.CreateTaskRequest;
import com.silverio.tasks.task.interfaces.rest.request.UpdateTaskRequest;
import com.silverio.tasks.task.interfaces.rest.response.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tarefas")
@CrossOrigin(origins = "*")
public class TaskController {

  private final TaskService service;
  private static final ZoneId ZONE_BR = ZoneId.of("America/Sao_Paulo");

  public TaskController(TaskService service) {
    this.service = service;
  }

  @Operation(summary = "Criar tarefa", description = "Cria uma nova tarefa com título, descrição, data limite e prioridade.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tarefa criada com sucesso", content = @Content(schema = @Schema(implementation = Task.class))),
      @ApiResponse(responseCode = "400", description = "Requisição inválida")
  })
  @PostMapping
  public TaskResponse create(@RequestBody @Valid CreateTaskRequest req) {
    var task = service.create(req.title(), req.description(), req.dueDate(), req.priority());
    return toResponse(task);
  }

  @Operation(summary = "Listar tarefas", description = "Lista todas as tarefas (ignorando deletadas). Permite filtrar por status.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
  })
  @GetMapping
  public List<TaskResponse> list(
      @Parameter(description = "Filtro opcional por status: TODO, DOING, DONE") @RequestParam(required = false) TaskStatus status) {
    return service.list(status).stream().map(this::toResponse).toList();
  }

  @Operation(summary = "Atualizar tarefa", description = "Edita título/descrição/prioridade/data ou move status.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
      @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
      @ApiResponse(responseCode = "400", description = "Requisição inválida")
  })
  @PutMapping("/{id}")
  public TaskResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateTaskRequest req) {
    var task = service.update(id, req.title(), req.description(), req.status(), req.priority(), req.dueDate());
    return toResponse(task);
  }

  @Operation(summary = "Excluir tarefa (lógica)", description = "Marca a tarefa como deletada (deleted=true).")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tarefa deletada logicamente com sucesso"),
      @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
  })
  @DeleteMapping("/{id}")
  public void deleteLogical(@PathVariable UUID id) {
    service.deleteLogical(id);
  }

  @Operation(summary = "Excluir tarefa (física)", description = "Remove fisicamente o registro do banco (opcional).")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tarefa removida fisicamente com sucesso")
  })
  @DeleteMapping("/{id}/hard")
  public void deletePhysical(@PathVariable UUID id) {
    service.deletePhysical(id);
  }

  private TaskResponse toResponse(Task t) {
    var createdLocal = LocalDateTime.ofInstant(t.getCreatedAt(), ZONE_BR);
    return new TaskResponse(
        t.getId().value(),
        t.getTitle(),
        t.getDescription(),
        t.getStatus(),
        t.getPriority(),
        t.getDueDate(),
        createdLocal);
  }
}
