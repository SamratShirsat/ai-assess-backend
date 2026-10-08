package com.samrat.ai.developer.assessment;

import com.samrat.ai.developer.assessment.problem.dto.ProblemCreateRequest;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class ProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void getProblems_AuthenticatedUser_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/problems"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProblem_NormalUser_ShouldReturn403Forbidden() throws Exception {
        ProblemCreateRequest request = ProblemCreateRequest.builder()
                .title("Test Problem")
                .description("Test Description")
                .difficulty(Difficulty.EASY)
                .topic(Topic.ARRAY)
                .build();

        mockMvc.perform(post("/api/admin/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProblem_AdminUser_ShouldReturn201Created() throws Exception {
        ProblemCreateRequest request = ProblemCreateRequest.builder()
                .title("Test Problem")
                .description("Test Description")
                .difficulty(Difficulty.EASY)
                .topic(Topic.ARRAY)
                .build();

        mockMvc.perform(post("/api/admin/problems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Problem"));
    }

    @Test
    void getProblems_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/problems"))
                .andExpect(status().isUnauthorized());
    }
}