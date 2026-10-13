
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import {
  Employee,
  EmployeeAnalytics,
  EmployeePage,
  EmployeeService
} from './services/employee.service';

interface BreakdownEntry {
  label: string;
  count: number;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  private readonly employeeService = inject(EmployeeService);

  title = 'Salary Management System';

  employees: Employee[] = [];
  pageData: EmployeePage | null = null;

  analytics: EmployeeAnalytics = {
    countryCount: 0,
    departmentCount: 0
  };

  countryBreakdown: BreakdownEntry[] = [];
  departmentBreakdown: BreakdownEntry[] = [];
  selectedBreakdown: 'countries' | 'departments' | null = null;

  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  searchTerm = '';
  currentPage = 0;
  pageSize = 10;
  sortBy = 'firstName';
  sortDirection: 'ASC' | 'DESC' = 'ASC';

  showModal = false;
  isEditMode = false;
  selectedEmployeeId: number | null = null;

  formEmployee: Employee = this.createEmptyEmployee();

  ngOnInit(): void {
    this.loadEmployeesPage();
    this.loadAnalytics();
    this.loadBreakdowns();
  }

  get totalEmployees(): number {
    return this.pageData?.totalElements ?? 0;
  }

  get totalPages(): number {
    return this.pageData?.totalPages ?? 0;
  }

  get firstRecordNumber(): number {
    if (this.totalEmployees === 0) {
      return 0;
    }
    return this.currentPage * this.pageSize + 1;
  }

  get lastRecordNumber(): number {
    return Math.min(
      (this.currentPage + 1) * this.pageSize,
      this.totalEmployees
    );
  }

  getCountryCount(): number {
    return this.analytics.countryCount;
  }

  getDepartmentCount(): number {
    return this.analytics.departmentCount;
  }

  getEmployeeName(employee: Employee): string {
    return `${employee.firstName} ${employee.lastName}`.trim();
  }

  loadEmployeesPage(): void {
    this.loading = true;
    this.errorMessage = '';

    this.employeeService.getEmployeesPage(
      this.currentPage,
      this.pageSize,
      this.sortBy,
      this.sortDirection,
      this.searchTerm
    ).subscribe({
      next: (response) => {
        this.pageData = response;
        this.employees = response.content;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.errorMessage = this.getErrorMessage(
          error,
          'Unable to load employees.'
        );
      }
    });
  }

  loadAnalytics(): void {
    this.employeeService.getEmployeeAnalytics().subscribe({
      next: (data) => {
        this.analytics = data;
      },
      error: (error: HttpErrorResponse) => {
        console.error('Unable to load analytics:', error);
      }
    });
  }

  loadBreakdowns(): void {
    this.employeeService.getEmployees().subscribe({
      next: (employees) => {
        this.countryBreakdown = this.buildBreakdown(employees, 'country');
        this.departmentBreakdown = this.buildBreakdown(employees, 'department');
      },
      error: (error: HttpErrorResponse) => {
        console.error('Unable to load employee breakdown:', error);
      }
    });
  }

  toggleBreakdown(type: 'countries' | 'departments'): void {
    this.selectedBreakdown = this.selectedBreakdown === type ? null : type;
  }

  getActiveBreakdown(): BreakdownEntry[] {
    if (this.selectedBreakdown === 'countries') {
      return this.countryBreakdown;
    }

    if (this.selectedBreakdown === 'departments') {
      return this.departmentBreakdown;
    }

    return [];
  }

  private buildBreakdown(
    employees: Employee[],
    key: 'country' | 'department'
  ): BreakdownEntry[] {
    const counts = new Map<string, number>();

    employees.forEach((employee) => {
      const value = String(employee[key] ?? '').trim();

      if (!value) {
        return;
      }

      counts.set(value, (counts.get(value) ?? 0) + 1);
    });

    return Array.from(counts.entries())
      .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
      .map(([label, count]) => ({ label, count }));
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadEmployeesPage();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.currentPage = 0;
    this.loadEmployeesPage();
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages) {
      return;
    }

    this.currentPage = page;
    this.loadEmployeesPage();
  }

  previousPage(): void {
    this.changePage(this.currentPage - 1);
  }

  nextPage(): void {
    this.changePage(this.currentPage + 1);
  }

  changePageSize(size: number | string): void {
    const parsedSize = Number(size);

    if (!Number.isFinite(parsedSize) ||
        parsedSize < 1 ||
        parsedSize > 100) {
      return;
    }

    this.pageSize = parsedSize;
    this.currentPage = 0;
    this.loadEmployeesPage();
  }

  sortEmployees(field: string): void {
    if (this.sortBy === field) {
      this.sortDirection =
        this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortBy = field;
      this.sortDirection = 'ASC';
    }

    this.currentPage = 0;
    this.loadEmployeesPage();
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.selectedEmployeeId = null;
    this.formEmployee = this.createEmptyEmployee();
    this.errorMessage = '';
    this.showModal = true;
  }

  openEditModal(employee: Employee): void {
    if (employee.id == null) {
      this.errorMessage = 'Employee ID is missing.';
      return;
    }

    this.isEditMode = true;
    this.selectedEmployeeId = employee.id;
    this.formEmployee = { ...employee };
    this.errorMessage = '';
    this.showModal = true;
  }

  closeModal(): void {
    if (this.saving) {
      return;
    }

    this.showModal = false;
    this.selectedEmployeeId = null;
    this.formEmployee = this.createEmptyEmployee();
  }

  saveEmployee(): void {
    if (this.saving) {
      return;
    }

    if (!this.isFormValid()) {
      this.errorMessage =
        'Please complete all required fields with valid values.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const employee: Employee = {
      ...this.formEmployee,
      employeeNumber: this.formEmployee.employeeNumber.trim(),
      firstName: this.formEmployee.firstName.trim(),
      lastName: this.formEmployee.lastName.trim(),
      email: this.formEmployee.email.trim(),
      country: this.formEmployee.country.trim(),
      department: this.formEmployee.department.trim(),
      jobTitle: this.formEmployee.jobTitle.trim(),
      currency: this.formEmployee.currency.trim(),
      salary: Number(this.formEmployee.salary)
    };

    const request = this.isEditMode &&
      this.selectedEmployeeId !== null
      ? this.employeeService.updateEmployee(
          this.selectedEmployeeId,
          employee
        )
      : this.employeeService.createEmployee(employee);

    request.subscribe({
      next: () => {
        this.saving = false;
        this.showModal = false;
        this.successMessage = this.isEditMode
          ? 'Employee updated successfully.'
          : 'Employee created successfully.';

        this.selectedEmployeeId = null;
        this.currentPage = 0;
        this.loadEmployeesPage();
        this.loadAnalytics();
        this.loadBreakdowns();
      },
      error: (error: HttpErrorResponse) => {
        this.saving = false;
        this.errorMessage = this.getErrorMessage(
          error,
          'Unable to save employee.'
        );
      }
    });
  }

  deleteEmployee(employee: Employee): void {
    if (employee.id == null) {
      this.errorMessage = 'Employee ID is missing.';
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to delete ${this.getEmployeeName(employee)}?`
    );

    if (!confirmed) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.employeeService.deleteEmployee(employee.id).subscribe({
      next: () => {
        this.successMessage = 'Employee deleted successfully.';

        if (this.employees.length === 1 && this.currentPage > 0) {
          this.currentPage--;
        }

        this.loadEmployeesPage();
        this.loadAnalytics();
        this.loadBreakdowns();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.getErrorMessage(
          error,
          'Unable to delete employee.'
        );
      }
    });
  }

  private isFormValid(): boolean {
    const employee = this.formEmployee;

    return Boolean(
      employee.employeeNumber?.trim() &&
      employee.firstName?.trim() &&
      employee.lastName?.trim() &&
      employee.email?.trim() &&
      employee.country?.trim() &&
      employee.department?.trim() &&
      employee.jobTitle?.trim() &&
      employee.currency?.trim() &&
      Number.isFinite(Number(employee.salary)) &&
      Number(employee.salary) >= 0
    );
  }

  private createEmptyEmployee(): Employee {
    return {
      employeeNumber: '',
      firstName: '',
      lastName: '',
      email: '',
      country: '',
      department: '',
      jobTitle: '',
      salary: 0,
      currency: 'INR'
    };
  }

  private getErrorMessage(
    error: HttpErrorResponse,
    fallback: string
  ): string {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }

    if (error.error?.message) {
      return error.error.message;
    }

    if (error.status === 0) {
      return 'Cannot connect to the backend. Check that Spring Boot is running on port 8080.';
    }

    if (error.status === 400) {
      return 'Invalid employee data. Please check the fields and try again.';
    }

    if (error.status === 404) {
      return 'The requested employee was not found.';
    }

    if (error.status === 409) {
      return 'An employee with this employee number or email already exists.';
    }

    return fallback;
  }
}
