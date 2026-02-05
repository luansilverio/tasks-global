import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Task, TaskStatus } from './task.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private baseUrl = 'http://localhost:8080/api/tarefas';

  constructor(private http: HttpClient) {}

  list(status?: TaskStatus): Observable<Task[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http.get<Task[]>(this.baseUrl, { params });
  }

  create(payload: { title: string; description?: string; dueDate: string; priority: string }): Observable<Task> {
    return this.http.post<Task>(this.baseUrl, payload);
  }

  update(id: string, payload: any): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
