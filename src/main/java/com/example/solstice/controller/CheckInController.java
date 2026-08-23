package com.example.solstice.controller;

import com.example.solstice.model.Attendee;
import com.example.solstice.service.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping("/{attendeeId}")
    public ResponseEntity<Attendee> checkIn(
            @PathVariable String attendeeId) {

        Attendee attendee = checkInService.checkIn(attendeeId);

        return ResponseEntity.ok(attendee);
    }
}