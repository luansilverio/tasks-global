package com.silverio.tasks.task.application.service;

import com.silverio.tasks.shared.exception.NotFoundException;
import com.silverio.tasks.task.domain.model.Task;
import com.silverio.tasks.task.domain.model.TaskId;
import com.silverio.tasks.task.domain.model.TaskPriority;
import com.silverio.tasks.task.domain.model.TaskStatus;
import com.silverio.tasks.task.domain.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

  private TaskRepository repository;
  private TaskService service;

  @BeforeEach
  void setUp() {
    repository = mock(TaskRepository.class);
    service = new TaskService(repository);
  }

  @Test
  void deve_criar_tarefa_com_status_inicial_todo() {
    // arrange
    var dueDate = LocalDateTime.of(2026, 2, 12, 0, 0);
    when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    // act
    var created = service.create("Título", "Descrição", dueDate, TaskPriority.HIGH);

    // assert
    assertThat(created).isNotNull();
    assertThat(created.getTitle()).isEqualTo("Título");
    assertThat(created.getDescription()).isEqualTo("Descrição");
    assertThat(created.getDueDate()).isEqualTo(dueDate);
    assertThat(created.getPriority()).isEqualTo(TaskPriority.HIGH);
    assertThat(created.getStatus()).isEqualTo(TaskStatus.TODO);

    verify(repository).save(any(Task.class));
  }

  @Test
  void list_sem_filtro_deve_chamar_findAll_com_status_null() {
    // arrange
    var t1 = task("A", TaskStatus.TODO, false);
    var t2 = task("B", TaskStatus.DOING, false);

    when(repository.findAll(null)).thenReturn(List.of(t1, t2));

    // act
    var result = service.list(null);

    // assert
    assertThat(result).hasSize(2);
    verify(repository).findAll(null);
    verify(repository, never()).findAll(any(TaskStatus.class));
  }

  @Test
  void list_com_status_deve_chamar_findAll_com_status_informado() {
    // arrange
    var t1 = task("A", TaskStatus.TODO, false);
    when(repository.findAll(TaskStatus.TODO)).thenReturn(List.of(t1));

    // act
    var result = service.list(TaskStatus.TODO);

    // assert
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.TODO);
    verify(repository).findAll(TaskStatus.TODO);
  }

  @Test
  void update_deve_lancar_not_found_quando_repo_nao_encontrar() {
    // arrange
    var id = new TaskId(UUID.randomUUID());
    when(repository.findById(id)).thenReturn(Optional.empty());

    // act + assert
    assertThatThrownBy(() ->
        service.update(id.value(), "Novo", null, TaskStatus.DOING, TaskPriority.MEDIUM,
            LocalDateTime.of(2026, 2, 12, 0, 0))
    ).isInstanceOf(NotFoundException.class)
     .hasMessageContaining("Tarefa não encontrada");

    verify(repository).findById(id);
    verify(repository, never()).save(any());
  }

  @Test
  void update_deve_atualizar_apenas_campos_informados() {
    // arrange
    var uuid = UUID.randomUUID();
    var id = new TaskId(uuid);

    var existing = task("Antigo", TaskStatus.TODO, false);
    existing.setId(id);
    existing.setDescription("Desc antiga");
    existing.setPriority(TaskPriority.LOW);
    existing.setDueDate(LocalDateTime.of(2026, 2, 10, 0, 0));

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    var newDue = LocalDateTime.of(2026, 2, 12, 0, 0);

    // act
    var updated = service.update(uuid, "Novo Título", null, TaskStatus.DOING, TaskPriority.HIGH, newDue);

    // assert
    assertThat(updated.getTitle()).isEqualTo("Novo Título");
    assertThat(updated.getDescription()).isEqualTo("Desc antiga"); // manteve
    assertThat(updated.getStatus()).isEqualTo(TaskStatus.DOING);
    assertThat(updated.getPriority()).isEqualTo(TaskPriority.HIGH);
    assertThat(updated.getDueDate()).isEqualTo(newDue);

    verify(repository).findById(id);
    verify(repository).save(existing);
  }

  @Test
  void delete_logico_deve_marcar_deleted_true_e_salvar() {
    // arrange
    var uuid = UUID.randomUUID();
    var id = new TaskId(uuid);

    var existing = task("A", TaskStatus.TODO, false);
    existing.setId(id);

    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    // act
    service.deleteLogical(uuid);

    // assert
    assertThat(existing.isDeleted()).isTrue();
    verify(repository).save(existing);
  }

  @Test
  void delete_logico_deve_lancar_not_found_quando_nao_encontrar() {
    // arrange
    var uuid = UUID.randomUUID();
    var id = new TaskId(uuid);

    when(repository.findById(id)).thenReturn(Optional.empty());

    // act + assert
    assertThatThrownBy(() -> service.deleteLogical(uuid))
      .isInstanceOf(NotFoundException.class)
      .hasMessageContaining("Tarefa não encontrada");

    verify(repository).findById(id);
    verify(repository, never()).save(any());
  }

  @Test
  void delete_fisico_deve_chamar_deletePhysical_com_taskId() {
    // arrange
    var uuid = UUID.randomUUID();
    var id = new TaskId(uuid);

    // act
    service.deletePhysical(uuid);

    // assert
    verify(repository).deletePhysical(id);
  }

  // ========= helpers =========
  private static Task task(String title, TaskStatus status, boolean deleted) {
    var t = new Task();
    t.setId(new TaskId(UUID.randomUUID()));
    t.setTitle(title);
    t.moveTo(status);
    if (deleted) {
      t.deleteLogical();
    }
    return t;
  }
}
