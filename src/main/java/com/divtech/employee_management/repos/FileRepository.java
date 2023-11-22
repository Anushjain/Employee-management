package com.divtech.employee_management.repos;

import com.divtech.employee_management.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FileRepository extends JpaRepository<File, Long> {
}
