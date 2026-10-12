
package com.acme.salary_management.repository;

import com.acme.salary_management.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long>,
                JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    boolean existsByEmployeeNumber(String employeeNumber);

    boolean existsByEmail(String email);

    // Counts distinct countries across the entire database.
    @Query("SELECT COUNT(DISTINCT e.country) FROM Employee e")
    long countDistinctCountries();

    // Counts distinct departments across the entire database.
    @Query("SELECT COUNT(DISTINCT e.department) FROM Employee e")
    long countDistinctDepartments();
}
