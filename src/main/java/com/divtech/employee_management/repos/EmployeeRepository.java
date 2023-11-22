package com.divtech.employee_management.repos;

import com.divtech.employee_management.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
