package com.example.solstice.model;

public class Attendee {

    private String attendeeId;
    private String name;
    private String status;

    public Attendee(String attendeeId, String name, String status) {
        this.attendeeId = attendeeId;
        this.name = name;
        this.status = status;
    }

    public String getAttendeeId() {
        return attendeeId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}