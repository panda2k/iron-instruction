package com.ironinstruction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironinstruction.api.requests.CreateUserRequest;
import com.ironinstruction.api.requests.LoginRequest;
import com.ironinstruction.api.requests.RefreshTokenRequest;
import com.ironinstruction.api.requests.UpdateAthleteRequest;
import com.ironinstruction.api.responses.JWTResponse;
import com.ironinstruction.api.user.Athlete;
import com.ironinstruction.api.user.UserService;
import com.ironinstruction.api.user.UserType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Date;

import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
public class UserDataTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService; 

    @Autowired
    private ObjectMapper objectMapper;

    private ArrayList<String> createdAccounts;
    private JWTResponse tokens;

    public UserDataTests () {
        this.createdAccounts = new ArrayList<String>();  
    }

    @BeforeAll
    public void createUsers() throws Exception {
        userService.createUser("data", "data@gmail.com", "test", UserType.ATHLETE);
        this.createdAccounts.add("data@gmail.com");

        MvcResult validResult = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new LoginRequest("data@gmail.com", "test"))))
            .andReturn();
        
        this.tokens = objectMapper.readValue(validResult.getResponse().getContentAsString(), JWTResponse.class);
    }

    @Test void testProgramAddition() throws Exception {
        
    }

    @Test
    public void testAthleteUpdate() throws Exception {
        // test valid body
        UpdateAthleteRequest validRequest = new UpdateAthleteRequest(
            "67.5kg",
            (float) 71.2,
            new Date(System.currentTimeMillis()),
            150,
            100,
            175,
            65
        );
        
        mockMvc.perform(post("/api/v1/users/data@gmail.com")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validRequest))
            .header("Authorization", "Bearer " + tokens.getAccessToken()))
            .andExpect(status().isOk());

        Athlete ath = (Athlete) userService.findByEmail("data@gmail.com");
        assertTrue(ath.getWeightClass().equals(validRequest.getWeightClass()));
        assertTrue(ath.getWeight() == validRequest.getWeight());
        assertTrue(ath.getDob().equals(validRequest.getDob()));
        assertTrue(ath.getSquatMax() == validRequest.getSquatMax());
        assertTrue(ath.getBenchMax() == validRequest.getBenchMax());
        assertTrue(ath.getDeadliftMax() == validRequest.getDeadliftMax());
        assertTrue(ath.getHeight() == validRequest.getHeight());
       
        // test invalid user
        mockMvc.perform(post("/api/v1/users/data1@gmail.com")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validRequest))
            .header("Authorization", "Bearer " + tokens.getAccessToken()))
            .andExpect(status().isForbidden());
       
        // test empty body
        mockMvc.perform(post("/api/v1/users/data@gmail.com")
            .contentType("application/json")
            .header("Authorization", "Bearer " + tokens.getAccessToken()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid body")));
    }

    @AfterAll
    public void deleteAccounts() {
        this.createdAccounts.forEach((email) -> userService.deleteByEmail(email));
    }
}

