package com.kenpb.app.serviceImplementation;

import com.kenpb.app.models.User;
import com.kenpb.app.repositories.UserRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetails implements UserDetailsService {

	private UserRepository userRepository;

	public CustomUserDetails(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> findUser = userRepository.findByEmail(username);
		if(!findUser.isPresent()) {
			throw new UsernameNotFoundException("User not found with email " + username);
		}
		User user = findUser.get();

		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

		return new org.springframework.security.core.userdetails.User(
				user.getEmail(),
				user.getPassword(),
				authorities
		);
	}
}
