package com.hospital.billing.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "hms.appointment.exchange";
    public static final String QUEUE_BILLING = "hms.appointment.completed.billing";
    public static final String ROUTING_KEY_COMPLETED = "appointment.completed";

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue billingQueue() {
        return new Queue(QUEUE_BILLING, true);
    }

    @Bean
    public Binding billingBinding(Queue billingQueue, TopicExchange appointmentExchange) {
        return BindingBuilder.bind(billingQueue).to(appointmentExchange).with(ROUTING_KEY_COMPLETED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleMessageListenerContainer listenerContainer(ConnectionFactory cf,
                                                            MessageConverter converter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(cf);
        container.setQueueNames(QUEUE_BILLING);
        container.setMessageConverter(converter);
        return container;
    }
}
