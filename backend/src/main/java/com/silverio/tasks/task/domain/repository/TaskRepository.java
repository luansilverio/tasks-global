package com.silverio.tasks.task.domain.repository;

import com.silverio.tasks.task.domain.model.Task;
import com.silverio.tasks.task.domain.model.TaskId;
import com.silverio.tasks.task.domain.model.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
  Task save(Task task);
  Optional<Task> findById(TaskId id);
  List<Task> findAll(TaskStatus status);
  void deletePhysical(TaskId id);
  List<Task> findAllByDeletedFalse();
  List<Task> findAllByStatusAndDeletedFalse(TaskStatus status);
}
