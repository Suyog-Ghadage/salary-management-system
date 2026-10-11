# Salary Management System — Requirements

## 1. Goal
Build a web-based salary management application for HR managers
to manage and analyze salary data for 10,000 employees across
multiple countries.

## 2. Primary User
HR Manager responsible for maintaining employee salary records
and understanding organizational compensation.

## 3. Scope and Features
- Dashboard with total employees and salary statistics.
- Employee list with search, sorting, filtering, and pagination.
- Create, view, edit, and delete employee records.
- Store employee ID, name, country, department, job title,
  salary amount, and salary currency.
- Analytics by country and department.
- REST APIs with validation and consistent error handling.
- Relational database with repeatable seed data for 10,000 employees.
- Automated backend and frontend tests.
- Responsive UI, deployment instructions, and demo video.

## 4. Salary and Currency Rules
- Salary must be a non-negative decimal value.
- Currency must be stored explicitly for every salary.
- Do not add salaries in different currencies without conversion.
- Exchange-rate integration is outside the initial scope.

## 5. Non-Functional Requirements
- Support efficient searching and paginated access to 10,000 records.
- Protect against invalid input and unauthorized data changes.
- Keep code modular, readable, and maintainable.
- Use deterministic seed data and reliable automated tests.
- Document setup, architecture, trade-offs, and AI assistance.

## 6. Deliberately Out of Scope
- Payroll disbursement and bank integration.
- Tax calculation and statutory deductions.
- Attendance, leave, and performance management.
- Live exchange rates and currency conversion.
- Complex enterprise SSO integration.

These features are excluded to keep the assessment focused on
employee salary management and organizational salary analytics.

## 7. Acceptance Criteria
- The application runs using documented instructions.
- The database can be seeded with 10,000 employees.
- HR can search, filter, paginate, and manage employee records.
- Dashboard statistics match the stored data.
- Core business rules are covered by automated tests.
- The repository includes incremental commits and a demo link.
