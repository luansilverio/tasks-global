package com.silverio.tasks.task.interfaces.rest.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.silverio.tasks.task.domain.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateTaskRequest(
  @NotBlank(message = "Título é obrigatório")
  String title,
  String description,

  @NotNull(message = "Data limite é obrigatória")
  @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
  LocalDateTime dueDate,

  TaskPriority priority
) {}
