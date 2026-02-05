package com.silverio.tasks.task.interfaces.rest.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.silverio.tasks.task.domain.model.TaskPriority;
import com.silverio.tasks.task.domain.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
  UUID id,
  String title,
  String description,
  TaskStatus status,
  TaskPriority priority,

  @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
  LocalDateTime dueDate,

  @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
  LocalDateTime createdAt
) {}
