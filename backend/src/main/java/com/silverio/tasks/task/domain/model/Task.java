package com.silverio.tasks.task.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
  private final TaskId id;
  private String title;
  private String description;
  private TaskStatus status;
  private TaskPriority priority;
  private LocalDateTime dueDate;
  private final Instant createdAt;
  private boolean deleted;

  public Task(TaskId id,
              String title,
              String description,
              TaskStatus status,
              TaskPriority priority,
              LocalDateTime dueDate,
              Instant createdAt,
              boolean deleted) {
    this.id = Objects.requireNonNull(id, "id é obrigatório");
    setTitle(title);
    this.description = description;
    this.status = status == null ? TaskStatus.TODO : status;
    this.priority = priority == null ? TaskPriority.MEDIUM : priority;
    this.dueDate = Objects.requireNonNull(dueDate, "data limite é obrigatória");
    this.createdAt = createdAt == null ? Instant.now() : createdAt;
    this.deleted = deleted;
  }

  public static Task create(String title, String description, LocalDateTime dueDate, TaskPriority priority) {
    return new Task(TaskId.newId(), title, description, TaskStatus.TODO, priority, dueDate, Instant.now(), false);
  }

  public void setTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("título é obrigatório");
    }
    this.title = title.trim();
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void moveTo(TaskStatus newStatus) {
    this.status = Objects.requireNonNull(newStatus, "status é obrigatório");
  }

  public void setPriority(TaskPriority priority) {
    if (priority != null) this.priority = priority;
  }

  public void setDueDate(LocalDateTime dueDate) {
    this.dueDate = Objects.requireNonNull(dueDate, "data limite é obrigatória");
  }

  public void deleteLogical() {
    this.deleted = true;
  }

  public TaskId getId() { return id; }
  public String getTitle() { return title; }
  public String getDescription() { return description; }
  public TaskStatus getStatus() { return status; }
  public TaskPriority getPriority() { return priority; }
  public LocalDateTime getDueDate() { return dueDate; }
  public Instant getCreatedAt() { return createdAt; }
  public boolean isDeleted() { return deleted; }
}
