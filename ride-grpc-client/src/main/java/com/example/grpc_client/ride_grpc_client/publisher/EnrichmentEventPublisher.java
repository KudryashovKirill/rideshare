package com.example.grpc_client.ride_grpc_client.publisher;

import com.example.rideshare.events.EventEnvelope;
import com.example.rideshare.events.RideEvent;
import com.example.rideshare.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EnrichmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentEventPublisher.class);

    private static final String SOURCE = "grpc-enrichment-client";

    private final RabbitTemplate rabbitTemplate;

    public EnrichmentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Заворачивает рекорд Enriched в стандартный конверт EventEnvelope
     * и отправляет в общий Topic Exchange бизнес-событий.
     */
    public void publishEnriched(RideEvent.Enriched enrichedEvent, String correlationId) {
        try {
            EventEnvelope<RideEvent> envelope = EventEnvelope.wrap(
                    enrichedEvent, SOURCE, "ride.enriched", correlationId);

            rabbitTemplate.convertAndSend(
                    RoutingKeys.EXCHANGE,
                    "ride.enriched",
                    envelope);

            log.info("Событие ride.enriched отправлено в шину с сохранением CorrelationID=[{}]", correlationId);

        } catch (Exception e) {
            log.error("Не удалось отправить событие ride.enriched: {}", e.getMessage());
        }
    }
}
