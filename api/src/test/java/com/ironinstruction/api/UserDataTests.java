package com.ironinstruction.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironinstruction.api.program.Day;
import com.ironinstruction.api.program.Exercise;
import com.ironinstruction.api.program.PercentageOptions;
import com.ironinstruction.api.program.Program;
import com.ironinstruction.api.program.ProgramService;
import com.ironinstruction.api.program.Set;
import com.ironinstruction.api.program.Week;
import com.ironinstruction.api.requests.AssignProgramRequest;
import com.ironinstruction.api.requests.NoteRequest;
import com.ironinstruction.api.requests.FinishSetRequest;
import com.ironinstruction.api.requests.CreateExerciseRequest;
import com.ironinstruction.api.requests.CreateProgramRequest;
import com.ironinstruction.api.requests.CreateSetRequest;
import com.ironinstruction.api.requests.LoginRequest;
import com.ironinstruction.api.requests.UpdateAthleteRequest;
import com.ironinstruction.api.responses.JWTResponse;
import com.ironinstruction.api.responses.VideoLinkResponse;
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
import java.util.List;

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
    private ProgramService programService;

    @Autowired
    private ObjectMapper objectMapper;

    private ArrayList<String> createdAccounts;
    private ArrayList<String> createdPrograms;

    private JWTResponse athleteTokens;
    private JWTResponse coachTokens;

    public UserDataTests () {
        this.createdAccounts = new ArrayList<String>();  
        this.createdPrograms = new ArrayList<String>();
    }

    @BeforeAll
    public void createUsers() throws Exception {
        userService.createUser("data", "data@gmail.com", "test", UserType.ATHLETE);
        userService.createUser("coachdata", "coachdata@gmail.com", "test", UserType.COACH);
        this.createdAccounts.add("data@gmail.com");
        this.createdAccounts.add("coachdata@gmail.com");

        MvcResult validAthleteResult = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new LoginRequest("data@gmail.com", "test"))))
            .andReturn();
        
        this.athleteTokens = objectMapper.readValue(validAthleteResult.getResponse().getContentAsString(), JWTResponse.class);

        MvcResult validCoachResult = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new LoginRequest("coachdata@gmail.com", "test"))))
            .andReturn();
        
        this.coachTokens = objectMapper.readValue(validCoachResult.getResponse().getContentAsString(), JWTResponse.class);
    }

    @Test void testProgramData() throws Exception {
        // create valid program
        CreateProgramRequest createProgramRequest = new CreateProgramRequest("strong", "get strong");
        Program createdProgram = objectMapper.readValue(mockMvc.perform(post("/api/v1/programs")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createProgramRequest))
            .header("Authorization", "Bearer " + this.coachTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString(), Program.class);

        assertDoesNotThrow(() -> programService.findById(createdProgram.getId()));
        this.createdPrograms.add(createdProgram.getId());

        String programUrlPath = "/api/v1/programs/" + createdProgram.getId();

        // create second valid program
        CreateProgramRequest secondProgramRequest = new CreateProgramRequest("get lean", "get very lean");
        Program secondProgram = objectMapper.readValue(mockMvc.perform(post("/api/v1/programs")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(secondProgramRequest))
            .header("Authorization", "Bearer " + this.coachTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString(), Program.class);
        createdPrograms.add(secondProgram.getId());
        // assign program
        mockMvc.perform(post(programUrlPath + "/assign")
            .header("Authorization", "Bearer " + coachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new AssignProgramRequest("data@gmail.com"))))
            .andExpect(status().isOk());
       
        assertTrue(programService.findById(createdProgram.getId()).getAthleteEmail().equals("data@gmail.com"));
        
        // assign program to coach
        mockMvc.perform(post(programUrlPath + "/assign")
            .header("Authorization", "Bearer " + coachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new AssignProgramRequest("coachdata@gmail.com"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Cannot assign program")));

        // get coach's programs 
        List<Program> coachPrograms = objectMapper.readValue(mockMvc.perform(get("/api/v1/programs/user/coachdata@gmail.com")
            .header("Authorization", "Bearer " + coachTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), new TypeReference<List<Program>>(){});

        assertTrue(coachPrograms.size() == 2);
        assertTrue(coachPrograms.get(0).getId().equals(createdProgram.getId()));
        assertTrue(coachPrograms.get(1).getId().equals(secondProgram.getId()));

        // get athlete's programs
        List<Program> athletePrograms = objectMapper.readValue(mockMvc.perform(get("/api/v1/programs/user/data@gmail.com")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), new TypeReference<List<Program>>(){});
        
        assertTrue(athletePrograms.size() == 1);
        assertTrue(athletePrograms.get(0).getId().equals(createdProgram.getId()));

        // add week to program
        mockMvc.perform(post(programUrlPath + "/weeks")
            .header("Authorization", "Bearer" + coachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("week 1"))))
            .andExpect(status().isOk());
        Week week = programService.findById(createdProgram.getId()).getWeeks().get(0);
        assertTrue(week.getCoachNotes().equals("week 1"));

        // add day to week
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days")
            .header("Authorization", "Bearer" + coachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("day 1"))))
            .andExpect(status().isOk());

        Day day = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0);
        assertTrue(day.getCoachNotes().equals("day 1"));
        
        // add exercise to day
        CreateExerciseRequest createExerciseRequest = new CreateExerciseRequest("Bench", "bench more", "");
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises")
            .header("Authorization", "Bearer" + coachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createExerciseRequest)))
            .andExpect(status().isOk());
        Exercise exercise = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0);
        assertTrue(exercise.getName().equals("Bench"));

        // add set to exercise (rpe)
        CreateSetRequest createSetRequestRpe = new CreateSetRequest(0, 8, 12, 100, PercentageOptions.Bench, "note", true);
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets")
            .header("Authorization", "Bearer" + coachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createSetRequestRpe)))
            .andExpect(status().isOk());

        Set set = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0).getSets().get(0);
        assertTrue(set.getCoachNotes().equals("note"));
        assertTrue(set.getRpe() == 8);
        assertTrue(set.getReps() == 0);

        // add set to exercise (reps)
        CreateSetRequest createSetRequestReps = new CreateSetRequest(10, -1, 12, 100, PercentageOptions.Bench, "note", true);
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets")
            .header("Authorization", "Bearer" + coachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createSetRequestReps)))
            .andExpect(status().isOk());

        Set setReps = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0).getSets().get(1);
        assertTrue(setReps.getCoachNotes().equals("note"));
        assertTrue(setReps.getRpe() == 0);
        assertTrue(setReps.getReps() == 10);
        assertTrue(setReps.getPercentageReference().equals(PercentageOptions.Bench));

        // complete set 
        FinishSetRequest finishSetRequest = new FinishSetRequest(8, "was fatigued");
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets/" + setReps.getId())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(finishSetRequest))
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken()))
            .andExpect(status().isOk());
        setReps = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0).getSets().get(1);
        assertTrue(setReps.getCompletedReps() == finishSetRequest.getRepsDone());
        assertTrue(setReps.getAthleteNotes().equals(finishSetRequest.getAthleteNotes()));

        // get non existent download link  
        mockMvc.perform(get(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets/" + set.getId() + "/video")
            .header("Authorization", "Bearer " + coachTokens.getAccessToken()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));

        // get set video upload link
        VideoLinkResponse setUploadLink = objectMapper.readValue(mockMvc.perform(get(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets/" + set.getId() + "/video/upload")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), VideoLinkResponse.class);

        // get set video download link
        VideoLinkResponse setDownloadLink = objectMapper.readValue(mockMvc.perform(get(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets/" + set.getId() + "/video")
            .header("Authorization", "Bearer " + coachTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), VideoLinkResponse.class);
        
        System.out.println("Set Links: ");
        System.out.println(setUploadLink.getUrl());
        System.out.println(setDownloadLink.getUrl());

        set = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0).getSets().get(0);
        assertTrue(set.getVideoRef().equals(set.getId() + ".mp4"));

        // get non-existent download link 
        mockMvc.perform(get(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/video")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));

        // get exercise video upload link
        VideoLinkResponse exerciseUploadLink = objectMapper.readValue(mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/video/upload")
            .header("Authorization", "Bearer " + coachTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), VideoLinkResponse.class);

        // get exercise video download link
        VideoLinkResponse exerciseDownloadLink = objectMapper.readValue(mockMvc.perform(get(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/video")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), VideoLinkResponse.class);
        
        System.out.println("Exercise Links: ");
        System.out.println(exerciseUploadLink.getUrl());
        System.out.println(exerciseDownloadLink.getUrl());

        exercise = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0);
        assertTrue(exercise.getVideoRef().equals(exercise.getId() + ".mp4"));

        // update day 
        day = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0);
        String dayId = day.getId();
        day.findExerciseById(exercise.getId()).getSets().remove(0);
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/update")
            .header("Authorization", "Bearer " + coachTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(day)))
            .andExpect(status().isOk());

        day = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0);
        assertTrue(day.getId().equals(dayId));
        assertTrue(day.findExerciseById(exercise.getId()).getSets().size() == 1);

        // update week note
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/notes")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("Good week"))))
            .andExpect(status().isOk()); 

        assertTrue(programService.findById(createdProgram.getId()).getWeeks().get(0).getAthleteNotes().equals("Good week"));

        // update day note 
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/notes")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("Good day"))))
            .andExpect(status().isOk()); 

        assertTrue(programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getAthleteNotes().equals("Good day"));

        // update exercise note
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/notes")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("Good bench"))))
            .andExpect(status().isOk()); 

        assertTrue(programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0).getAthleteNotes().equals("Good bench"));

        // get invalid program 
        mockMvc.perform(get("/api/v1/programs/fdsjafjkas")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Invalid resource")));

        // add invalid week note
        mockMvc.perform(patch(programUrlPath + "/weeks/fjdkskfsd/notes")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("bad"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));

        // add invalid day note
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/days/fjdsjkfksd/notes")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("bad"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));
        
        // add invalid exercise note 
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/fdsjkfkjdskj/notes")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("bad"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));

        // update invalid set
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets/fdsjkkjlfsdjkls")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(finishSetRequest))
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));
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
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken()))
            .andExpect(status().isOk());

        Athlete ath = (Athlete) userService.findByEmail("data@gmail.com");
        assertTrue(ath.getWeightClass().equals(validRequest.getWeightClass()));
        assertTrue(ath.getWeight() == validRequest.getWeight());
        assertTrue(ath.getDob().getDay() == validRequest.getDob().getDay());
        assertTrue(ath.getDob().getYear() == validRequest.getDob().getYear());
        assertTrue(ath.getDob().getMonth() == validRequest.getDob().getMonth());
        assertTrue(ath.getSquatMax() == validRequest.getSquatMax());
        assertTrue(ath.getBenchMax() == validRequest.getBenchMax());
        assertTrue(ath.getDeadliftMax() == validRequest.getDeadliftMax());
        assertTrue(ath.getHeight() == validRequest.getHeight());
       
        // test invalid user
        mockMvc.perform(post("/api/v1/users/data1@gmail.com")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validRequest))
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken()))
            .andExpect(status().isForbidden());
       
        // test empty body
        mockMvc.perform(post("/api/v1/users/data@gmail.com")
            .contentType("application/json")
            .header("Authorization", "Bearer " + athleteTokens.getAccessToken()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid body")));
    }

    @AfterAll
    public void deleteAccounts() {
        this.createdAccounts.forEach((email) -> userService.deleteByEmail(email));
        this.createdPrograms.forEach((id) -> programService.deleteById(id));
    }
}

