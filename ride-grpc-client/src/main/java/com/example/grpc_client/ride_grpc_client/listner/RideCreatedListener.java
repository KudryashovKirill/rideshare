package com.example.grpc_client.ride_grpc_client.listner;

import com.example.grpc_client.ride_grpc_client.config.RabbitMQConfig;
import com.example.grpc_client.ride_grpc_client.publisher.EnrichmentEventPublisher;
import com.example.rideshare.events.EventMetadata;
import com.example.rideshare.events.RideEvent;
import com.example.rideshare.grpc.AnalyzeRideRequest;
import com.example.rideshare.grpc.RideAdditionGrpc;
import com.example.rideshare.grpc.RideAnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Component
public class RideCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(RideCreatedListener.class);
    private static final String CORRELATION_ID_KEY = "correlationId";

    private final RideAdditionGrpc.RideAdditionBlockingStub analyticsStub;
    private final EnrichmentEventPublisher enrichmentPublisher;
    private final JsonMapper jsonMapper;

    public RideCreatedListener(RideAdditionGrpc.RideAdditionBlockingStub analyticsStub,
                               EnrichmentEventPublisher enrichmentPublisher,
                               JsonMapper jsonMapper) {
        this.analyticsStub = analyticsStub;
        this.enrichmentPublisher = enrichmentPublisher;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.ENRICHMENT_QUEUE, messageConverter = "")
    public void handleRideCreated(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            JsonNode metaNode = root.get("eventMetadata");
            EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);
            String correlationId = metadata.correlationId();
            MDC.put(CORRELATION_ID_KEY, correlationId);

            JsonNode payloadNode = root.get("payload");
            RideEvent.Created rideCreated = jsonMapper.treeToValue(payloadNode, RideEvent.Created.class);

            log.info("Получено событие ride.created для поездки ID={}", rideCreated.id());

            AnalyzeRideRequest grpcRequest = AnalyzeRideRequest.newBuilder()
                    .setRideId(String.valueOf(rideCreated.id()))
                    .setDepartureCity(rideCreated.departureCity())
                    .setArrivalCity(rideCreated.arrivalCity())
                    .setPriceDeclared(rideCreated.price())
                    .setDepartureTime(rideCreated.departureTime() != null ? rideCreated.departureTime().toString() : "")
                    .setTotalSeats(rideCreated.totalSeats())
                    .build();

            log.info("Вызов gRPC: RideAnalytics.AnalyzeRide(rideId={})", rideCreated.id());
            RideAnalysisResponse grpcResponse = analyticsStub.additionRide(grpcRequest);

            log.info("gRPC ответ получен успешно");

            RideEvent.Enriched enrichedEvent = new RideEvent.Enriched(
                    UUID.fromString(grpcResponse.getRideId()),
                    grpcResponse.getEstimatedDistanceKm(),
                    grpcResponse.getRecommendedPrice(),
                    grpcResponse.getPriceDeviation(),
                    grpcResponse.getRouteDifficulty()
            );

            enrichmentPublisher.publishEnriched(enrichedEvent, correlationId);

        } catch (io.grpc.StatusRuntimeException e) {
            log.error("gRPC ошибка при обогащении поездки: {} ({})", e.getStatus().getDescription(), e.getStatus().getCode());
            throw new RuntimeException("gRPC-вызов завершился ошибкой", e);
        } catch (Exception e) {
            log.error("Ошибка обработки события ride.created: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать событие ride.created", e);
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}