
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Employee {
  id?: number;
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  country: string;
  department: string;
  jobTitle: string;
  salary: number;
  currency: string;
}

export interface EmployeePage {
  content: Employee[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface EmployeeAnalytics {
  countryCount: number;
  departmentCount: number;
}

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/employees';

  // Get all employees.
  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.apiUrl);
  }

  // Get a single employee.
  getEmployee(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }

  // Create an employee.
  createEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, employee);
  }

  // Update an employee.
  updateEmployee(
    id: number,
    employee: Employee
  ): Observable<Employee> {
    return this.http.put<Employee>(
      `${this.apiUrl}/${id}`,
      employee
    );
  }

  // Delete an employee.
  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Server-side pagination, sorting, search, and filters.
  getEmployeesPage(
    page: number,
    size: number,
    sortBy = 'firstName',
    direction: 'ASC' | 'DESC' = 'ASC',
    search = '',
    country = '',
    department = ''
  ): Observable<EmployeePage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('direction', direction);

    if (search.trim()) {
      params = params.set('search', search.trim());
    }

    if (country.trim()) {
      params = params.set('country', country.trim());
    }

    if (department.trim()) {
      params = params.set('department', department.trim());
    }

    return this.http.get<EmployeePage>(
      `${this.apiUrl}/page`,
      { params }
    );
  }

  // Unique country and department counts across the whole database.
  getEmployeeAnalytics(): Observable<EmployeeAnalytics> {
    return this.http.get<EmployeeAnalytics>(
      `${this.apiUrl}/analytics`
    );
  }
}
