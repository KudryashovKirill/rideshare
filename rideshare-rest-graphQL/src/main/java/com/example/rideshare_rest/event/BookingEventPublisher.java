package com.example.rideshare_rest.event;

import com.example.rideshare.events.BookingEvent;
import com.example.rideshare.events.EventEnvelope;
import com.example.rideshare.events.RoutingKeys;
import com.example.rideshare_api_contract.dto.BookingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BookingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingEventPublisher.class);
    private static final String SOURCE = "rideshare-backend";

    private final RabbitTemplate rabbitTemplate;

    public BookingEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCreated(BookingResponse booking) {
        var event = new BookingEvent.Created(
                booking.getId(),
                String.valueOf(booking.getRide().getId()),
                String.valueOf(booking.getPassenger().getId()),
                booking.getStatus().toString(),
                booking.getRequestedSeats()
        );
        send(RoutingKeys.BOOKING_CREATED, event);
    }

    public void publishUpdated(BookingResponse booking) {
        var event = new BookingEvent.Updated(
                booking.getId(),
                booking.getStatus().toString(),
                booking.getRequestedSeats()
        );
        send(RoutingKeys.BOOKING_UPDATED, event);
    }

    public void publishDeleted(BookingResponse booking) {
        var event = new BookingEvent.Deleted(
                booking.getId(),
                booking.getStatus().toString()
        );
        send(RoutingKeys.BOOKING_DELETED, event);
    }

    private void send(String routingKey, BookingEvent event) {
        try {
            String correlationId = UUID.randomUUID().toString();
            EventEnvelope<BookingEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey, correlationId);

            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие бронирования отправлено: {} [eventId={}]", routingKey, envelope.eventMetadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие бронирования {}: {}", routingKey, e.getMessage());
        }
    }
}
