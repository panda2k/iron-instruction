package com.ironinstruction.api.program;

import java.util.NoSuchElementException;

import com.ironinstruction.api.errors.ResourceNotFound;

import org.springframework.stereotype.Service;

@Service
public class ProgramService {
    private final ProgramRepository programRepository;

    public ProgramService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public Program createProgram(String coachEmail, String name, String description, boolean template) {
        Program program = new Program(coachEmail, name, description, template);

        return programRepository.insert(program);
    }

    public Program findProgramById(String id) throws ResourceNotFound {
        try {
            return programRepository.findById(id).get();
        } catch (NoSuchElementException e) {
            throw new ResourceNotFound(id);
        }
    }

    public Program assignProgram(String programId, String athleteEmail) throws ResourceNotFound {
        Program program = this.findProgramById(programId);
        program.setAthleteEmail(athleteEmail);

        return programRepository.save(program);
    }

    public Program addWeek(String programId, String coachNotes) throws ResourceNotFound {
        Program program = this.findProgramById(programId);
        program.addWeek(new Week(coachNotes));

        return programRepository.save(program);
    }

    /*public Program addDay(String programId, String dayId, String coachNotes) throws ResourceNotFound {
        Program program = this.findProgramById(programId); 
    }*/
}

