package com.example.rideshare_rest.event;

import com.example.rideshare.events.EventEnvelope;
import com.example.rideshare.events.RideEvent;
import com.example.rideshare.events.RoutingKeys;
import com.example.rideshare_api_contract.dto.RideResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RideEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RideEventPublisher.class);
    private static final String SOURCE = "rideshare-backend";

    private final RabbitTemplate rabbitTemplate;

    public RideEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCreated(RideResponse ride) {
        var event = new RideEvent.Created(
                ride.getId(),
                ride.getDriver().getId(),
                ride.getDepartureCity(),
                ride.getArrivalCity(),
                ride.getDepartureTime(),
                ride.getArrivalTime(),
                ride.getTotalSeats(),
                ride.getFreeSeats(),
                ride.getStatus().toString(),
                ride.getPrice()
        );
        send(RoutingKeys.RIDE_CREATED, event);
    }

    public void publishUpdated(RideResponse ride) {
        var event = new RideEvent.Updated(
                ride.getId(),
                ride.getDepartureCity(),
                ride.getArrivalCity(),
                ride.getDepartureTime(),
                ride.getArrivalTime(),
                ride.getTotalSeats(),
                ride.getFreeSeats(),
                ride.getStatus().toString(),
                ride.getPrice()
        );
        send(RoutingKeys.RIDE_UPDATED, event);
    }

    public void publishDeleted(RideResponse ride) {
        var event = new RideEvent.Deleted(
                ride.getId(),
                ride.getDriver().getId(),
                ride.getDepartureCity(),
                ride.getArrivalCity(),
                ride.getDepartureTime(),
                ride.getArrivalTime()
        );
        send(RoutingKeys.RIDE_DELETED, event);
    }

    private void send(String routingKey, RideEvent event) {
        try {
            String correlationId = UUID.randomUUID().toString();
            EventEnvelope<RideEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey, correlationId);

            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие поездки отправлено: {} [eventId={}]", routingKey, envelope.eventMetadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие поездки {}: {}", routingKey, e.getMessage());
        }
    }
}
