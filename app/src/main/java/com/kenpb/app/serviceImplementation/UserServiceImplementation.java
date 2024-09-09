package com.kenpb.app.serviceImplementation;

import com.kenpb.app.config.JwtTokenProvider;
import com.kenpb.app.converter.UserConverter;
import com.kenpb.app.dtos.AuthResponse;
import com.kenpb.app.dtos.LoginRequest;
import com.kenpb.app.dtos.UserDto;
import com.kenpb.app.models.User;
import com.kenpb.app.repositories.UserRepository;
import com.kenpb.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Lazy
public class UserServiceImplementation implements UserService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	private final PasswordEncoder passwordEncoder;

	private final CustomUserDetails customUserDetails;

	private final UserConverter userConverter;




	public UserServiceImplementation(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder,
									 CustomUserDetails customUserDetails, UserConverter userConverter){
		this.userRepository = userRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.passwordEncoder = passwordEncoder;
		this.customUserDetails = customUserDetails;
		this.userConverter = userConverter;

	}



//	@Override
//	public ResponseEntity<AuthResponse> createUserHandler(UserDto userDto, HttpServletRequest request){
//
//		Optional<User> isEmailExist = userRepository.findByEmail(userDto.getEmail());
//
//		if(isEmailExist.isPresent()){
//			throw new RuntimeException("email already exit");
//		}
//
//		User newUser = userConverter.convertDTOtoEntity(userDto);
//		newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
//		newUser.setRole("USER");
//		User savedUser= userRepository.save(newUser);
//
//
//	Authentication authentication = new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword());
//		SecurityContextHolder.getContext().setAuthentication(authentication);
//
//		String token = jwtTokenProvider.generateToken(authentication);
//
//		AuthResponse authResponse= new AuthResponse(token,true);
////		UserDto savedUserResponse = userConverter.convertEntityToDTO(savedUser);
//
//		return new ResponseEntity<>(authResponse,HttpStatus.CREATED);
//
//	}

		@Override
		public ResponseEntity<AuthResponse> createUserHandler(UserDto userDto, HttpServletRequest request) {
			Optional<User> isEmailExist = userRepository.findByEmail(userDto.getEmail());

			if(isEmailExist.isPresent()) {
				throw new RuntimeException("Email already exists");
			}

			User newUser = userConverter.convertDTOtoEntity(userDto);
			newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
			newUser.setRole("USER");
			User savedUser = userRepository.save(newUser);

			Authentication authentication = new UsernamePasswordAuthenticationToken(savedUser.getEmail(), userDto.getPassword());
			SecurityContextHolder.getContext().setAuthentication(authentication);

			String token = jwtTokenProvider.generateToken(authentication);

			AuthResponse authResponse = new AuthResponse(token, true);

			return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
		}

		@Override
		public ResponseEntity<AuthResponse> signIn(LoginRequest loginRequest) {
			try {
				Authentication authentication = authenticate(loginRequest.getUsername(), loginRequest.getPassword());
		SecurityContextHolder.getContext().setAuthentication(authentication);


				SecurityContextHolder.getContext().setAuthentication(authentication);

				String token = jwtTokenProvider.generateToken(authentication);
				AuthResponse authResponse = new AuthResponse(token, true);

				return new ResponseEntity<>(authResponse, HttpStatus.OK);
			} catch (AuthenticationException e) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, false));
			}
		}

//@Override
//	public ResponseEntity<AuthResponse> signIn(LoginRequest loginRequest) {
//		String username = loginRequest.getEmail();
//		String password = loginRequest.getPassword();
//
//		System.out.println(username +" ----- "+password);
//
//		Authentication authentication = authenticate(username, password);
//		SecurityContextHolder.getContext().setAuthentication(authentication);
//
//
//		String token = jwtTokenProvider.generateToken(authentication);
//		AuthResponse authResponse= new AuthResponse();
//
//		authResponse.setStatus(true);
//		authResponse.setJwt(token);
//
//		return new ResponseEntity<>(authResponse,HttpStatus.OK);
//	}

//	@Override
//	public ResponseEntity<AuthResponse> signIn(LoginRequest loginRequest) {
//		try {
//			Authentication authentication = authenticationManager.authenticate(
//					new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
//			);
//
//			SecurityContextHolder.getContext().setAuthentication(authentication);
//			String jwt = jwtTokenProvider.generateToken(authentication);
//
//			AuthResponse authResponse = new AuthResponse(jwt, true);
//			return ResponseEntity.ok(authResponse);
//		} catch (AuthenticationException e) {
//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, false));
//		}
//	}
@Override
public Authentication authenticate(String username, String password) {
		UserDetails userDetails = customUserDetails.loadUserByUsername(username);

		System.out.println("sign in userDetails - "+userDetails);

		if (userDetails == null) {
			System.out.println("sign in userDetails - null " + userDetails);
			throw new BadCredentialsException("Invalid username or password");
		}
		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			System.out.println("sign in userDetails - password not match " + userDetails);
			throw new BadCredentialsException("Invalid username or password");
		}
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}
	


	@Override
	public User findUserById(Long userId) {
		Optional<User> user=userRepository.findById(userId);
		
		if(user.isPresent()){
			return user.get();
		}
		throw new RuntimeException("User no dey here abeg");
	}

	@Override
	public User findUserProfileByJwt(String jwt) {
		System.out.println("user service");
		String email=jwtTokenProvider.getEmailFromJwtToken(jwt);
		
		System.out.println("email"+email);
		
		Optional<User> user=userRepository.findByEmail(email);
		if(!user.isPresent()) {
			throw new RuntimeException("User with jwt no dey");
		}
//		System.out.println("email user"+user.getEmail());
		return user.get();
	}

	@Override
	public List<User> findAllUsers() {
		// TODO Auto-generated method stub
		return userRepository.findAllByOrderByCreatedAtDesc();
	}

	@Override
	public void saveUserVerificationToken(User theUser, String token) {

	}

	@Override
	public String validateToken(String theToken) {
		return null;
	}

}
