// AdminSecurityIntegrationTest.java
package com.samrat.ai.developer.assessment.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Non-admin (USER) receives 403 Forbidden accessing Admin Dashboard API")
    @WithMockUser(username = "dev_user", roles = {"USER"})
    void testUserAccessDashboardForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Non-admin (USER) receives 403 Forbidden attempting to fetch submissions log")
    @WithMockUser(username = "dev_user", roles = {"USER"})
    void testUserAccessSubmissionsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/submissions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin receives 200 OK accessing Admin Dashboard API")
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void testAdminAccessDashboardOk() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk());
    }
}