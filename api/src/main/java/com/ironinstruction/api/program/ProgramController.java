package com.ironinstruction.api.program;

import java.util.List;

import com.ironinstruction.api.errors.InvalidRequest;
import com.ironinstruction.api.errors.ResourceNotFound;
import com.ironinstruction.api.requests.AssignProgramRequest;
import com.ironinstruction.api.requests.NoteRequest;
import com.ironinstruction.api.requests.FinishSetRequest;
import com.ironinstruction.api.requests.CreateExerciseRequest;
import com.ironinstruction.api.requests.CreateProgramRequest;
import com.ironinstruction.api.requests.CreateSetRequest;
import com.ironinstruction.api.responses.VideoLinkResponse;
import com.ironinstruction.api.security.SecurityConstants;
import com.ironinstruction.api.user.User;
import com.ironinstruction.api.user.UserService;
import com.ironinstruction.api.user.UserType;
import com.ironinstruction.api.utils.AwsS3Manager;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramController {
    private final AwsS3Manager s3Manager;
    private final ProgramService programService;
    private final UserService userService;

    public ProgramController(ProgramService programService, UserService userService) {
        this.s3Manager = new AwsS3Manager(SecurityConstants.S3_BUCKET_NAME);
        this.programService = programService; 
        this.userService = userService;
    }

    @PostMapping("")
    public Program createProgram(@RequestBody CreateProgramRequest request) throws InvalidRequest {
        if (request.getName().isBlank()) {
            throw new InvalidRequest("name field must not be empty");
        }
        return programService.createProgram(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), // JWT token's associated email
            request.getName(),
            request.getDescription()
        );
    }

    @GetMapping("/user/me")
    public List<Program> getUserPrograms() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return programService.findUserPrograms(userEmail);
    }

    @GetMapping("/{programId}") 
    public Program getProgram(@PathVariable String programId) throws ResourceNotFound {
        return programService.findById(programId);
    }

    @PostMapping("/{programId}")
    public Program updateProgram(@PathVariable String programId, @RequestBody CreateProgramRequest request) throws ResourceNotFound {
        return programService.updateProgram(programId, request.getName(), request.getDescription()); 
    }

    @PostMapping("/{programId}/assign")
    public Program assignProgram(@PathVariable String programId, @RequestBody AssignProgramRequest request) throws ResourceNotFound, InvalidRequest {
        if (!request.getEmail().isBlank()) {
            User user = userService.findByEmail(request.getEmail());
            if (user.getUserType().equals(UserType.COACH)) {
                throw new InvalidRequest("Cannot assign program to coach");
            }
        } 

        return programService.assignProgram(
            programId,
            request.getEmail()
        );
    }

    @PostMapping("/{programId}/weeks")
    public Program createWeek(@PathVariable String programId, @RequestBody NoteRequest request) throws ResourceNotFound {
        return programService.addWeek(programId, request.getNote());
    }

    @DeleteMapping("/{programId}/weeks/{weekId}")
    public Program deleteWeek(@PathVariable String programId, @PathVariable String weekId) throws ResourceNotFound {
        return programService.deleteWeek(programId, weekId);  
    }

    @PostMapping("/{programId}/weeks/{weekId}/notes")
    public Program updateWeekCoachNotes(@PathVariable String programId, @PathVariable String weekId, @RequestBody NoteRequest request) throws ResourceNotFound {
        return programService.updateWeekCoachNote(programId, weekId, request.getNote());
    }

    @PatchMapping("/{programId}/weeks/{weekId}/notes")
    public Program updateWeekAthleteNote(@PathVariable String programId, @PathVariable String weekId, @RequestBody NoteRequest request) throws ResourceNotFound {
        return programService.updateWeekAthleteNote(programId, weekId, request.getNote());
    }

    @PostMapping("/{programId}/weeks/{weekId}/days")
    public Program createDay(@PathVariable String programId, @PathVariable String weekId, @RequestBody NoteRequest request) throws ResourceNotFound {
        return programService.addDay(programId, weekId, request.getNote());
    }

    @DeleteMapping("/{programId}/weeks/{weekId}/days/{dayId}")
    public Program deleteDay(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId) throws ResourceNotFound {
        return programService.deleteDay(programId, weekId, dayId);
    }

    @PatchMapping("/{programId}/weeks/{weekId}/days/{dayId}/notes")
    public Program updateDayAthleteNote(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @RequestBody NoteRequest request) throws ResourceNotFound {
        return programService.updateDayAthleteNote(programId, weekId, dayId, request.getNote());
    }

    @PostMapping("/{programId}/weeks/{weekId}/days/{dayId}/notes")
    public Program updateDayCoachNote(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @RequestBody NoteRequest request) throws ResourceNotFound {
        return programService.updateDayCoachNote(programId, weekId, dayId, request.getNote());
    }

    @PostMapping("/{programId}/weeks/{weekId}/days/{dayId}/exercises")
    public Program createExercise(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @RequestBody CreateExerciseRequest request) throws ResourceNotFound, InvalidRequest {
        if (request.getName().isBlank()) {
            throw new InvalidRequest("Exercise name must not be blank");
        }

        return programService.addExercise(programId, weekId, dayId, request.getName(), request.getVideoRef());
    }

    @PutMapping("/{programId}/weeks/{weekId}/days/{dayId}/exercises/{exerciseId}")
    public Program updateExercise(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @PathVariable String exerciseId, @RequestBody Exercise request) throws ResourceNotFound, InvalidRequest {
        if (request.getName().isBlank()) {
            throw new InvalidRequest("Exercise name must not be blank");
        }

        request.getSets().forEach(set -> {
            if (set.getId().isBlank()) {
                set.generateId();   
            }
        });

        return programService.updateExercise(programId, weekId, dayId, request);
    }

    @DeleteMapping("/{programId}/weeks/{weekId}/days/{dayId}/exercises/{exerciseId}")
    public Program deleteExercise(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @PathVariable String exerciseId) throws ResourceNotFound {
        return programService.deleteExercise(programId, weekId, dayId, exerciseId);
    }

    @GetMapping("/{programId}/weeks/{weekId}/days/{dayId}/exercises/{exerciseId}/video") 
    public VideoLinkResponse getExerciseVideoLink(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @PathVariable String exerciseId) throws ResourceNotFound { 
        String key = programService.findById(programId).findWeekById(weekId).findDayById(dayId).findExerciseById(exerciseId).getVideoRef();
        if (key == null || key.length() == 0) {
            throw new ResourceNotFound(exerciseId);
        }
        return new VideoLinkResponse(this.s3Manager.newPresignedGetUrl(key));
    }
    
    // make it post so only coach can access
    @PostMapping("/{programId}/weeks/{weekId}/days/{dayId}/exercises/{exerciseId}/video/upload") 
    public VideoLinkResponse createExerciseVideoLink(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @PathVariable String exerciseId) throws ResourceNotFound { 
        String existingKey = programService.findById(programId).findWeekById(weekId).findDayById(dayId).findExerciseById(exerciseId).getVideoRef();
        if (existingKey != null && existingKey.length() != 0) {
            this.s3Manager.deleteObject(existingKey);
        }
        String key = exerciseId + ".mp4"; 
        String videoLink = this.s3Manager.newPresignedPutUrl(key);
        programService.assignExerciseVideoUrl(programId, weekId, dayId, exerciseId, key);

        return new VideoLinkResponse(videoLink);
    }

    @PostMapping("/{programId}/weeks/{weekId}/days/{dayId}/exercises/{exerciseId}/sets")
    public Program createSet(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @PathVariable String exerciseId, @RequestBody CreateSetRequest request) throws ResourceNotFound {
        // rpe == -1 when set is designed with reps instead of rpe
        if (request.getRpe() == -1) { 
            return programService.addSet(
                programId,
                weekId,
                dayId,
                exerciseId,
                request.getReps(),
                request.getPercentage(),
                request.getPercentageReference(),
                request.getVideoRequested()
            );
        } else {
            return programService.addSet(
                programId,
                weekId,
                dayId,
                exerciseId,
                request.getRpe(),
                request.getReps(),
                request.getWeight(),
                request.getVideoRequested()
            );
        }
    }

    @PatchMapping("/{programId}/weeks/{weekId}/days/{dayId}/exercises/{exerciseId}/sets/{setId}")
    public Program finishSet(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @PathVariable String exerciseId, @PathVariable String setId, @RequestBody FinishSetRequest request) throws ResourceNotFound {
        return programService.updateSet(programId, weekId, dayId, exerciseId, setId, request.getRepsDone());
    }

    @GetMapping("/{programId}/weeks/{weekId}/days/{dayId}/exercises/{exerciseId}/sets/{setId}/video") 
    public VideoLinkResponse getSetVideoLink(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @PathVariable String exerciseId, @PathVariable String setId) throws ResourceNotFound { 
        String key = programService.findById(programId).findWeekById(weekId).findDayById(dayId).findExerciseById(exerciseId).findSetById(setId).getVideoRef();
        if (key == null || key.length() == 0) {
            throw new ResourceNotFound(setId);
        }
        return new VideoLinkResponse(this.s3Manager.newPresignedGetUrl(key));
    }

    @GetMapping("/{programId}/weeks/{weekId}/days/{dayId}/exercises/{exerciseId}/sets/{setId}/video/upload") 
    public VideoLinkResponse createSetVideoLink(@PathVariable String programId, @PathVariable String weekId, @PathVariable String dayId, @PathVariable String exerciseId, @PathVariable String setId) throws ResourceNotFound { 
        String existingKey = programService.findById(programId).findWeekById(weekId).findDayById(dayId).findExerciseById(exerciseId).findSetById(setId).getVideoRef();
        if (existingKey != null && existingKey.length() != 0) {
            this.s3Manager.deleteObject(existingKey);
        }
        String key = setId + ".mp4"; 
        String url = this.s3Manager.newPresignedPutUrl(key);
        programService.assignSetVideoUrl(programId, weekId, dayId, exerciseId, setId, key);
        return new VideoLinkResponse(url);
    }
}

