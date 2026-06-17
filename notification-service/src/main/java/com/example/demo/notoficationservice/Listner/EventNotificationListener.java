package com.example.demo.notoficationservice.Listner;

import com.example.demo.notoficationservice.websocket.NotificationWebSocketHandler;
import com.example.rideshare.events.BookingEvent;
import com.example.rideshare.events.EventMetadata;
import com.example.rideshare.events.RideEvent;
import com.example.rideshare.events.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Слушатель всех доменных событий из RabbitMQ.
 *
 * Получает события из очереди q.notifications.all (binding "#"),
 * формирует человекочитаемое JSON-уведомление и рассылает
 * всем подключённым WebSocket-клиентам через NotificationWebSocketHandler.
 *
 * Дедупликация — по eventId (на случай повторной доставки RabbitMQ).
 */
@Component
public class EventNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(EventNotificationListener.class);

    private final NotificationWebSocketHandler webSocketHandler;
    private final JsonMapper jsonMapper;

    /** Набор обработанных eventId для дедупликации. */
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public EventNotificationListener(NotificationWebSocketHandler webSocketHandler,
                                     JsonMapper jsonMapper) {
        this.webSocketHandler = webSocketHandler;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = "q.notifications.all", messageConverter = "")
    public void handleEvent(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            JsonNode metaNode = root.get("eventMetadata");
            EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

            if (!processedEventIds.add(metadata.eventId())) {
                log.warn("Дубликат уведомления пропущен: eventId={}", metadata.eventId());
                return;
            }

            JsonNode payloadNode = root.get("payload");
            String title = buildTitle(metadata.eventType());
            String description = buildDescription(metadata.eventType(), payloadNode);
            String icon = resolveIcon(metadata.eventType());
            String level = resolveLevel(metadata.eventType());

            String notificationJson = jsonMapper.writeValueAsString(
                    new NotificationPayload(
                            "NOTIFICATION",
                            metadata.eventId(),
                            metadata.eventType(),
                            title,
                            description,
                            icon,
                            level,
                            metadata.source(),
                            metadata.correlationId(),
                            metadata.timestamp().toString(),
                            Instant.now().toString()
                    )
            );

            webSocketHandler.broadcast(notificationJson);

            log.info("[NOTIFY] {} | {} (клиентов: {})",
                    metadata.eventType(), description, webSocketHandler.getActiveConnectionCount());

        } catch (Exception e) {
            log.error("Ошибка обработки события для уведомлений: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать событие", e);
        }
    }


    private String buildTitle(String eventType) {
        return switch (eventType) {
            case "ride.created"    -> "Новая поездка";
            case "ride.updated"    -> "Поездка изменена";
            case "ride.deleted"    -> "Поездка отменена";
            case "ride.enriched"   -> "Аналитика стоимости";
            case "user.created"    -> "Регистрация";
            case "user.updated"    -> "Профиль обновлен";
            case "user.deleted"    -> "Аккаунт удален";
            case "booking.created" -> "Новое бронирование";
            case "booking.updated" -> "Бронирование изменено";
            case "booking.deleted" -> "Бронирование отменено";
            default                -> "Системное уведомление";
        };
    }

    private String buildDescription(String eventType, JsonNode payload) {
        try {
            return switch (eventType) {
                case "ride.created" -> {
                    RideEvent.Created e = jsonMapper.treeToValue(payload, RideEvent.Created.class);
                    yield "Создан маршрут из \"%s\" в \"%s\". Мест: %d, Цена: %d руб.".formatted(
                            e.departureCity(), e.arrivalCity(), e.freeSeats(), e.price());
                }
                case "ride.updated" -> {
                    RideEvent.Updated e = jsonMapper.treeToValue(payload, RideEvent.Updated.class);
                    yield "Обновлены данные поездки id=%d (%s -> %s)".formatted(
                            e.id(), e.departureCity(), e.arrivalCity());
                }
                case "ride.deleted" -> {
                    RideEvent.Deleted e = jsonMapper.treeToValue(payload, RideEvent.Deleted.class);
                    yield "Поездка id=%d из \"%s\" в \"%s\" отменена водителем".formatted(
                            e.id(), e.departureCity(), e.arrivalCity());
                }
                case "ride.enriched" -> {
                    RideEvent.Enriched e = jsonMapper.treeToValue(payload, RideEvent.Enriched.class);
                    yield "Анализ поездки id=%d: Дистанция %d км, Рекомендованная цена: %d руб. (%s). Сложность: %s".formatted(
                            e.rideId(), e.estimatedDistanceKm(), e.recommendedPrice(),
                            e.priceDeviation(), e.routeDifficulty());
                }
                case "user.created" -> {
                    UserEvent.Created e = jsonMapper.treeToValue(payload, UserEvent.Created.class);
                    yield "В системе зарегистрирован новый пользователь: %s".formatted(e.fullName());
                }
                case "user.updated" -> {
                    UserEvent.Updated e = jsonMapper.treeToValue(payload, UserEvent.Updated.class);
                    yield "Пользователь id=%d обновил личные данные".formatted(e.id());
                }
                case "user.deleted" -> {
                    UserEvent.Deleted e = jsonMapper.treeToValue(payload, UserEvent.Deleted.class);
                    yield "Пользователь id=%d удалил свой профиль".formatted(e.id());
                }
                case "booking.created" -> {
                    BookingEvent.Created e = jsonMapper.treeToValue(payload, BookingEvent.Created.class);
                    yield "Забронировано мест: %d на поездку id=%s".formatted(
                            e.requestedSeats(), e.rideId());
                }
                case "booking.updated" -> {
                    BookingEvent.Updated e = jsonMapper.treeToValue(payload, BookingEvent.Updated.class);
                    yield "Изменены параметры бронирования id=%d".formatted(e.id());
                }
                case "booking.deleted" -> {
                    BookingEvent.Deleted e = jsonMapper.treeToValue(payload, BookingEvent.Deleted.class);
                    yield "Отменено бронирование id=%d".formatted(e.id());
                }
                default -> "Выполнено действие: " + eventType;
            };
        } catch (Exception e) {
            return "Событие " + eventType + " (ошибка десериализации payload)";
        }
    }

    private String resolveIcon(String eventType) {
        return switch (eventType) {
            case "ride.created"    -> "car-plus";
            case "ride.updated"    -> "car-edit";
            case "ride.deleted"    -> "car-remove";
            case "ride.enriched"   -> "calculator-trending";
            case "user.created"    -> "account-plus";
            case "user.updated"    -> "account-edit";
            case "user.deleted"    -> "account-remove";
            case "booking.created" -> "ticket-confirmation";
            case "booking.deleted" -> "ticket-cancel";
            default                -> "bell-ring";
        };
    }

    private String resolveLevel(String eventType) {
        return switch (eventType) {
            case "ride.deleted", "user.deleted", "booking.deleted" -> "warning";
            case "ride.enriched"                                   -> "info";
            default                                                -> "success";
        };
    }

    /**
     * Payload уведомления для передачи через WebSocket-соединение.
     */
    record NotificationPayload(
            String type,
            String eventId,
            String eventType,
            String title,
            String description,
            String icon,
            String level,
            String source,
            String correlationId,
            String eventTimestamp,
            String receivedAt
    ) {}
}

