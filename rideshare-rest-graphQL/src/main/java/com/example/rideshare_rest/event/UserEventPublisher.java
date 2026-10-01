package com.example.rideshare_rest.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.example.rideshare.events.EventEnvelope;
import com.example.rideshare.events.RoutingKeys;
import com.example.rideshare.events.UserEvent;
import com.example.rideshare_api_contract.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);
    private static final String SOURCE = "rideshare-backend";

    private final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCreated(UserResponse user) {
        var event = new UserEvent.Created(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getBirthDate()
        );
        send(RoutingKeys.USER_CREATED, event);
    }

    public void publishUpdated(UserResponse user) {
        var event = new UserEvent.Updated(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getBirthDate()
        );
        send(RoutingKeys.USER_UPDATED, event);
    }

    public void publishDeleted(UserResponse user) {
        var event = new UserEvent.Deleted(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getBirthDate()
        );
        send(RoutingKeys.USER_DELETED, event);
    }

    private void send(String routingKey, UserEvent event) {
        try {
            String correlationId = UUID.randomUUID().toString();
            EventEnvelope<UserEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey, correlationId);

            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие пользователя отправлено: {} [eventId={}]", routingKey, envelope.eventMetadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие пользователя {}: {}", routingKey, e.getMessage());
        }
    }
}
