import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CdkDragDrop, DragDropModule, transferArrayItem } from '@angular/cdk/drag-drop';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { Task, TaskStatus } from '../task.model';
import { TaskService } from '../task.service';
import { TaskModalComponent } from '../task-modal/task-modal.component';
import { animate, state, style, transition, trigger } from '@angular/animations';

@Component({
  selector: 'app-board',
  standalone: true,
  imports: [CommonModule, DragDropModule, MatDialogModule, MatButtonModule, TaskModalComponent],
  templateUrl: './board.component.html',
  styleUrls: ['./board.component.scss'],
  animations: [
    trigger('collapse', [
      state('open', style({ height: '*', opacity: 1, paddingTop: '*', paddingBottom: '*' })),
      state('closed', style({ height: '0px', opacity: 0, paddingTop: '0px', paddingBottom: '0px' })),
      transition('open <=> closed', animate('220ms ease-in-out')),
    ]),
  ]
})
export class BoardComponent implements OnInit {
  todo: Task[] = [];
  doing: Task[] = [];
  done: Task[] = [];

  collapsed = {
    TODO: false,
    DOING: false,
    DONE: false
  };

  toggleColumn(status: 'TODO' | 'DOING' | 'DONE') {
    this.collapsed[status] = !this.collapsed[status];
  }

  isCollapsed(status: 'TODO' | 'DOING' | 'DONE') {
    return this.collapsed[status];
  }

  constructor(private tasks: TaskService, private dialog: MatDialog) { }

  ngOnInit(): void {
    this.reload();
  }

  reload() {
    this.tasks.list().subscribe(all => {
      this.todo = all.filter(t => t.status === 'TODO');
      this.doing = all.filter(t => t.status === 'DOING');
      this.done = all.filter(t => t.status === 'DONE');
    });
  }

  openNewTask() {
    const ref = this.dialog.open(TaskModalComponent, { width: '520px', data: null });
    ref.afterClosed().subscribe(result => {
      if (!result) return;
      this.tasks.create(result).subscribe(() => this.reload());
    });
  }

  openEdit(task: Task) {
    const ref = this.dialog.open(TaskModalComponent, { width: '520px', data: task });
    ref.afterClosed().subscribe(result => {
      if (!result) return;
      this.tasks.update(task.id, result).subscribe(() => this.reload());
    });
  }

  drop(event: CdkDragDrop<Task[]>, newStatus: 'TODO' | 'DOING' | 'DONE') {
    if (event.previousContainer === event.container) { return; }

    const movedTask = event.previousContainer.data[event.previousIndex];

    transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);

    this.tasks.update(movedTask.id, { status: newStatus }).subscribe({
      next: () => {
        // Opcional: manter a task local consistente
        movedTask.status = newStatus;
      },
      error: (err) => {
        // rollback: volta pro lugar anterior se falhar
        transferArrayItem(
          event.container.data,
          event.previousContainer.data,
          event.currentIndex,
          event.previousIndex
        );
        console.error(err);
        // se você tiver snackbar/toast, mostrar msg
      }
    });
  }

  priorityLabel(p: string | null | undefined): string {
    switch (p) {
      case 'LOW': return 'BAIXA';
      case 'MEDIUM': return 'MÉDIA';
      case 'HIGH': return 'ALTA';
      default: return '';
    }
  }

  priorityClass(p: string | null | undefined): string {
    // use classes CSS para cor do badge
    switch (p) {
      case 'LOW': return 'prio-low';
      case 'MEDIUM': return 'prio-medium';
      case 'HIGH': return 'prio-high';
      default: return '';
    }
  }

  /**
   * Converte "dd/MM/yyyy HH:mm" (BR) para Date (local).
   * Retorna null se inválido.
   */
  parseBrDateTime(value: string | null | undefined): Date | null {
    if (!value) return null;
    const [dpart, tpart] = value.trim().split(' ');
    if (!dpart) return null;

    const [dd, MM, yyyy] = dpart.split('/').map(Number);
    const [hh, mm] = (tpart || '00:00').split(':').map(Number);

    if (!dd || !MM || !yyyy) return null;
    const dt = new Date(yyyy, MM - 1, dd, hh || 0, mm || 0, 0, 0);

    // valida data real (evita 31/02)
    if (dt.getFullYear() !== yyyy || dt.getMonth() !== (MM - 1) || dt.getDate() !== dd) return null;

    return dt;
  }

  formatDueDate(value: string | null | undefined): string {
    const dt = this.parseBrDateTime(value);
    if (!dt) return value || '';

    const dd = String(dt.getDate()).padStart(2, '0');
    const MM = String(dt.getMonth() + 1).padStart(2, '0');
    const yyyy = dt.getFullYear();
    const HH = String(dt.getHours()).padStart(2, '0');
    const mm = String(dt.getMinutes()).padStart(2, '0');

    return `${dd}/${MM}/${yyyy} ${HH}:${mm}`; // mantém BR + 24h
  }

  trackById(_: number, t: Task) {
    return t.id;
  }

}
