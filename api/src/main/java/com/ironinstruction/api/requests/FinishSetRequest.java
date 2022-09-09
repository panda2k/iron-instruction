package com.ironinstruction.api.requests;

public class FinishSetRequest {
    private int repsDone;
    
    public FinishSetRequest() { }

    public FinishSetRequest(int repsDone) {
        this.repsDone = repsDone;
    }

    public void setRepsDone(int repsDone) {
        this.repsDone = repsDone;
    }

    public int getRepsDone() {
        return this.repsDone;
    }
}
