package com.acme.salary_management.congfig;

import com.acme.salary_management.entity.Employee;
import com.acme.salary_management.repository.EmployeeRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class EmployeeDataSeeder {

    private static final int TOTAL_EMPLOYEES = 10_000;
    private static final int BATCH_SIZE = 500;

    private static final String[] FIRST_NAMES = {
        "Rahul", "Priya", "Amit", "Sneha", "Rohan",
        "Neha", "Vikas", "Pooja", "Arjun", "Ananya",
        "Karan", "Kavita", "Aditya", "Meera", "Sanjay",
        "Isha", "Nikhil", "Divya", "Akash", "Riya"
    };

    private static final String[] LAST_NAMES = {
        "Sharma", "Patil", "Ghadage", "Verma", "Kulkarni",
        "Deshmukh", "Singh", "Joshi", "Reddy", "Gupta",
        "Pawar", "Shinde", "Kumar", "Jadhav", "Mehta"
    };

    private static final String[] COUNTRIES = {
        "India", "USA", "UK", "Germany", "Canada",
        "Australia", "Singapore", "Japan", "France", "UAE"
    };

    private static final String[] DEPARTMENTS = {
        "Engineering", "Human Resources", "Finance",
        "Sales", "Marketing", "Operations",
        "IT", "Legal", "Research", "Customer Support"
    };

    private static final String[] JOB_TITLES = {
        "Software Engineer", "Design Engineer",
        "Senior Engineer", "Business Analyst",
        "Project Manager", "HR Executive",
        "Data Analyst", "Team Lead",
        "Sales Executive", "Operations Manager"
    };

    @Bean
    CommandLineRunner seedEmployees(EmployeeRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                System.out.println(
                    "Employee seeding skipped: existing records found."
                );
                return;
            }

            System.out.println("Starting employee data seeding...");

            List<Employee> batch = new ArrayList<>(BATCH_SIZE);

            for (int i = 1; i <= TOTAL_EMPLOYEES; i++) {
                Employee employee = new Employee();

                employee.setEmployeeNumber(
                    String.format("EMP%05d", i)
                );

                employee.setFirstName(
                    FIRST_NAMES[(i - 1) % FIRST_NAMES.length]
                );

                employee.setLastName(
                    LAST_NAMES[(i - 1) % LAST_NAMES.length]
                );

                employee.setEmail(
                    String.format("employee%05d@example.com", i)
                );

                employee.setCountry(
                    COUNTRIES[(i - 1) % COUNTRIES.length]
                );

                employee.setDepartment(
                    DEPARTMENTS[(i - 1) % DEPARTMENTS.length]
                );

                employee.setJobTitle(
                    JOB_TITLES[(i - 1) % JOB_TITLES.length]
                );

                double salary = 25_000 + ((i * 137L) % 125_000);

                employee.setSalary(
                    BigDecimal.valueOf(salary)
                );

                employee.setCurrency("INR");

                batch.add(employee);

                if (batch.size() == BATCH_SIZE) {
                    repository.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                repository.saveAll(batch);
            }

            System.out.println(
                "Employee seeding completed. Total employees: "
                + repository.count()
            );
        };
    }
}