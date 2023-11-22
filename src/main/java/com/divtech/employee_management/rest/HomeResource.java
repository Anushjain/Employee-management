package com.divtech.employee_management.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class HomeResource {

    @GetMapping("/")
    @ResponseBody
    public String index() {
        return "Hello World!";
    }

}
