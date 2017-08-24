package com.broadcom.wbi.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("protected")
public class ProtectedController {

    @RequestMapping(method = GET)
    @PreAuthorize("hasRole('USERS')")
    public String getDaHoney() {

        // fake class just for testing purpose
        return "hello World";
    }


}