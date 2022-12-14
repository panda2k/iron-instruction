package com.ironinstruction.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ironinstruction.api.errors.ResourceNotFound;
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
import com.ironinstruction.api.requests.UpdateUserRequest;
import com.ironinstruction.api.responses.VideoLinkResponse;
import com.ironinstruction.api.user.Athlete;
import com.ironinstruction.api.user.User;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

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

    private Cookie athleteAccess;
    private Cookie coachAccess;

    private String getCookieValue(String header, String cookieName) {
        String regexPattern = cookieName + "=([^;]+ *);";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(header);
        matcher.find();
        return matcher.group(1);
    }

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
        
        
        this.athleteAccess = new Cookie("accessToken", getCookieValue(validAthleteResult.getResponse().getHeaders("set-cookie").get(0), "accessToken"));

        MvcResult validCoachResult = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new LoginRequest("coachdata@gmail.com", "test"))))
            .andReturn();

        this.coachAccess= new Cookie("accessToken", getCookieValue(validCoachResult.getResponse().getHeaders("set-cookie").get(0), "accessToken"));
    }

    @Test void testProgramDeleteData() throws Exception {
        CreateProgramRequest createProgramRequest = new CreateProgramRequest("strong", "get strong");
        Program createdProgram = objectMapper.readValue(mockMvc.perform(post("/api/v1/programs")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createProgramRequest))
            .cookie(coachAccess))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString(), Program.class);

        this.createdPrograms.add(createdProgram.getId());

        String programUrlPath = "/api/v1/programs/" + createdProgram.getId();

        mockMvc.perform(post(programUrlPath + "/weeks")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("week 1"))))
            .andExpect(status().isOk());
        Week week = programService.findById(createdProgram.getId()).getWeeks().get(0);

        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("day 1"))))
            .andExpect(status().isOk());

        Day day = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0);
        
        // test delete day
        mockMvc.perform(delete(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId())
            .cookie(coachAccess))
            .andExpect(status().isOk());

        assertThrows(
            ResourceNotFound.class, () -> {
            programService
                .findById(createdProgram.getId())
                .getWeeks().get(0)
                .findDayById(day.getId());
        });

        // test delete week
        mockMvc.perform(delete(programUrlPath + "/weeks/" + week.getId())
            .cookie(coachAccess))
            .andExpect(status().isOk());

        assertThrows(
            ResourceNotFound.class, () -> {
            programService
                .findById(createdProgram.getId())
                .findWeekById(week.getId());
        });
    }

    @Test void testProgramData() throws Exception {
        // create valid program
        CreateProgramRequest createProgramRequest = new CreateProgramRequest("strong", "get strong");
        Program createdProgram = objectMapper.readValue(mockMvc.perform(post("/api/v1/programs")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createProgramRequest))
            .cookie(coachAccess))
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
            .cookie(coachAccess))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString(), Program.class);
        createdPrograms.add(secondProgram.getId());

        // update program info
        CreateProgramRequest updateProgramRequest = new CreateProgramRequest("update", "updated");
        mockMvc.perform(post(programUrlPath)
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(updateProgramRequest)))
            .andExpect(status().isOk());

        Program updatedProgram = programService.findById(createdProgram.getId());
        assertTrue(updatedProgram.getName().equals(updateProgramRequest.getName()));
        assertTrue(updatedProgram.getDescription().equals(updateProgramRequest.getDescription()));

        // assign program
        mockMvc.perform(post(programUrlPath + "/assign")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new AssignProgramRequest("data@gmail.com"))))
            .andExpect(status().isOk());
       
        assertTrue(programService.findById(createdProgram.getId()).getAthleteEmail().equals("data@gmail.com"));
        
        // assign program to coach
        mockMvc.perform(post(programUrlPath + "/assign")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new AssignProgramRequest("coachdata@gmail.com"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Cannot assign program")));

        // get coach's programs 
        List<Program> coachPrograms = objectMapper.readValue(mockMvc.perform(get("/api/v1/programs/user/me")
            .cookie(coachAccess))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), new TypeReference<List<Program>>(){});

        assertTrue(coachPrograms.size() == 2);
        assertTrue(coachPrograms.get(0).getId().equals(createdProgram.getId()));
        assertTrue(coachPrograms.get(1).getId().equals(secondProgram.getId()));

        // get athlete's programs
        List<Program> athletePrograms = objectMapper.readValue(mockMvc.perform(get("/api/v1/programs/user/me")
            .cookie(athleteAccess))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), new TypeReference<List<Program>>(){});
        
        assertTrue(athletePrograms.size() == 1);
        assertTrue(athletePrograms.get(0).getId().equals(createdProgram.getId()));

        // add week to program
        mockMvc.perform(post(programUrlPath + "/weeks")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("week 1"))))
            .andExpect(status().isOk());
        Week week = programService.findById(createdProgram.getId()).getWeeks().get(0);
        assertTrue(week.getCoachNotes().equals("week 1"));

        // add day to week
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("day 1"))))
            .andExpect(status().isOk());

        Day day = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0);
        assertTrue(day.getCoachNotes().equals("day 1"));

        // add exercise to day
        CreateExerciseRequest createExerciseRequest = new CreateExerciseRequest("Bench", "");
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createExerciseRequest)))
            .andExpect(status().isOk());
        Exercise exercise = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0);
        assertTrue(exercise.getName().equals("Bench"));

        // add invalid exercise
        createExerciseRequest.setName("    ");
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createExerciseRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("must not be blank")));

        // add set to exercise (rpe)
        CreateSetRequest createSetRequestRpe = new CreateSetRequest(9, 8, 12, 100, PercentageOptions.Bench, true);
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createSetRequestRpe)))
            .andExpect(status().isOk());

        Set set = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0).getSets().get(0);
        assertTrue(set.getRpe() == 8);
        assertTrue(set.getReps() == 9);

        // add set to exercise (reps)
        CreateSetRequest createSetRequestReps = new CreateSetRequest(10, -1, 12, 100, PercentageOptions.Bench, true);
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(createSetRequestReps)))
            .andExpect(status().isOk());

        Set setReps = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0).getSets().get(1);
        assertTrue(setReps.getRpe() == 0);
        assertTrue(setReps.getReps() == 10);
        assertTrue(setReps.getPercentageReference().equals(PercentageOptions.Bench));

        // complete set 
        FinishSetRequest finishSetRequest = new FinishSetRequest(8);
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets/" + setReps.getId())
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(finishSetRequest))
            .cookie(athleteAccess))
            .andExpect(status().isOk());
        setReps = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0).getSets().get(1);
        assertTrue(setReps.getCompletedReps() == finishSetRequest.getRepsDone());

        // get non existent download link  
        mockMvc.perform(get(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets/" + set.getId() + "/video")
            .cookie(coachAccess))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));

        // get set video upload link
        VideoLinkResponse setUploadLink = objectMapper.readValue(mockMvc.perform(get(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets/" + set.getId() + "/video/upload")
            .cookie(athleteAccess))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), VideoLinkResponse.class);

        // get set video download link
        VideoLinkResponse setDownloadLink = objectMapper.readValue(mockMvc.perform(get(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets/" + set.getId() + "/video")
            .cookie(coachAccess))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), VideoLinkResponse.class);
        
        System.out.println("Set Links: ");
        System.out.println(setUploadLink.getUrl());
        System.out.println(setDownloadLink.getUrl());

        set = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0).getSets().get(0);
        assertTrue(set.getVideoRef().equals(set.getId() + ".mp4"));

        // get non-existent download link 
        mockMvc.perform(get(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/video")
            .cookie(athleteAccess))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));

        // get exercise video upload link
        VideoLinkResponse exerciseUploadLink = objectMapper.readValue(mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/video/upload")
            .cookie(coachAccess))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), VideoLinkResponse.class);

        // get exercise video download link
        VideoLinkResponse exerciseDownloadLink = objectMapper.readValue(mockMvc.perform(get(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/video")
            .cookie(athleteAccess))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), VideoLinkResponse.class);
        
        System.out.println("Exercise Links: ");
        System.out.println(exerciseUploadLink.getUrl());
        System.out.println(exerciseDownloadLink.getUrl());

        exercise = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0);
        assertTrue(exercise.getVideoRef().equals(exercise.getId() + ".mp4"));

        // update exercise with blank name 
        exercise.setName("   ");
        mockMvc.perform(put(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId())
            .contentType("application/json")
            .cookie(coachAccess)
            .content(objectMapper.writeValueAsString(exercise)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("must not be blank")));

        // update exercise
        exercise.setName("updating name test");
        final String deletedSetId = exercise.getSets().remove(0).getId();
        String content = objectMapper.writeValueAsString(exercise).replaceAll(exercise.getSets().get(0).getId(), "");
        mockMvc.perform(put(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId())
            .contentType("application/json")
            .cookie(coachAccess)
            .content(content))
            .andExpect(status().isOk());  
         
        exercise = programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getExercises().get(0);
        final String exerciseId = exercise.getId();

        assertTrue(exercise.getName().equals("updating name test"));
        assertThrows(ResourceNotFound.class, () -> programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).findExerciseById(exerciseId).findSetById(deletedSetId));
        assertTrue(!exercise.getSets().get(0).getId().isEmpty());

        // delete exercise
        mockMvc.perform(delete(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId())
            .cookie(coachAccess))
            .andExpect(status().isOk());

        assertThrows(ResourceNotFound.class, () -> programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).findExerciseById(exerciseId));

        // update week coach note 
        NoteRequest updateWeekNote = new NoteRequest("updated week 1");
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/notes")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(updateWeekNote)))
            .andExpect(status().isOk());

        week = programService.findById(createdProgram.getId()).findWeekById(week.getId());
        assertTrue(week.getCoachNotes().equals(updateWeekNote.getNote()));

        // update day coach note 
        NoteRequest updatedDayNote = new NoteRequest("updated day 1");
        mockMvc.perform(post(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/notes")
            .cookie(coachAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(updatedDayNote)))
            .andExpect(status().isOk());

        day = programService.findById(createdProgram.getId()).findWeekById(week.getId()).findDayById(day.getId());
        assertTrue(day.getCoachNotes().equals(updatedDayNote.getNote()));

        // update week athlete note
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/notes")
            .cookie(athleteAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("Good week"))))
            .andExpect(status().isOk()); 

        assertTrue(programService.findById(createdProgram.getId()).getWeeks().get(0).getAthleteNotes().equals("Good week"));

        // update day athlete note 
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/notes")
            .cookie(athleteAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("Good day"))))
            .andExpect(status().isOk()); 

        assertTrue(programService.findById(createdProgram.getId()).getWeeks().get(0).getDays().get(0).getAthleteNotes().equals("Good day"));

        // get invalid program 
        mockMvc.perform(get("/api/v1/programs/fdsjafjkas")
            .cookie(athleteAccess))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("Invalid resource")));

        // add invalid week note
        mockMvc.perform(patch(programUrlPath + "/weeks/fjdkskfsd/notes")
            .cookie(athleteAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("bad"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));

        // add invalid day note
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/days/fjdsjkfksd/notes")
            .cookie(athleteAccess)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new NoteRequest("bad"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));
        
        // update invalid set
        mockMvc.perform(patch(programUrlPath + "/weeks/" + week.getId() + "/days/" + day.getId() + "/exercises/" + exercise.getId() + "/sets/fdsjkkjlfsdjkls")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(finishSetRequest))
            .cookie(athleteAccess))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test 
    public void testUserUpdate() throws Exception {
        userService.createUser("hi", "userupdate@gmail.com", "test", UserType.ATHLETE);
        createdAccounts.add("userupdate@gmail.com");

        MvcResult userLogin = mockMvc.perform(post("/api/v1/login")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(new LoginRequest("data@gmail.com", "test"))))
            .andReturn();
        
        Cookie login = new Cookie("accessToken", getCookieValue(userLogin.getResponse().getHeaders("set-cookie").get(0), "accessToken"));

        // test valid update
        UpdateUserRequest validRequest = new UpdateUserRequest("newemail@gmail.com", "new name");
        mockMvc.perform(post("/api/v1/users/me")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validRequest))
            .cookie(login))
            .andExpect(status().isOk());
         
        assertDoesNotThrow(() -> {
            User updatedCoach = userService.findByEmail("newemail@gmail.com");
            assertTrue(updatedCoach.getName().equals("new name"));
        });
        this.createdAccounts.add("newemail@gmail.com");

        // test invalid request 
        mockMvc.perform(post("/api/v1/users/me")
            .contentType("application/json")
            .cookie(coachAccess))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid body")));
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
        
        mockMvc.perform(post("/api/v1/users/me/athlete")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validRequest))
            .cookie(athleteAccess))
            .andExpect(status().isOk());

        Athlete ath = (Athlete) userService.findByEmail("data@gmail.com");
        assertTrue(ath.getWeightClass().equals(validRequest.getWeightClass()));
        assertTrue(ath.getWeight() == validRequest.getWeight());
        /*
        TODO get better way to test date
        assertTrue(ath.getDob().getDay() == validRequest.getDob().getDay());
        assertTrue(ath.getDob().getYear() == validRequest.getDob().getYear());
        assertTrue(ath.getDob().getMonth() == validRequest.getDob().getMonth());
        */
        assertTrue(ath.getSquatMax() == validRequest.getSquatMax());
        assertTrue(ath.getBenchMax() == validRequest.getBenchMax());
        assertTrue(ath.getDeadliftMax() == validRequest.getDeadliftMax());
        assertTrue(ath.getHeight() == validRequest.getHeight());

        // test updating coach like an athlete 
        mockMvc.perform(post("/api/v1/users/me/athlete")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(validRequest))
            .cookie(coachAccess))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message", containsString("must be an athlete")));

        // test empty body
        mockMvc.perform(post("/api/v1/users/me")
            .contentType("application/json")
            .cookie(athleteAccess))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid body")));
    }

    @AfterAll
    public void deleteAccounts() {
        this.createdAccounts.forEach((email) -> userService.deleteByEmail(email));
        this.createdPrograms.forEach((id) -> programService.deleteById(id));
    }
}

