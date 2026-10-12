
package com.acme.salary_management.service;

import com.acme.salary_management.entity.Employee;
import com.acme.salary_management.repository.EmployeeRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "employeeNumber", "firstName", "lastName",
            "email", "country", "department", "jobTitle",
            "salary", "currency"
    );

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // Get all employees.
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Get one employee by ID.
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found with ID: " + id));
    }

    // Create an employee.
    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employeeRepository.existsByEmployeeNumber(
                employee.getEmployeeNumber())) {
            throw new IllegalArgumentException(
                    "Employee number already exists");
        }

        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already exists");
        }

        return employeeRepository.save(employee);
    }

    // Update an employee while preserving the original ID.
    @Transactional
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee existing = getEmployeeById(id);

        employeeRepository.findByEmployeeNumber(
                updatedEmployee.getEmployeeNumber()
        ).ifPresent(found -> {
            if (!found.getId().equals(id)) {
                throw new IllegalArgumentException(
                        "Employee number already exists");
            }
        });

        if (employeeRepository.existsByEmail(updatedEmployee.getEmail())) {
            Employee emailOwner = employeeRepository.findAll().stream()
                    .filter(e -> e.getEmail().equalsIgnoreCase(
                            updatedEmployee.getEmail()))
                    .findFirst()
                    .orElse(null);

            if (emailOwner != null && !emailOwner.getId().equals(id)) {
                throw new IllegalArgumentException("Email already exists");
            }
        }

        existing.setEmployeeNumber(updatedEmployee.getEmployeeNumber());
        existing.setFirstName(updatedEmployee.getFirstName());
        existing.setLastName(updatedEmployee.getLastName());
        existing.setEmail(updatedEmployee.getEmail());
        existing.setCountry(updatedEmployee.getCountry());
        existing.setDepartment(updatedEmployee.getDepartment());
        existing.setJobTitle(updatedEmployee.getJobTitle());
        existing.setSalary(updatedEmployee.getSalary());
        existing.setCurrency(updatedEmployee.getCurrency());

        return employeeRepository.save(existing);
    }

    // Delete an employee.
    @Transactional
    public void deleteEmployee(Long id) {
        Employee existing = getEmployeeById(id);
        employeeRepository.delete(existing);
    }

    // Basic pagination and sorting.
    @Transactional(readOnly = true)
    public Page<Employee> getEmployeesPaginated(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        return getEmployeesPaginated(
                page, size, sortBy, direction, "", "", "");
    }

    // Pagination, sorting, global search, and exact country/department filters.
    @Transactional(readOnly = true)
    public Page<Employee> getEmployeesPaginated(
            int page,
            int size,
            String sortBy,
            String direction,
            String search,
            String country,
            String department
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field");
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(
                    direction == null ? "ASC" : direction);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Direction must be ASC or DESC");
        }

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(sortDirection, sortBy));

        Specification<Employee> specification =
                (root, query, criteriaBuilder) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    if (search != null && !search.isBlank()) {
                        String term = "%" + search.trim().toLowerCase() + "%";

                        predicates.add(criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("employeeNumber")), term),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("firstName")), term),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("lastName")), term),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("email")), term),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("country")), term),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("department")), term),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(
                                                root.get("jobTitle")), term)
                        ));
                    }

                    if (country != null && !country.isBlank()) {
                        predicates.add(criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("country")),
                                country.trim().toLowerCase()));
                    }

                    if (department != null && !department.isBlank()) {
                        predicates.add(criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("department")),
                                department.trim().toLowerCase()));
                    }

                    return criteriaBuilder.and(
                            predicates.toArray(new Predicate[0]));
                };

        return employeeRepository.findAll(specification, pageable);
    }

    // Unique country count across the entire database.
    @Transactional(readOnly = true)
    public long getCountryCount() {
        return employeeRepository.countDistinctCountries();
    }

    // Unique department count across the entire database.
    @Transactional(readOnly = true)
    public long getDepartmentCount() {
        return employeeRepository.countDistinctDepartments();
    }
}
