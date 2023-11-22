package com.divtech.employee_management.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class FileDTO {

    private Long id;

    @NotNull
    private String fileName;

    private OffsetDateTime lastAccess;
}
