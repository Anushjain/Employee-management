package com.divtech.employee_management.service;

import com.divtech.employee_management.domain.Employee;
import com.divtech.employee_management.domain.User;
import com.divtech.employee_management.dto.FileDTO;
import com.divtech.employee_management.dto.HeaderInputStreamResponse;
import com.divtech.employee_management.dto.ProgressTracker;
import com.divtech.employee_management.constant.Role;
import com.divtech.employee_management.repos.EmployeeRepository;
import com.divtech.employee_management.repos.FileRepository;
import com.divtech.employee_management.util.JwtUtil;
import com.divtech.employee_management.util.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final EmployeeRepository employeeRepository;
    private final FileRepository fileRepository;
    private final JwtUtil jwtUtil;
    private static final String DIRECTORY = "C:/Users/Hp/Desktop/employee-managment/src/main/resources/upload/";
    private final ConcurrentMap<Long, ProgressTracker> progressTrackers = new ConcurrentHashMap<>();


    @Override
    public Long uploadFile(final MultipartFile file, final String token) {
        if (!JwtUtil.getRole(token).equals(Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only admin can access");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        String fileName = file.getOriginalFilename();
        File directory = new File(DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String filePath = DIRECTORY + fileName;
        com.divtech.employee_management.domain.File saveFile;
        try {
            com.divtech.employee_management.domain.File requestedFile = new com.divtech.employee_management.domain.File();
            requestedFile.setFileName(fileName);
            requestedFile.setLastAccess(OffsetDateTime.now());
            saveFile = fileRepository.save(requestedFile);
            ProgressTracker tracker = new ProgressTracker();
            progressTrackers.put(saveFile.getId(), tracker);
            processExcelFile(file, saveFile, tracker);
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return saveFile.getId();
    }

    @Override
    public List<FileDTO> getAll() {
        return fileRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public HeaderInputStreamResponse get(Long id, String token) {
        com.divtech.employee_management.domain.File file = fileRepository.findById(id).orElseThrow();
        User user = jwtUtil.getUserDetailsFromToken(token);
        log.info("File is accessed by USER_INFO: "  + "firstName: " + user.getUsername()
                + ", userEmail: " + user.getEmail());
        String filePath = DIRECTORY + file.getFileName();
        File fileResponse = new File(filePath);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileResponse);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileResponse.getName());
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        updateAccessTime(file);
        HeaderInputStreamResponse response = new HeaderInputStreamResponse();
        response.setHttpHeaders(headers);
        response.setInputStreamResource(inputStreamResource);
        return response;
    }

    @Override
    public ProgressTracker getProgressTracker(Long fileId) {
        return progressTrackers.get(fileId);
    }

    @Override
    public void delete(final Long id, final String token) {
        if (!JwtUtil.getRole(token).equals(Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only admin can access");
        }
        com.divtech.employee_management.domain.File file = fileRepository.findById(id)
               .orElseThrow(NotFoundException::new);
        fileRepository.deleteById(id);
        deleteFileFromSystem(file.getFileName());
    }

    private void deleteFileFromSystem(String fileName) {
        Path filePath = Paths.get(DIRECTORY + fileName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from system", e);
        }
    }

    private void updateAccessTime(final com.divtech.employee_management.domain.File file) {
        file.setLastAccess(OffsetDateTime.now());
        fileRepository.save(file);
    }
    private FileDTO mapToDTO(final com.divtech.employee_management.domain.File file) {
        FileDTO fileDTO = new FileDTO();
        fileDTO.setFileName(file.getFileName());
        fileDTO.setLastAccess(file.getLastAccess());
        fileDTO.setId(file.getId());
        return fileDTO;
    }

    private void processExcelFile(final MultipartFile file,
                                  final com.divtech.employee_management.domain.File savedFile,
                                  final ProgressTracker tracker) throws IOException {
        byte[] byteArr = file.getBytes(); // Get bytes from MultipartFile
        Workbook workbook;
        try (InputStream is = new ByteArrayInputStream(byteArr)) {
            workbook = new XSSFWorkbook(is); // Now use ByteArrayInputStream
        }
        Sheet sheet = workbook.getSheetAt(0);

        // Set the total rows in the tracker
        tracker.setTotalRows(sheet.getPhysicalNumberOfRows() - 1);

        List<Employee> employees = new ArrayList<>();
        for (Row row : sheet) {
            // Skipping the header row
            if (row.getRowNum() == 0) {
                continue;
            }
            Employee employee = new Employee();
            employee.setFile(savedFile);
            employee.setEeid(row.getCell(0).getStringCellValue());
            employee.setFullName(row.getCell(1).getStringCellValue());
            employee.setJobTittle(row.getCell(2).getStringCellValue());
            employee.setDepartment(row.getCell(3).getStringCellValue());
            employee.setBusinessUnit(row.getCell(4).getStringCellValue());
            employee.setGender(row.getCell(5).getStringCellValue());
            employee.setEthnicity(row.getCell(6).getStringCellValue());

            if (row.getCell(7) != null) {
                employee.setAge((int) row.getCell(7).getNumericCellValue());
            }
            employee.setCountry(row.getCell(8).getStringCellValue());
            employee.setCity(row.getCell(9).getStringCellValue());
            employees.add(employee);
            tracker.incrementProcessedRows();
            if (employees.size() >= 100) {
                employeeRepository.saveAll(employees);
                employees.clear();
            }
        }
        if (!employees.isEmpty()) {
            employeeRepository.saveAll(employees);
        }
        workbook.close();
    }
}
