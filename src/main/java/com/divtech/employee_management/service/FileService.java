package com.divtech.employee_management.service;

import com.divtech.employee_management.dto.FileDTO;
import com.divtech.employee_management.dto.HeaderInputStreamResponse;
import com.divtech.employee_management.dto.ProgressTracker;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    Long uploadFile(final MultipartFile file, final String token);
    List<FileDTO> getAll();
    HeaderInputStreamResponse get(final Long id, final String token);
    void delete(final Long id, final String token);
    ProgressTracker getProgressTracker(Long fileId);
}
