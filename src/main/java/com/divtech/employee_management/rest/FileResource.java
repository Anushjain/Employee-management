package com.divtech.employee_management.rest;

import com.divtech.employee_management.dto.FileDTO;
import com.divtech.employee_management.dto.HeaderInputStreamResponse;
import com.divtech.employee_management.dto.ProgressTracker;
import com.divtech.employee_management.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/files", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class FileResource {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> uploadFile(@RequestBody final MultipartFile file,
                                           @RequestHeader(name="Authorization", required = false) final String token) {
        return new ResponseEntity<>(fileService.uploadFile(file, token), HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<List<FileDTO>> getAllFiles() {
        return ResponseEntity.ok(fileService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable final Long id,
                                                       @RequestHeader(name="Authorization", required = false) final String token) {

        HeaderInputStreamResponse headerInputStreamResponse = fileService.get(id,token.split(" ")[1].trim());
        return new ResponseEntity<>(headerInputStreamResponse.getInputStreamResource(),
                headerInputStreamResponse.getHttpHeaders(), HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteFile(@PathVariable final Long id,
                                           @RequestHeader(name="Authorization", required = false) final String token) {
        fileService.delete(id, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/progress/{fileId}")
    public ResponseEntity<String> checkProgress(@PathVariable Long fileId) {
        ProgressTracker tracker = fileService.getProgressTracker(fileId);
        if (tracker == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No progress tracker found for file ID: " + fileId);
        }
        int progress = tracker.getProgressPercentage();
        return ResponseEntity.ok("Progress for file ID " + fileId + ": " + progress + "%");
    }
}
