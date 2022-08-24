package com.ironinstruction.api.requests;

public class FinishSetRequest {
    private int repsDone;
    private String athleteNotes;
    
    public FinishSetRequest() { }

    public FinishSetRequest(int repsDone, String athleteNotes) {
        this.repsDone = repsDone;
        this.athleteNotes = athleteNotes;
    }

    public void setRepsDone(int repsDone) {
        this.repsDone = repsDone;
    }

    public int getRepsDone() {
        return this.repsDone;
    }

    public void setAthleteNotes(String athleteNotes) {
        this.athleteNotes = athleteNotes;
    }

    public String getAthleteNotes() {
        return this.athleteNotes;
    }
}
