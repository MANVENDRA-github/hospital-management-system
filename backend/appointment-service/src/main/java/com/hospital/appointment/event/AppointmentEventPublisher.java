package com.hospital.appointment.event;

import com.hospital.appointment.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventPublisher.class);

    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;

    public AppointmentEventPublisher(ObjectProvider<RabbitTemplate> rabbitTemplateProvider) {
        this.rabbitTemplateProvider = rabbitTemplateProvider;
    }

    @Async
    public void publishCompleted(AppointmentCompletedEvent event) {
        RabbitTemplate template = rabbitTemplateProvider.getIfAvailable();
        if (template == null) {
            log.info("[no broker] AppointmentCompletedEvent: appointmentId={}, patientId={}",
                    event.getAppointmentId(), event.getPatientId());
            return;
        }
        try {
            template.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY_COMPLETED,
                    event);
            log.info("Published AppointmentCompletedEvent for appointmentId={}", event.getAppointmentId());
        } catch (Exception ex) {
            log.warn("Failed to publish AppointmentCompletedEvent for appointmentId={}: {}",
                    event.getAppointmentId(), ex.getMessage());
        }
    }
}
