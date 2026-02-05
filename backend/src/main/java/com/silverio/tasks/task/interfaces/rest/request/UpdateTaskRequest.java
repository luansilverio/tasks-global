package com.silverio.tasks.task.interfaces.rest.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.silverio.tasks.task.domain.model.TaskPriority;
import com.silverio.tasks.task.domain.model.TaskStatus;

import java.time.LocalDateTime;

public record UpdateTaskRequest(
  String title,
  String description,
  TaskStatus status,
  TaskPriority priority,

  @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
  LocalDateTime dueDate
) {}
