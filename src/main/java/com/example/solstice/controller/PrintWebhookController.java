package com.example.solstice.controller;

import com.example.solstice.service.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class PrintWebhookController {

    private final CheckInService checkInService;

    public PrintWebhookController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping("/print-completed")
    public ResponseEntity<?> printCompleted(@RequestBody Map<String, String> payload) {

        String jobId = payload.get("jobId");
        String attendeeId = payload.get("attendeeId");

        System.out.println("=================================");
        System.out.println("PRINT COMPLETION WEBHOOK RECEIVED");
        System.out.println("Job ID: " + jobId);
        System.out.println("Attendee ID: " + attendeeId);
        System.out.println("=================================");

        checkInService.markAsCheckedIn(attendeeId);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Print completion received",
                        "attendeeId", attendeeId,
                        "status", "CHECKED_IN"
                )
        );
    }
}
