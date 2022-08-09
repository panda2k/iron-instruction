package com.ironinstruction.api.errors;

public class ResourceNotFound extends Exception{
    private final String resourceId;
    public ResourceNotFound(String resourceId) {
        super();
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }
}
