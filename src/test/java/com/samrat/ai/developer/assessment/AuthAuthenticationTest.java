package com.samrat.ai.developer.assessment;

import com.fasterxml.jackson.databind.ObjectMapper;


import com.samrat.ai.developer.assessment.security.JwtService;
import com.samrat.ai.developer.assessment.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

//    @Test
//    void login_Success_ReturnsJwtToken() throws Exception {
//        User user = new User();
//        user.setName("Jane Doe");
//        user.setEmail("jane@example.com");
//        user.setPassword(passwordEncoder.encode("password123"));
//        user.setRole(Role.USER);
//        userRepository.save(user);
//
//        LoginRequest request = new LoginRequest("jane@example.com", "password123");
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token", notNullValue()))
//                .andExpect(jsonPath("$.tokenType", is("Bearer")))
//                .andExpect(jsonPath("$.user.email", is("jane@example.com")));
//    }

//    @Test
//    void login_BadCredentials_Returns401() throws Exception {
//        LoginRequest request = new LoginRequest("nonexistent@example.com", "wrongpass");
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.status", is(401)))
//                .andExpect(jsonPath("$.message", is("Invalid email or password")));
//    }

//    @Test
//    void protectedEndpoint_WithValidToken_Returns200() throws Exception {
//        User user = new User();
//        user.setName("Test User");
//        user.setEmail("test@example.com");
//        user.setPassword(passwordEncoder.encode("password123"));
//        user.setRole(Role.USER);
//        userRepository.save(user);
//
//        String token = jwtService.generateToken(user);
//
//        mockMvc.perform(get("/api/test/protected")
//                        .header("Authorization", "Bearer " + token))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("You are authenticated")))
//                .andExpect(jsonPath("$.user", is("test@example.com")));
//    }

//    @Test
//    void protectedEndpoint_WithoutToken_Returns401() throws Exception {
//        mockMvc.perform(get("/api/test/protected"))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.status", is(401)));
//    }

//    @Test
//    void adminEndpoint_UserRole_Returns403Forbidden() throws Exception {
//        User user = new User();
//        user.setName("Normal User");
//        user.setEmail("normal@example.com");
//        user.setPassword(passwordEncoder.encode("password123"));
//        user.setRole(Role.USER);
//        userRepository.save(user);
//
//        String token = jwtService.generateToken(user);
//
//        mockMvc.perform(get("/api/admin/dashboard")
//                        .header("Authorization", "Bearer " + token))
//                .andExpect(status().isForbidden())
//                .andExpect(jsonPath("$.status", is(403)));
//    }

//    @Test
//    void adminEndpoint_AdminRole_Returns200Ok() throws Exception {
//        User admin = new User();
//        admin.setName("Admin User");
//        admin.setEmail("admin@example.com");
//        admin.setPassword(passwordEncoder.encode("password123"));
//        admin.setRole(Role.ADMIN);
//        userRepository.save(admin);
//
//        String token = jwtService.generateToken(admin);
//
//        mockMvc.perform(get("/api/admin/dashboard")
//                        .header("Authorization", "Bearer " + token))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message", is("Welcome to Admin Dashboard")));
//    }
}