
package com.acme.salary_management.controller;

import com.acme.salary_management.entity.Employee;
import com.acme.salary_management.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:4200")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // GET /api/employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // GET /api/employees/page?page=0&size=10&sortBy=firstName&direction=ASC
    @GetMapping("/page")
    public ResponseEntity<Page<Employee>> getEmployeesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String country,
            @RequestParam(defaultValue = "") String department
    ) {
        return ResponseEntity.ok(
                employeeService.getEmployeesPaginated(
                        page, size, sortBy, direction,
                        search, country, department));
    }

    // GET /api/employees/analytics
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Long>> getAnalytics() {
        Map<String, Long> analytics = Map.of(
                "countryCount", employeeService.getCountryCount(),
                "departmentCount", employeeService.getDepartmentCount()
        );

        return ResponseEntity.ok(analytics);
    }

    // GET /api/employees/1
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                employeeService.getEmployeeById(id));
    }

    // POST /api/employees
    @PostMapping
    public ResponseEntity<Employee> createEmployee(
            @Valid @RequestBody Employee employee) {
        Employee created = employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/employees/1
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody Employee employee) {
        return ResponseEntity.ok(
                employeeService.updateEmployee(id, employee));
    }

    // DELETE /api/employees/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
