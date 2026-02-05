import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { Task } from '../task.model';

@Component({
  selector: 'app-task-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule
  ],
  templateUrl: './task-modal.component.html'
})
export class TaskModalComponent {
  form = this.fb.group({
    title: ['', [Validators.required, Validators.minLength(2)]],
    description: [''],

    // Separado: data (Date) + hora (string HH:mm)
    dueDateDate: [null as Date | null, [Validators.required]],
    dueDateTime: ['00:00', [
      Validators.required,
      Validators.pattern(/^([01]\d|2[0-3]):[0-5]\d$/)
    ]],


    priority: ['MEDIUM', [Validators.required]],
    status: ['TODO']
  });

  constructor(
    private fb: FormBuilder,
    private ref: MatDialogRef<TaskModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Task | null
  ) {
    if (data) {
      // Backend devolve dueDate como "dd/MM/yyyy HH:mm"
      const parsed = this.parseBrDateTime(data.dueDate);

      this.form.patchValue({
        title: data.title,
        description: data.description || '',
        dueDateDate: parsed.date,
        dueDateTime: parsed.time,
        priority: data.priority,
        status: data.status ?? 'TODO'
      });
    } else {
      // default: 00:00, data obrigatória (usuário escolhe)
      this.form.patchValue({ dueDateTime: '00:00' });
    }
  }

  private parseBrDateTime(value: string): { date: Date; time: string } {
    // Espera "dd/MM/yyyy HH:mm"
    const parts = (value || '').trim().split(' ');
    const dpart = parts[0] || '';
    const tpart = parts[1] || '00:00';

    const [dd, MM, yyyy] = dpart.split('/').map(Number);
    const [hh, mm] = tpart.split(':').map(Number);

    const date = new Date(yyyy, (MM || 1) - 1, dd || 1, hh || 0, mm || 0, 0, 0);
    const time = `${String(hh || 0).padStart(2, '0')}:${String(mm || 0).padStart(2, '0')}`;

    return { date, time };
  }

  private formatBrDateTime(date: Date, time: string): string {
    const d = new Date(date);
    const [hh, mm] = (time || '00:00').split(':').map(Number);
    d.setHours(hh || 0, mm || 0, 0, 0);

    const dd = String(d.getDate()).padStart(2, '0');
    const MM = String(d.getMonth() + 1).padStart(2, '0');
    const yyyy = d.getFullYear();
    const HH = String(d.getHours()).padStart(2, '0');
    const min = String(d.getMinutes()).padStart(2, '0');

    return `${dd}/${MM}/${yyyy} ${HH}:${min}`;
  }

  onTimeInput() {
    const ctrl = this.form.controls.dueDateTime;
    const raw = (ctrl.value || '').toString();

    // só números, máximo 4 (HHmm)
    const digits = raw.replace(/\D/g, '').slice(0, 4);

    let formatted = digits;
    if (digits.length >= 3) {
      formatted = `${digits.slice(0, 2)}:${digits.slice(2)}`;
    }

    ctrl.setValue(formatted, { emitEvent: false });
  }

  get isEditMode(): boolean {
    return !!this.data?.id;
  }

  save() {
    if (this.form.invalid) return;

    const date = this.form.value.dueDateDate!;
    const time = this.form.value.dueDateTime || '00:00';

    const dueDate = this.formatBrDateTime(date, time);

    const result: any = {
      title: this.form.value.title,
      description: this.form.value.description,
      dueDate,
      priority: this.form.value.priority
    };

    if (this.isEditMode) {
      result.status = this.form.value.status; // ✅ só em edição
    }

    this.ref.close(result);
  }

  cancel() {
    this.ref.close(null);
  }
}
