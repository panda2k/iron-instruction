package com.ironinstruction.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironinstruction.api.requests.AssignProgramRequest;
import com.ironinstruction.api.requests.NoteRequest;
import com.ironinstruction.api.requests.CreateProgramRequest;
import com.ironinstruction.api.requests.CreateUserRequest;
import com.ironinstruction.api.requests.LoginRequest;
import com.ironinstruction.api.user.UserService;
import com.ironinstruction.api.user.UserType;
import com.ironinstruction.api.program.Program;
import com.ironinstruction.api.program.ProgramService;
import com.ironinstruction.api.utils.TokenManager;
import com.ironinstruction.api.utils.TokenType;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

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

    private String getCookieValue(String header, String cookieName) {
        String regexPattern = cookieName + "=([^;]+ *);";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(header);
        matcher.find();
        return matcher.group(1);
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
        assertTrue(validResult.getResponse().getCookie("accessToken").getValue().length() != 0);
        assertTrue(validResult.getResponse().getCookie("refreshToken").getValue().length() != 0);
    }

    @Test 
    public void testTokenPermissions() throws Exception {
        LoginRequest validLogin = new LoginRequest("hello@gmail.com", "test");
        
        MvcResult validResult = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validLogin)))
            .andReturn();

        Cookie accessTokenCookie = new Cookie("accessToken", getCookieValue(validResult.getResponse().getHeaders("set-cookie").get(0), "accessToken"));
        Cookie refreshTokenCookie = new Cookie("refreshToken", getCookieValue(validResult.getResponse().getHeaders("set-cookie").get(1), "refreshToken"));
        
        // valid request
        mockMvc.perform(get("/api/v1/users/me")
            .cookie(accessTokenCookie))
            .andExpect(status().isOk());
       
        // invalid token
        mockMvc.perform(get("/api/v1/users/hello@gmail.com")
            .cookie(new Cookie("accessToken", refreshTokenCookie.getValue())))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Invalid token")));
        
        // no token
        mockMvc.perform(get("/api/v1/users/hello@gmail.com"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("No token")));
    }

    @Test
    public void testRefreshToken() throws Exception {
        LoginRequest validLogin = new LoginRequest("hello@gmail.com", "test");

        MvcResult validResult = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validLogin)))
            .andReturn();

        Cookie expiredAccessCookie = new Cookie("accessToken", TokenManager.generateJWT("hello@gmail.com;COACH", TokenType.ACCESS, new Date(0)));
        Cookie refreshTokenCookie = new Cookie("refreshToken", getCookieValue(validResult.getResponse().getHeaders("set-cookie").get(1), "refreshToken"));
         
        // invalid token
        mockMvc.perform(post("/api/v1/refreshtoken")
            .cookie(expiredAccessCookie)
            .cookie(new Cookie("refreshToken", "hfdhjsjk;fasd")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Invalid token")));
        
        // no token
        mockMvc.perform(post("/api/v1/refreshtoken")
            .cookie(expiredAccessCookie))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("No token")));

        // valid refresh
        MvcResult newTokenResponse =  mockMvc.perform(post("/api/v1/refreshtoken")
            .cookie(expiredAccessCookie)
            .cookie(refreshTokenCookie))
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(newTokenResponse.getResponse().getHeaders("set-cookie").size() == 2);

        // old token
        mockMvc.perform(post("/api/v1/refreshtoken")
            .cookie(expiredAccessCookie)
            .cookie(refreshTokenCookie))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Invalid token")));
        
        // valid new token
        mockMvc.perform(post("/api/v1/refreshtoken")
            .cookie(expiredAccessCookie)
            .cookie(new Cookie("refreshToken", getCookieValue(newTokenResponse.getResponse().getHeaders("set-cookie").get(1), "refreshToken"))))
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
        MvcResult validCoachResponse = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validCoach)))
            .andReturn();

        Cookie validCoachAccess = new Cookie("accessToken", getCookieValue(validCoachResponse.getResponse().getHeaders("set-cookie").get(0), "accessToken"));

        LoginRequest validAthlete = new LoginRequest("ath@gmail.com", "test");
        MvcResult validAthleteResponse = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validAthlete)))
            .andReturn();
        System.out.println(validCoachAccess.getValue());
        Cookie validAthleteAccess = new Cookie("accessToken", getCookieValue(validAthleteResponse.getResponse().getHeaders("set-cookie").get(0), "accessToken"));

        LoginRequest badAthlete = new LoginRequest("badath@gmail.com", "test");
        MvcResult badAthleteResponse = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(badAthlete)))
            .andReturn();

        Cookie badAthleteAccess = new Cookie("accessToken", getCookieValue(badAthleteResponse.getResponse().getHeaders("set-cookie").get(0), "accessToken"));

        LoginRequest badCoach = new LoginRequest("badcoach@gmail.com", "test");
        MvcResult badCoachResponse = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(badCoach)))
            .andReturn();

        Cookie badCoachAccess = new Cookie("accessToken", getCookieValue(badCoachResponse.getResponse().getHeaders("set-cookie").get(0), "accessToken"));

        // create program as athlete 
        CreateProgramRequest createProgramRequest = new CreateProgramRequest("strong", "get strong");

        mockMvc.perform(post("/api/v1/programs")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createProgramRequest))
            .cookie(validAthleteAccess))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Only coach")));
        
        // create valid program
        Program createdProgram = objectMapper.readValue(mockMvc.perform(post("/api/v1/programs")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createProgramRequest))
            .cookie(validCoachAccess))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString(), Program.class);

        this.createdPrograms.add(createdProgram.getId());

        String programUrlPath = "/api/v1/programs/" + createdProgram.getId();

        // assign program
        mockMvc.perform(post(programUrlPath + "/assign")
            .cookie(validCoachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new AssignProgramRequest("ath@gmail.com"))))
            .andExpect(status().isOk());
        
        // assign program as invalid coach
        mockMvc.perform(post(programUrlPath + "/assign")
            .cookie(badCoachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new AssignProgramRequest("ath@gmail.com"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("doesn't have permission")));

        // assign program as athlete 
        mockMvc.perform(post(programUrlPath + "/assign")
            .cookie(validAthleteAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new AssignProgramRequest("ath@gmail.com"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Only coaches")));

        // get program as coach
        assertDoesNotThrow(() -> objectMapper.readValue(mockMvc.perform(get(programUrlPath)
            .cookie(validCoachAccess))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(createdProgram.getName()))
            .andReturn()
            .getResponse().getContentAsString(), Program.class));

        // get program as athlete
        assertDoesNotThrow(() -> objectMapper.readValue(mockMvc.perform(get(programUrlPath)
            .cookie(validAthleteAccess))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(createdProgram.getName()))
            .andReturn()
            .getResponse().getContentAsString(), Program.class));

        // get program with unauthorized coach 
        mockMvc.perform(get(programUrlPath)
            .cookie(badAthleteAccess))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("doesn't have permission")));

        // get program with unautorized athlete
        mockMvc.perform(get(programUrlPath)
            .cookie(badCoachAccess))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("doesn't have permission")));
       
        // add to program as coach
        Program updatedProgram = objectMapper.readValue(mockMvc.perform(post(programUrlPath + "/weeks")
            .cookie(validCoachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("hi"))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), Program.class);

        // add to program as athlete
        mockMvc.perform(post(programUrlPath + "/weeks")
            .cookie(validAthleteAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("hi"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Only coach")));

        // add to program as bad coach 
        mockMvc.perform(post(programUrlPath + "/weeks")
            .cookie(badCoachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("hi"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("doesn't have permission")));

        // add to athlete notes as coach
        mockMvc.perform(patch(programUrlPath + "/weeks/" + updatedProgram.getWeeks().get(0).getId() + "/notes")
            .cookie(validCoachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("hi"))))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Coaches can't use")));

        // add to athlete note as athlete
        mockMvc.perform(patch(programUrlPath + "/weeks/" + updatedProgram.getWeeks().get(0).getId() + "/notes")
            .cookie(validAthleteAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("hi"))))
            .andExpect(status().isOk());
    }

    @AfterAll
    public void deleteAccounts() {
        this.createdAccounts.forEach((email) -> userService.deleteByEmail(email));
        this.createdPrograms.forEach((id) -> programService.deleteById(id));
    }
}


