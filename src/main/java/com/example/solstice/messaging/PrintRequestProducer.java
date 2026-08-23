package com.example.solstice.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.solstice.config.RabbitMQConfig;

@Service
public class PrintRequestProducer {

    private final RabbitTemplate rabbitTemplate;

    public PrintRequestProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendPrintRequest(PrintRequest request) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PRINT_QUEUE,
                request
        );

        System.out.println(
                "Print request sent for attendee: "
                        + request.getAttendeeId()
        );
    }
}