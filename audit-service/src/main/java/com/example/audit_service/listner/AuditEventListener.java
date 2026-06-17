package com.example.audit_service.listner;

import com.example.audit_service.model.AuditEntry;
import com.example.audit_service.storage.AuditStorage;
import com.example.rideshare.events.BookingEvent;
import com.example.rideshare.events.EventMetadata;
import com.example.rideshare.events.RideEvent;
import com.example.rideshare.events.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

/**
 * Единый слушатель всех доменных событий из RabbitMQ.
 */
@Component
public class AuditEventListener {
    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditStorage auditStorage;
    private final JsonMapper jsonMapper;

    @Autowired
    public AuditEventListener(AuditStorage auditStorage, JsonMapper jsonMapper) {
        this.auditStorage = auditStorage;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = "q.audit.events", messageConverter = "")
    public void handleEvent(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            JsonNode metaNode = root.get("eventMetadata");
            EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

            if (auditStorage.isDuplicate(metadata.eventId())) {
                log.warn("Дубликат события пропущен: eventId={}", metadata.eventId());
                return;
            }
            JsonNode payloadNode = root.get("payload");
            String description = buildDescription(metadata.eventType(), payloadNode);

            AuditEntry entry = auditStorage.save(new AuditEntry(
                    0,
                    metadata.eventId(),
                    metadata.eventType(),
                    metadata.source(),
                    metadata.correlationId(),
                    metadata.timestamp(),
                    Instant.now(),
                    description
            ));

            log.info("[AUDIT #{}] {} | {}", entry.sequenceNumber(), metadata.eventType(), description);
        } catch (Exception e) {
            log.error("Ошибка обработки события: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать событие", e);
        }
    }

    /**
     * Формирует человеко читаемое описание события для аудит-лога.
     */

    private String buildDescription(String eventType, JsonNode payloadNode) throws Exception {
        return switch (eventType) {
            case "ride.created" -> {
                RideEvent.Created e = jsonMapper.treeToValue(payloadNode, RideEvent.Created.class);
                yield String.format("Создана поездка из \"%s\" в \"%s\" , время отправления %s, время прибытия %s. Оставшихся мест %d",
                        e.departureCity(), e.arrivalCity(), e.departureTime(), e.arrivalTime(), e.freeSeats());
            }
            case "ride.updated" -> {
                RideEvent.Updated e = jsonMapper.treeToValue(payloadNode, RideEvent.Updated.class);
                yield String.format("Обновлена поездка id=%d ", e.id());
            }
            case "ride.deleted" -> {
                RideEvent.Deleted e = jsonMapper.treeToValue(payloadNode, RideEvent.Deleted.class);
                yield String.format("Удалена поездка id=%d ", e.id());
            }
            case "ride.enriched" -> {
                RideEvent.Enriched e = jsonMapper.treeToValue(payloadNode, RideEvent.Enriched.class);
                yield String.format("Сформирована дополнительная информация о поездке id=%d", e.rideId());
            }
            case "user.created" -> {
                UserEvent.Created e = jsonMapper.treeToValue(payloadNode, UserEvent.Created.class);
                yield String.format("Создан пользователь «%s»",
                        e.fullName());
            }
            case "user.updated" -> {
                UserEvent.Updated e = jsonMapper.treeToValue(payloadNode, UserEvent.Updated.class);
                yield String.format("Обновлен пользователь id=%d",
                        e.id());
            }
            case "user.deleted" -> {
                UserEvent.Deleted e = jsonMapper.treeToValue(payloadNode, UserEvent.Deleted.class);
                yield String.format("Удалён пользователь id=%d)",
                        e.id());
            }
            case "booking.created" -> {
                BookingEvent.Created e = jsonMapper.treeToValue(payloadNode, BookingEvent.Created.class);
                yield String.format("Создано бронирование на поездку с id=%s. Забронированных мест %d",
                        e.rideId(), e.requestedSeats());
            }
            case "booking.updated" -> {
                BookingEvent.Updated e = jsonMapper.treeToValue(payloadNode, BookingEvent.Updated.class);
                yield String.format("Обновлено бронирование с id=%d",
                        e.id());
            }
            case "booking.deleted" -> {
                BookingEvent.Deleted e = jsonMapper.treeToValue(payloadNode, BookingEvent.Deleted.class);
                yield String.format("Удалено бронирование id=%d",
                        e.id());
            }
            default -> "Неизвестное событие: " + eventType;
        };
    }
}
