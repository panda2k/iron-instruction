package com.ironinstruction.api.requests;

public class CreateProgramRequest {
    private String name;
    private String description;

    public CreateProgramRequest() { }

    public CreateProgramRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
