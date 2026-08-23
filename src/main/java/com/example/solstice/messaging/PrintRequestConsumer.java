package com.example.solstice.messaging;

import com.example.solstice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class PrintRequestConsumer {

    private final RestTemplate restTemplate = new RestTemplate();

    @RabbitListener(queues = RabbitMQConfig.PRINT_QUEUE)
    public void processPrintRequest(PrintRequest request) {

        System.out.println("=================================");
        System.out.println("BADGE PRINTER RECEIVED REQUEST");
        System.out.println("Job ID: " + request.getJobId());
        System.out.println("Attendee ID: " + request.getAttendeeId());
        System.out.println("Attendee Name: " + request.getAttendeeName());
        System.out.println("Printing badge...");
        System.out.println("=================================");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        System.out.println("BADGE PRINTED SUCCESSFULLY");

        Map<String, String> webhookPayload = new HashMap<>();
        webhookPayload.put("jobId", request.getJobId());
        webhookPayload.put("attendeeId", request.getAttendeeId());

        String webhookUrl =
                "http://localhost:8081/api/webhook/print-completed";

        try {

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            webhookUrl,
                            webhookPayload,
                            String.class
                    );

            System.out.println("=================================");
            System.out.println("WEBHOOK SENT TO CHECK-IN SERVICE");
            System.out.println("Webhook response: " + response.getBody());
            System.out.println("=================================");

        } catch (Exception e) {

            System.out.println("=================================");
            System.out.println("WEBHOOK FAILED");
            System.out.println("Error: " + e.getMessage());
            System.out.println("=================================");
        }
    }
}
