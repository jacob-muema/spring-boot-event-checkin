package com.example.solstice.service;

import com.example.solstice.messaging.PrintRequest;
import com.example.solstice.messaging.PrintRequestProducer;
import com.example.solstice.model.Attendee;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CheckInService {

    private final PrintRequestProducer printRequestProducer;

    private final Map<String, Attendee> attendees = new HashMap<>();

    public CheckInService(PrintRequestProducer printRequestProducer) {

        this.printRequestProducer = printRequestProducer;

        attendees.put(
                "A001",
                new Attendee("A001", "Jacob Muema", "NOT_CHECKED_IN")
        );

        attendees.put(
                "A002",
                new Attendee("A002", "Jane Doe", "NOT_CHECKED_IN")
        );

        attendees.put(
                "A003",
                new Attendee("A003", "John Smith", "NOT_CHECKED_IN")
        );
    }

    public Attendee checkIn(String attendeeId) {

        Attendee attendee = attendees.get(attendeeId);

        if (attendee == null) {
            throw new RuntimeException("Attendee not found");
        }

        // Prevent duplicate printing while pending
        if ("PENDING".equals(attendee.getStatus())) {

            System.out.println(
                    "Duplicate scan: " + attendeeId
                            + " already has a pending print job."
            );

            return attendee;
        }

        // Prevent duplicate printing after successful check-in
        if ("CHECKED_IN".equals(attendee.getStatus())) {

            System.out.println(
                    "Duplicate scan: " + attendeeId
                            + " is already checked in."
            );

            return attendee;
        }

        // Mark attendee as pending
        attendee.setStatus("PENDING");

        // Create unique print job
        String jobId = UUID.randomUUID().toString();

        PrintRequest request = new PrintRequest(
                jobId,
                attendee.getAttendeeId(),
                attendee.getName()
        );

        // Publish print request to RabbitMQ
        printRequestProducer.sendPrintRequest(request);

        System.out.println(
                "Print request sent for attendee: " + attendeeId
        );

        System.out.println(
                "Attendee " + attendeeId + " is now PENDING."
        );

        return attendee;
    }

    // Called when the printer webhook confirms successful printing
    public Attendee markAsCheckedIn(String attendeeId) {

        Attendee attendee = attendees.get(attendeeId);

        if (attendee == null) {
            throw new RuntimeException("Attendee not found");
        }

        attendee.setStatus("CHECKED_IN");

        System.out.println(
                "Attendee " + attendeeId
                        + " is now CHECKED_IN."
        );

        return attendee;
    }
}
