package com.anuththara.jobhunttracker.company;

import com.anuththara.jobhunttracker.auth.dto.RegisterRequest;
import com.anuththara.jobhunttracker.company.dto.CompanyRequest;
import com.anuththara.jobhunttracker.jobapplication.JobApplicationRepository;
import com.anuththara.jobhunttracker.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private JobApplicationRepository jobApplicationRepository;

    @BeforeEach
    void cleanDatabase() {
        jobApplicationRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String registerAndGetToken(String email) throws Exception {
        RegisterRequest request = new RegisterRequest("Test", "User", email, "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    @Test
    void createCompany_noToken_returns401() throws Exception {
        CompanyRequest request = new CompanyRequest("Xero", "Fintech", null, "Auckland", null);

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCompany_validToken_returns201WithCompany() throws Exception {
        String token = registerAndGetToken("anu@test.com");
        CompanyRequest request = new CompanyRequest("Xero", "Fintech", "https://xero.com", "Auckland", null);

        mockMvc.perform(post("/api/companies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Xero"))
                .andExpect(jsonPath("$.industry").value("Fintech"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getCompany_anotherUsersCompany_returns404() throws Exception {
        // User A creates a company
        String tokenA = registerAndGetToken("usera@test.com");
        MvcResult createResult = mockMvc.perform(post("/api/companies")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CompanyRequest("Xero", "Fintech", null, "Auckland", null))))
                .andReturn();
        Long companyId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // User B tries to access User A's company
        String tokenB = registerAndGetToken("userb@test.com");
        mockMvc.perform(get("/api/companies/" + companyId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }
}