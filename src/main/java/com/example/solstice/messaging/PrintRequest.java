package com.example.solstice.messaging;

public class PrintRequest {

    private String jobId;
    private String attendeeId;
    private String attendeeName;

    public PrintRequest() {
    }

    public PrintRequest(String jobId, String attendeeId, String attendeeName) {
        this.jobId = jobId;
        this.attendeeId = attendeeId;
        this.attendeeName = attendeeName;
    }

    public String getJobId() {
        return jobId;
    }

    public String getAttendeeId() {
        return attendeeId;
    }

    public String getAttendeeName() {
        return attendeeName;
    }
}