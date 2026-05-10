package com.hospital.appointment.event;

import com.hospital.appointment.config.RabbitConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AppointmentEventPublisherTest {

    @Test
    void publishCompleted_sends_to_exchange_with_routing_key() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        AppointmentEventPublisher publisher = new AppointmentEventPublisher(template);

        AppointmentCompletedEvent event = AppointmentCompletedEvent.builder()
                .appointmentId(1L).patientId(2L).doctorId(3L).completedAt(Instant.now()).build();

        publisher.publishCompleted(event);

        verify(template).convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_COMPLETED, event);
    }
}
