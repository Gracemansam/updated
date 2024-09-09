package com.kenpb.app.controllers;

import com.kenpb.app.dtos.AuthResponse;
import com.kenpb.app.dtos.LoginRequest;
import com.kenpb.app.dtos.UserDto;
import com.kenpb.app.serviceImplementation.UserServiceImplementation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final UserServiceImplementation userServiceImplementation;

    @Autowired
    public AuthController(UserServiceImplementation userServiceImplementation) {
        this.userServiceImplementation = userServiceImplementation;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserDto userDto, HttpServletRequest request){
            ResponseEntity<AuthResponse> responseEntity = userServiceImplementation.createUserHandler(userDto,request);
            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);

    }


    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            ResponseEntity<AuthResponse> responseEntity = userServiceImplementation.signIn(loginRequest);
            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
        } catch (Exception e) {

            return new ResponseEntity<>(new AuthResponse(e.getMessage() , false), HttpStatus.UNAUTHORIZED);
        }
    }




}
