package com.ironinstruction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironinstruction.api.requests.CreateProgramRequest;
import com.ironinstruction.api.requests.CreateUserRequest;
import com.ironinstruction.api.requests.LoginRequest;
import com.ironinstruction.api.requests.RefreshTokenRequest;
import com.ironinstruction.api.responses.JWTResponse;
import com.ironinstruction.api.user.UserService;
import com.ironinstruction.api.user.UserType;
import com.ironinstruction.api.program.Program;
import com.ironinstruction.api.program.ProgramService;
import com.ironinstruction.api.request.AssignProgramRequest;
import com.ironinstruction.api.request.CreateWithCoachNoteRequest;

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
    private ProgramService programService;

    @Autowired
    private ObjectMapper objectMapper;

    private ArrayList<String> createdAccounts;
    private ArrayList<String> createdPrograms;

    public AuthenticationTests() {
        this.createdPrograms = new ArrayList<String>();
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
        
        assertDoesNotThrow(() -> userService.findByEmail(request.getEmail()));

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

    @Test 
    public void testProgramPermissions() throws Exception {
        userService.createUser("athlete", "ath@gmail.com", "test", UserType.ATHLETE);
        userService.createUser("athlete", "badath@gmail.com", "test", UserType.ATHLETE);
        userService.createUser("coach", "badcoach@gmail.com", "test", UserType.COACH);
        this.createdAccounts.add("ath@gmail.com");
        this.createdAccounts.add("badath@gmail.com");
        this.createdAccounts.add("badcoach@gmail.com");
        
        LoginRequest validCoach = new LoginRequest("hello@gmail.com", "test");
        JWTResponse validCoachTokens = objectMapper.readValue(mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validCoach)))
            .andReturn()
            .getResponse().getContentAsString(), JWTResponse.class);

        LoginRequest validAthlete = new LoginRequest("ath@gmail.com", "test");
        JWTResponse validAthleteTokens = objectMapper.readValue(mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validAthlete)))
            .andReturn()
            .getResponse().getContentAsString(), JWTResponse.class);

        LoginRequest badAthlete = new LoginRequest("badath@gmail.com", "test");
        JWTResponse badAthleteTokens = objectMapper.readValue(mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(badAthlete)))
            .andReturn()
            .getResponse().getContentAsString(), JWTResponse.class);

        LoginRequest badCoach = new LoginRequest("badcoach@gmail.com", "test");
        JWTResponse badCoachTokens = objectMapper.readValue(mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(badCoach)))
            .andReturn()
            .getResponse().getContentAsString(), JWTResponse.class);

        // create program as athlete 
        CreateProgramRequest createProgramRequest = new CreateProgramRequest("strong", "get strong", false);
        mockMvc.perform(post("/api/v1/programs")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createProgramRequest))
            .header("Authorization", "Bearer " + validAthleteTokens.getAccessToken()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Only coach")));
        
        // create valid program
        Program createdProgram = objectMapper.readValue(mockMvc.perform(post("/api/v1/programs")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createProgramRequest))
            .header("Authorization", "Bearer " + validCoachTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString(), Program.class);

        this.createdPrograms.add(createdProgram.getId());

        String programUrlPath = "/api/v1/programs/" + createdProgram.getId();

        // assign program
        mockMvc.perform(post(programUrlPath + "/assign")
            .header("Authorization", "Bearer " + validCoachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new AssignProgramRequest("ath@gmail.com"))))
            .andExpect(status().isOk());
        
        // assign program as invalid coach
        mockMvc.perform(post(programUrlPath + "/assign")
            .header("Authorization", "Bearer " + badCoachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new AssignProgramRequest("ath@gmail.com"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("doesn't have permission")));

        // assign program as athlete 
        mockMvc.perform(post(programUrlPath + "/assign")
            .header("Authorization", "Bearer " + validAthleteTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new AssignProgramRequest("ath@gmail.com"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Only coaches")));

        // get program as coach
        assertDoesNotThrow(() -> objectMapper.readValue(mockMvc.perform(get(programUrlPath)
            .header("Authorization", "Bearer " + validCoachTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(createdProgram.getName()))
            .andReturn()
            .getResponse().getContentAsString(), Program.class));

        // get program as athlete
        assertDoesNotThrow(() -> objectMapper.readValue(mockMvc.perform(get(programUrlPath)
            .header("Authorization", "Bearer " + validAthleteTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(createdProgram.getName()))
            .andReturn()
            .getResponse().getContentAsString(), Program.class));

        // get program with unauthorized coach 
        mockMvc.perform(get(programUrlPath)
            .header("Authorization", "Bearer " + badAthleteTokens.getAccessToken()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("doesn't have permission")));

        // get program with unautorized athlete
        mockMvc.perform(get(programUrlPath)
            .header("Authorization", "Bearer " + badCoachTokens.getAccessToken()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("doesn't have permission")));
       
        // add to program as coach
        mockMvc.perform(post(programUrlPath + "/weeks")
            .header("Authorization", "Bearer" + validCoachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new CreateWithCoachNoteRequest("hi"))))
            .andExpect(status().isOk());

        // add to program as athlete
        mockMvc.perform(post(programUrlPath + "/weeks")
            .header("Authorization", "Bearer" + validAthleteTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new CreateWithCoachNoteRequest("hi"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Only coach")));

        // add to program as bad coach 
        mockMvc.perform(post(programUrlPath + "/weeks")
            .header("Authorization", "Bearer" + badCoachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new CreateWithCoachNoteRequest("hi"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("doesn't have permission")));
    }

    @AfterAll
    public void deleteAccounts() {
        this.createdAccounts.forEach((email) -> userService.deleteByEmail(email));
        this.createdPrograms.forEach((id) -> programService.deleteById(id));
    }
}


