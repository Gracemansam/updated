package com.kenpb.app.service;

import com.kenpb.app.dtos.AuthResponse;
import com.kenpb.app.dtos.LoginRequest;
import com.kenpb.app.dtos.UserDto;
import com.kenpb.app.models.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {





    ResponseEntity<AuthResponse> createUserHandler(UserDto userDto, HttpServletRequest request);

    ResponseEntity<AuthResponse> signIn(LoginRequest loginRequest);

    Authentication authenticate(String username, String password);

    public User findUserById(Long userId) ;
	
	public User findUserProfileByJwt(String jwt) ;
	
	public List<User> findAllUsers();

    void saveUserVerificationToken(User theUser, String token);

    String validateToken(String theToken);

}
