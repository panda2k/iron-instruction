package com.ironinstruction.api.program;

import java.util.NoSuchElementException;

import com.ironinstruction.api.errors.ResourceNotFound;
import com.ironinstruction.api.request.AssignProgramRequest;
import com.ironinstruction.api.request.CreateWithCoachNoteRequest;
import com.ironinstruction.api.requests.CreateProgramRequest;
import com.ironinstruction.api.responses.VideoLinkResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramController {
    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService; 
    }

    @PostMapping("")
    public Program createProgram(@RequestBody CreateProgramRequest request) {
        return programService.createProgram(
            (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), // JWT token's associated email
            request.getName(),
            request.getDescription(),
            request.getTemplate()
        );
    }

    @GetMapping("/{programId}") 
    public Program getProgram(@PathVariable String programId) throws ResourceNotFound {
        return programService.findProgramById(programId);
    }

    @PostMapping("/{programId}/assign")
    public Program assignProgram(@PathVariable String programId, @RequestBody AssignProgramRequest request) throws ResourceNotFound {
        return programService.assignProgram(
            programId,
            request.getEmail()
        );
    }

    @PostMapping("/{programId}/week")
    public Program createWeek(@PathVariable String programId, @RequestBody CreateWithCoachNoteRequest request) throws ResourceNotFound {
        return programService.addWeek(programId, request.getCoachNote());
    }
/*
    @GetMapping("/{programId}/set/{setId}/video") 
    public VideoLinkResponse (@PathVariable String programId, @PathVariable String setId)  {
         
    }

    @GetMapping("/{programId}/set/{setId}/video/upload")
    public VideoLinkResponse getNewVideoLink(@PathVariable String programId, @PathVariable String setId) {
        
    }*/
}

