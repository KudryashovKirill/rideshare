package com.example.grpc_client.ride_grpc_client.config;

import com.example.rideshare.grpc.RideAdditionGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    private static final Logger log = LoggerFactory.getLogger(GrpcClientConfig.class);

    @Value("${grpc.client.analytics-server.host:localhost}")
    private String grpcHost;

    @Value("${grpc.client.analytics-server.port:9090}")
    private int grpcPort;

    private ManagedChannel channel;

    /**
     * Создает и настраивает gRPC канал связи (ManagedChannel).
     * Благодаря HTTP/2, этот единый канал эффективно мультиплексирует
     * множество параллельных запросов от нашего RabbitMQ-слушателя.
     */
    @Bean
    public ManagedChannel managedChannel() {
        channel = ManagedChannelBuilder
                .forAddress(grpcHost, grpcPort)
                .usePlaintext()
                .build();

        log.info("gRPC канал для Rideshare Analytics успешно создан на {}:{}", grpcHost, grpcPort);
        return channel;
    }

    /**
     * Создает синхронный клиентский стаб (BlockingStub) на основе сгенерированного .proto класса.
     * Именно этот бин мы будем внедрять (inject) в наш RabbitMQ Listener для вызова метода analyzeRide().
     */
    @Bean
    public RideAdditionGrpc.RideAdditionBlockingStub rideAnalyticsStub(ManagedChannel channel) {
        return RideAdditionGrpc.newBlockingStub(channel);
    }

    /**
     * Безопасное закрытие TCP-соединений при остановке контекста Spring Boot application.
     * Защищает операционную систему от утечки сокетов.
     */
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            log.info("Закрытие gRPC канала клиентского модуля...");
            channel.shutdown();
        }
    }
}
