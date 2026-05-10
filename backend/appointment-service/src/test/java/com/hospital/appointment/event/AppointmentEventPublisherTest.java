package com.hospital.appointment.event;

import com.hospital.appointment.config.RabbitConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentEventPublisherTest {

    private AppointmentCompletedEvent sampleEvent() {
        return AppointmentCompletedEvent.builder()
                .appointmentId(1L).patientId(2L).doctorId(3L).completedAt(Instant.now()).build();
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<RabbitTemplate> providerOf(RabbitTemplate template) {
        ObjectProvider<RabbitTemplate> provider = (ObjectProvider<RabbitTemplate>) mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(template);
        return provider;
    }

    @Test
    void publishCompleted_sends_to_exchange_when_template_available() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        AppointmentEventPublisher publisher = new AppointmentEventPublisher(providerOf(template));
        AppointmentCompletedEvent event = sampleEvent();

        publisher.publishCompleted(event);

        verify(template).convertAndSend(
                eq(RabbitConfig.EXCHANGE),
                eq(RabbitConfig.ROUTING_KEY_COMPLETED),
                eq((Object) event));
    }

    @Test
    void publishCompleted_no_op_when_template_missing() {
        AppointmentEventPublisher publisher = new AppointmentEventPublisher(providerOf(null));

        publisher.publishCompleted(sampleEvent());
        // No exception expected; nothing to verify on a null template.
    }

    @Test
    void publishCompleted_swallows_send_failure() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        doThrow(new AmqpException("broker down"))
                .when(template).convertAndSend(anyString(), anyString(), any(Object.class));
        AppointmentEventPublisher publisher = new AppointmentEventPublisher(providerOf(template));

        publisher.publishCompleted(sampleEvent()); // must not throw
        verify(template).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void publishCompleted_does_not_call_template_when_provider_empty() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        AppointmentEventPublisher publisher = new AppointmentEventPublisher(providerOf(null));

        publisher.publishCompleted(sampleEvent());
        verify(template, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}
