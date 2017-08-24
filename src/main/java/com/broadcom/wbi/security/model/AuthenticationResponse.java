package com.broadcom.wbi.security.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationResponse {

    private String token;

    public AuthenticationResponse() {
        this.token = token;
    }

    public AuthenticationResponse(String token) {
        this.token = token;
    }

}