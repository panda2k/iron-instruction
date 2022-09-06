package com.ironinstruction.api.program;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.ironinstruction.api.errors.ResourceNotFound;

import org.springframework.stereotype.Service;

@Service
public class ProgramService {
    private final ProgramRepository programRepository;

    public ProgramService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public Program createProgram(String coachEmail, String name, String description) {
        Program program = new Program(coachEmail, name, description);

        return programRepository.insert(program);
    }

    public Program updateProgram(String programId, String name, String description) throws ResourceNotFound {
        Program program = findById(programId);

        program.setName(name);
        program.setDescription(description);

        return programRepository.save(program);
    }

    public List<Program> findUserPrograms(String email) {
        return programRepository.findByEmail(email); 
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

    public Program updateSet(String programId, String weekId, String dayId, String exerciseId, String setId, int completedReps, String athleteNotes) throws ResourceNotFound {
        Program program = this.findById(programId);
        Set set = program.findWeekById(weekId).findDayById(dayId).findExerciseById(exerciseId).findSetById(setId);
        set.setCompletedReps(completedReps);
        set.setAthleteNotes(athleteNotes);

        return programRepository.save(program);
    }

    public Program updateDay(String programId, String weekId, String dayId, Day day) throws ResourceNotFound {
        Program program = this.findById(programId);
        ArrayList<Day> days = program.findWeekById(weekId).getDays();
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i).getId().equals(dayId)) {
                days.set(i, day);
                return programRepository.save(program);
            }
        }

        throw new ResourceNotFound(dayId);
    }

    public Program updateWeekAthleteNote(String programId, String weekId, String athleteNote) throws ResourceNotFound {
        Program program = this.findById(programId);
        program.findWeekById(weekId).setAthleteNotes(athleteNote);

        return programRepository.save(program);
    }

    public Program updateDayAthleteNote(String programId, String weekId, String dayId, String athleteNote) throws ResourceNotFound {
        Program program = this.findById(programId);
        program.findWeekById(weekId).findDayById(dayId).setAthleteNotes(athleteNote);

        return programRepository.save(program);
    }

    public Program updateExerciseAthleteNote(String programId, String weekId, String dayId, String exerciseId, String athleteNote) throws ResourceNotFound {
        Program program = this.findById(programId);
        program.findWeekById(weekId).findDayById(dayId).findExerciseById(exerciseId).setAthleteNotes(athleteNote);

        return programRepository.save(program);
    }

    public Program assignSetVideoUrl(String programId, String weekId, String dayId, String exerciseId, String setId, String link) throws ResourceNotFound {
        Program program = this.findById(programId);
        program.findWeekById(weekId).findDayById(dayId).findExerciseById(exerciseId).findSetById(setId).setVideoRef(link);

        return programRepository.save(program);
    }

    public Program assignExerciseVideoUrl(String programId, String weekId, String dayId, String exerciseId, String url) throws ResourceNotFound {
        Program program = this.findById(programId);
        program.findWeekById(weekId).findDayById(dayId).findExerciseById(exerciseId).setVideoRef(url);

        return programRepository.save(program);
    }

    public void deleteById(String programId) {
        programRepository.deleteById(programId);
        return;
    }
}

