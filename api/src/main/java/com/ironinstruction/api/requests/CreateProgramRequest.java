package com.ironinstruction.api.requests;

public class CreateProgramRequest {
    private String name;
    private String description;
    private boolean template;

    public CreateProgramRequest() { }

    public CreateProgramRequest(String name, String description, boolean template) {
        this.name = name;
        this.description = description;
        this.template = template;
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

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public boolean getTemplate() {
        return this.template;
    }
}
