package com.divtech.employee_management.service;

import com.divtech.employee_management.constant.Role;
import com.divtech.employee_management.domain.Employee;
import com.divtech.employee_management.domain.File;
import com.divtech.employee_management.domain.User;
import com.divtech.employee_management.dto.FileDTO;
import com.divtech.employee_management.dto.HeaderInputStreamResponse;
import com.divtech.employee_management.repos.EmployeeRepository;
import com.divtech.employee_management.repos.FileRepository;
import com.divtech.employee_management.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FileServiceImplTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    private Path sourceFilePath;
    private Path targetFilePath;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    public byte[] loadFileContent() {
        ClassPathResource resource = new ClassPathResource("/upload/test.xlsx");
        try {
            return Files.readAllBytes(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void uploadFile_WithValidFile_ShouldUploadSuccessfully() {
        File file = new File();
        file.setId(1L);
        file.setFileName("test.xlsx");
        try (MockedStatic<JwtUtil> mockedJwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwtUtil.when(() -> JwtUtil.getRole(anyString())).thenReturn(Role.ADMIN);
            MultipartFile mockFile = new MockMultipartFile("file", "test.xlsx",
                    MediaType.MULTIPART_FORM_DATA_VALUE, loadFileContent());
            when(fileRepository.save(any())).thenReturn(file);
            when(employeeRepository.saveAll(anyCollection())).thenReturn(List.of(new Employee()));
            Long fileId = fileService.uploadFile(mockFile, "token");
            assertNotNull(fileId);
        }
    }

    @Test
    void uploadFile_WithEmptyFile_ShouldThrowException() {
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        try (MockedStatic<JwtUtil> mockedJwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwtUtil.when(() -> JwtUtil.getRole(anyString())).thenReturn(Role.ADMIN);
            assertThrows(ResponseStatusException.class, () -> {
                fileService.uploadFile(emptyFile, "token");
            });
        }
    }

    @Test
    void getAll_ShouldReturnFileList() {
        List<com.divtech.employee_management.domain.File> files = List.of(new com.divtech.employee_management.domain.File());
        when(fileRepository.findAll()).thenReturn(files);

        List<FileDTO> fileDTOs = fileService.getAll();

        assertFalse(fileDTOs.isEmpty());
    }

    @Test
    void getFile_ValidId_ShouldReturnFile() {
        com.divtech.employee_management.domain.File file = new com.divtech.employee_management.domain.File();
        file.setFileName("test.xlsx");
        try (MockedStatic<JwtUtil> mockedJwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwtUtil.when(() -> JwtUtil.getRole(anyString())).thenReturn(Role.ADMIN);
            when(jwtUtil.getUserDetailsFromToken(anyString())).thenReturn(new User());
            when(fileRepository.findById(anyLong())).thenReturn(Optional.of(file));

            HeaderInputStreamResponse response = fileService.get(1L, "token");

            assertNotNull(response);
        }
    }

    @Test
    void delete_WithValidId_ShouldDeleteFile() {
        sourceFilePath = Paths.get("C:/Users/Hp/Desktop/employee-managment/src/main/resources/upload/test.xlsx");
        targetFilePath = sourceFilePath.resolveSibling("testdelete.xlsx");
        try {
            Files.createDirectories(sourceFilePath.getParent());
            Files.copy(sourceFilePath, targetFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (MockedStatic<JwtUtil> mockedJwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwtUtil.when(() -> JwtUtil.getRole(anyString())).thenReturn(Role.ADMIN);
            com.divtech.employee_management.domain.File file = new com.divtech.employee_management.domain.File();
            file.setFileName("testdelete.xlsx");
            when(fileRepository.findById(anyLong())).thenReturn(Optional.of(file));

            fileService.delete(1L, "token");

            verify(fileRepository, times(1)).deleteById(1L);
        }
    }

    @Test
    void delete_WithNonAdminRole_ShouldThrowException() {
        try (MockedStatic<JwtUtil> mockedJwtUtil = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwtUtil.when(() -> JwtUtil.getRole(anyString())).thenReturn(Role.USER);
            assertThrows(ResponseStatusException.class, () -> {
                fileService.delete(1L, "token");
            });
        }
    }
}
