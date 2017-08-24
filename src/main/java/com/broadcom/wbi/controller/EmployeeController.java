package com.broadcom.wbi.controller;


import com.broadcom.wbi.exception.CustomGenericException;
import com.broadcom.wbi.model.mysql.Employee;
import com.broadcom.wbi.service.jpa.EmployeeService;
import com.broadcom.wbi.service.jpa.RevisionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api/user")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private RevisionService revisionService;

    @RequestMapping(value = {"/getBookmarks"}, method = {RequestMethod.GET})
    public Callable<List> getBookmarks(HttpServletRequest req) {
        return new Callable<List>() {
            public List call() throws JsonProcessingException {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                Employee user = employeeService.findByAccountName(username);
                if (user == null && username.matches(".*\\d+.*")) {
                    username = username.substring(3);
                    try {
                        user = employeeService.findById(Integer.parseInt(username));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if (user == null)
                    throw new CustomGenericException("User not found");

                return revisionService.findByEmployee(user);
            }
        };
    }

}
