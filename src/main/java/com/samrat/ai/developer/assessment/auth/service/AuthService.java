package com.samrat.ai.developer.assessment.auth.service;

import com.samrat.ai.developer.assessment.auth.dto.LoginRequest;
import com.samrat.ai.developer.assessment.auth.dto.LoginResponse;
import com.samrat.ai.developer.assessment.auth.dto.RegisterRequest;
import com.samrat.ai.developer.assessment.auth.dto.UserResponse;
import com.samrat.ai.developer.assessment.exception.EmailAlreadyExistsException;
import com.samrat.ai.developer.assessment.security.JwtService;
import com.samrat.ai.developer.assessment.user.model.Role;
import com.samrat.ai.developer.assessment.user.model.User;
import com.samrat.ai.developer.assessment.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Fix: Normalize email FIRST before checking existence
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        response.setCreatedAt(savedUser.getCreatedAt());

        return response;
    }

    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        System.out.println("--> Login attempt received for: " + normalizedEmail);

        // 1. Fetch and debug BEFORE running authenticationManager
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);

        if (user == null) {
            System.out.println("--> DEBUG RESULT: User NOT FOUND in DB for email: " + normalizedEmail);
        } else {
            System.out.println("--> DEBUG RESULT: Stored DB Hash: " + user.getPassword());
            System.out.println("--> DEBUG RESULT: Raw Input Password: " + request.getPassword());
            boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
            System.out.println("--> DEBUG RESULT: Does BCrypt Match?: " + matches);
        }

        // 2. Now run Spring Security Authentication
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // 3. Fetch user again (guaranteed to exist if authenticate passed)
        User authenticatedUser = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(normalizedEmail);
        String jwtToken = jwtService.generateToken(userDetails);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(authenticatedUser.getId());
        userResponse.setName(authenticatedUser.getName());
        userResponse.setEmail(authenticatedUser.getEmail());
        userResponse.setRole(authenticatedUser.getRole());
        userResponse.setCreatedAt(authenticatedUser.getCreatedAt());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setTokenType("Bearer");
        loginResponse.setExpiresIn(jwtService.getExpirationTime());
        loginResponse.setUser(userResponse);

        return loginResponse;
    }
}