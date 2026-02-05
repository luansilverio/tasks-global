package com.silverio.tasks.task.infrastructure.mapper;

import com.silverio.tasks.task.domain.model.Task;
import com.silverio.tasks.task.domain.model.TaskId;
import com.silverio.tasks.task.infrastructure.persistence.JpaTaskEntity;

public class TaskMapper {

  public static JpaTaskEntity toEntity(Task task) {
    var e = new JpaTaskEntity();
    e.setId(task.getId().value());
    e.setTitle(task.getTitle());
    e.setDescription(task.getDescription());
    e.setStatus(task.getStatus());
    e.setPriority(task.getPriority());
    e.setDueDate(task.getDueDate());
    e.setCreatedAt(task.getCreatedAt());
    e.setDeleted(task.isDeleted());
    return e;
  }

  public static Task toDomain(JpaTaskEntity e) {
    return new Task(
      new TaskId(e.getId()),
      e.getTitle(),
      e.getDescription(),
      e.getStatus(),
      e.getPriority(),
      e.getDueDate(),
      e.getCreatedAt(),
      e.isDeleted()
    );
  }
}
