package com.madeeasy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DepartmentController {
    @GetMapping(value = "/departments")
    public String welcome(){
        return "welcome from department service";
    }
}
