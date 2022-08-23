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

    public Program findById(String id) throws ResourceNotFound {
        try {
            return programRepository.findById(id).get();
        } catch (NoSuchElementException e) {
            throw new ResourceNotFound(id);
        }
    }

    public Program assignProgram(String programId, String athleteEmail) throws ResourceNotFound {
        Program program = this.findById(programId);
        program.setAthleteEmail(athleteEmail);

        return programRepository.save(program);
    }

    public Program addWeek(String programId, String coachNotes) throws ResourceNotFound {
        Program program = this.findById(programId);
        program.addWeek(new Week(coachNotes));

        return programRepository.save(program);
    }

    public Program addDay(String programId, String weekId, String coachNotes) throws ResourceNotFound {
        Program program = this.findById(programId);
        program.findWeekById(weekId).addDay(new Day(coachNotes)); 

        return programRepository.save(program);
    }

    public Program addExercise(String programId, String weekId, String dayId, String name, String coachNotes, String videoRef) throws ResourceNotFound {
        Program program = this.findById(programId);
        Exercise exercise = new Exercise(name, coachNotes, videoRef);
        program.findWeekById(weekId).findDayById(dayId).addExercise(exercise);

        return programRepository.save(program);
    }

    public Program addSet(String programId, String weekId, String dayId, String exerciseId, int reps, float percentage, PercentageOptions percentageReference, String coachNotes, boolean videoRequested) throws ResourceNotFound {
        Program program = this.findById(programId);
        Set set = new  Set(reps, percentage, percentageReference, coachNotes, videoRequested);
        program.findWeekById(weekId).findDayById(dayId).findExerciseById(exerciseId).addSet(set);
        
        return programRepository.save(program);
    }

    public Program addSet(String programId, String weekId, String dayId, String exerciseId, float rpe, float weight, String coachNotes, boolean videoRequested) throws ResourceNotFound {
        Program program = this.findById(programId);
        Set set = new  Set(rpe, weight, coachNotes, videoRequested);
        program.findWeekById(weekId).findDayById(dayId).findExerciseById(exerciseId).addSet(set);
        
        return programRepository.save(program);
    }

    public void deleteById(String programId) {
        programRepository.deleteById(programId);
        return;
    }
}

