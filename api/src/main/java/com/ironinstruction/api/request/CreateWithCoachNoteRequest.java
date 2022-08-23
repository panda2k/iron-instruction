package com.ironinstruction.api.request;

public class CreateWithCoachNoteRequest {
    private String coachNote; 

    public CreateWithCoachNoteRequest() { }

    public CreateWithCoachNoteRequest(String coachNote) {
        this.coachNote = coachNote;
    }

    public void setCoachNote(String coachNote) {
        this.coachNote = coachNote;
    }

    public String getCoachNote() {
        return this.coachNote;
    }
}
