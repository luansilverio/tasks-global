package com.silverio.tasks.task.application.service;

import com.silverio.tasks.shared.exception.BadRequestException;
import com.silverio.tasks.shared.exception.NotFoundException;
import com.silverio.tasks.task.domain.model.Task;
import com.silverio.tasks.task.domain.model.TaskId;
import com.silverio.tasks.task.domain.model.TaskPriority;
import com.silverio.tasks.task.domain.model.TaskStatus;
import com.silverio.tasks.task.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

  private final TaskRepository repo;

  public TaskService(TaskRepository repo) {
    this.repo = repo;
  }

  public Task create(String title, String description, LocalDateTime dueDate, TaskPriority priority) {
    try {
      var task = Task.create(title, description, dueDate, priority);
      return repo.save(task);
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Não foi possível criar a tarefa: " + ex.getMessage());
    }
  }

  public List<Task> list(TaskStatus status) {
    return repo.findAll(status);
  }

  public Task update(UUID id,
                     String title,
                     String description,
                     TaskStatus status,
                     TaskPriority priority,
                     LocalDateTime dueDate) {
    var task = repo.findById(new TaskId(id))
      .orElseThrow(() -> new NotFoundException("Tarefa não encontrada"));

    try {
      System.out.println(dueDate);
      if (title != null) task.setTitle(title);
      if (description != null) task.setDescription(description);
      if (status != null) task.moveTo(status);
      if (priority != null) task.setPriority(priority);
      if (dueDate != null) task.setDueDate(dueDate);
      return repo.save(task);
    } catch (IllegalArgumentException ex) {
      throw new BadRequestException("Não foi possível atualizar a tarefa: " + ex.getMessage());
    }
  }

  public void deleteLogical(UUID id) {
    var task = repo.findById(new TaskId(id))
      .orElseThrow(() -> new NotFoundException("Tarefa não encontrada"));

    task.deleteLogical();
    repo.save(task);
  }

  public void deletePhysical(UUID id) {
    repo.deletePhysical(new TaskId(id));
  }
}
