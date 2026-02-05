package com.silverio.tasks.task.infrastructure.persistence;

import com.silverio.tasks.task.domain.model.Task;
import com.silverio.tasks.task.domain.model.TaskId;
import com.silverio.tasks.task.domain.model.TaskStatus;
import com.silverio.tasks.task.domain.repository.TaskRepository;
import com.silverio.tasks.task.infrastructure.mapper.TaskMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepositoryImpl implements TaskRepository {

  private final JpaTaskRepository jpa;

  public TaskRepositoryImpl(JpaTaskRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Task save(Task task) {
    var saved = jpa.save(TaskMapper.toEntity(task));
    return TaskMapper.toDomain(saved);
  }

  @Override
  public Optional<Task> findById(TaskId id) {
    // @Where já filtra deleted=false em todas as consultas
    return jpa.findById(id.value()).map(TaskMapper::toDomain);
  }

  @Override
  public List<Task> findAll(TaskStatus status) {
    var list = (status == null) ? jpa.findAllByDeletedFalse() : jpa.findAllByStatusAndDeletedFalse(status);
    return list.stream().map(TaskMapper::toDomain).toList();
  }

  @Override
  public void deletePhysical(TaskId id) {
    jpa.deleteById(id.value());
  }

  public List<Task> findAllByDeletedFalse() {
    return jpa.findAllByDeletedFalse()
        .stream()
        .map(TaskMapper::toDomain)
        .toList();
  }

  public List<Task> findAllByStatusAndDeletedFalse(TaskStatus status) {
    return jpa.findAllByStatusAndDeletedFalse(status)
        .stream()
        .map(TaskMapper::toDomain)
        .toList();
  }
}
