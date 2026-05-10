package com.hospital.billing.event;

import com.hospital.billing.config.RabbitConfig;
import com.hospital.billing.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("docker")
public class AppointmentCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentCompletedListener.class);

    private final BillingService billingService;

    public AppointmentCompletedListener(BillingService billingService) {
        this.billingService = billingService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_BILLING)
    public void onAppointmentCompleted(AppointmentCompletedEvent event) {
        log.info("Received AppointmentCompletedEvent for appointmentId={}", event.getAppointmentId());
        billingService.generateBillFromAppointment(event);
    }
}
