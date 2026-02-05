package com.silverio.tasks.task.infrastructure.persistence;

import com.silverio.tasks.task.domain.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaTaskRepository extends JpaRepository<JpaTaskEntity, UUID> {
  List<JpaTaskEntity> findByStatus(TaskStatus status);
}
