package com.divtech.employee_management.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;


@Getter
@Setter
public class HeaderInputStreamResponse {
    private HttpHeaders httpHeaders;
    private InputStreamResource inputStreamResource;
}
