package com.ironinstruction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironinstruction.api.requests.CreateUserRequest;
import com.ironinstruction.api.requests.LoginRequest;
import com.ironinstruction.api.requests.RefreshTokenRequest;
import com.ironinstruction.api.responses.JWTResponse;
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

import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
public class AuthenticationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService; 

    @Autowired
    private ObjectMapper objectMapper;

    private ArrayList<String> createdAccounts;

    public AuthenticationTests() {
        this.createdAccounts = new ArrayList<String>();  
    }

    @BeforeAll
    public void createUsers() throws Exception {
        userService.createUser("hello", "hello@gmail.com", "test", UserType.COACH);
        this.createdAccounts.add("hello@gmail.com");
    }

    @Test 
    public void testRegistration() throws Exception {
        CreateUserRequest request = new CreateUserRequest("test@gmail.com", "test", "test user", UserType.ATHLETE);
        CreateUserRequest duplicateUser = new CreateUserRequest("test@gmail.com", "test1", "test user1", UserType.COACH);
        
        mockMvc.perform(post("/api/v1/users")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
        
        this.createdAccounts.add(request.getEmail());

        mockMvc.perform(post("/api/v1/users")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(duplicateUser)))
            .andExpect(status().isBadRequest());
        
        assertDoesNotThrow(() -> userService.findUserByEmail(request.getEmail()));

    }

    @Test
    public void testLogin() throws Exception {
        LoginRequest validLogin = new LoginRequest("hello@gmail.com", "test");
        LoginRequest badEmail = new LoginRequest("hello1@gmail.com", "test");
        LoginRequest badPassword = new LoginRequest("hello@gmail.com", "test1");
        
        mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(badEmail)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("No user found")));

        mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(badPassword)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Incorrect password")));

        MvcResult validResult = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validLogin)))
            .andExpect(status().isOk())
            .andReturn();
        
        JWTResponse tokens = objectMapper.readValue(validResult.getResponse().getContentAsString(), JWTResponse.class);
        assertTrue(tokens.getAccessToken().length() != 0);
        assertTrue(tokens.getRefreshToken().length() != 0);

    }

    @Test 
    public void testTokenPermissions() throws Exception {
        LoginRequest validLogin = new LoginRequest("hello@gmail.com", "test");
        
        MvcResult validResult = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validLogin)))
            .andReturn();
        
        JWTResponse tokens = objectMapper.readValue(validResult.getResponse().getContentAsString(), JWTResponse.class);
        
        // invalid permissions
        mockMvc.perform(get("/api/v1/users/test@gmail.com")
            .header("Authorization", "Bearer " + tokens.getAccessToken()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("doesn't have permission")));
        
        // valid request
        mockMvc.perform(get("/api/v1/users/hello@gmail.com")
            .header("Authorization", "Bearer " + tokens.getAccessToken()))
            .andExpect(status().isOk());
       
        // invalid token
        mockMvc.perform(get("/api/v1/users/hello@gmail.com")
            .header("Authorization", "Bearer " + tokens.getRefreshToken()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid token")));
        
        // no token
        mockMvc.perform(get("/api/v1/users/hello@gmail.com"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("No token")));
    }

    @Test
    public void testRefreshToken() throws Exception {
        LoginRequest validLogin = new LoginRequest("hello@gmail.com", "test");

        MvcResult validResult = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validLogin)))
            .andReturn();
        
        JWTResponse tokens = objectMapper.readValue(validResult.getResponse().getContentAsString(), JWTResponse.class);
        
        // invalid token
        RefreshTokenRequest badRefreshRequest = new RefreshTokenRequest(tokens.getAccessToken());
        mockMvc.perform(post("/api/v1/refreshtoken")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(badRefreshRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid token")));
        
        // no token
        mockMvc.perform(post("/api/v1/refreshtoken"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid body")));

        // valid refresh
        RefreshTokenRequest refreshRequestOne = new RefreshTokenRequest(tokens.getRefreshToken());
        MvcResult newTokenResponse =  mockMvc.perform(post("/api/v1/refreshtoken")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(refreshRequestOne)))
            .andExpect(status().isOk())
            .andReturn();
        JWTResponse newTokens = objectMapper.readValue(newTokenResponse.getResponse().getContentAsString(), JWTResponse.class);

        // old token
        mockMvc.perform(post("/api/v1/refreshtoken")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(refreshRequestOne)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid token")));
        
        // valid new token
        RefreshTokenRequest refreshRequestTwo = new RefreshTokenRequest(newTokens.getRefreshToken());
        mockMvc.perform(post("/api/v1/refreshtoken")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(refreshRequestTwo)))
            .andExpect(status().isOk());
    }

    @AfterAll
    public void deleteAccounts() {
        this.createdAccounts.forEach((email) -> userService.deleteUserByEmail(email));
    }
}


