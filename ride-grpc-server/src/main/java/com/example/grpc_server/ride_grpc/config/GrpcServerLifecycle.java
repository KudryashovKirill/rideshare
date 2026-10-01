package com.example.grpc_server.ride_grpc.config;

import com.example.grpc_server.ride_grpc.service.RideAnalyticsServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class GrpcServerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    private Server server;
    private boolean running = false;

    /**
     * Автоматический запуск gRPC-сервера.
     * Регистрируем здесь нашу будущую реализацию сервиса гео- и ценового анализа.
     */
    @Override
    public void start() {
        try {
            server = ServerBuilder.forPort(grpcPort)
                    .addService(new RideAnalyticsServiceImpl())
                    .build()
                    .start();

            running = true;
            log.info("gRPC-сервер аналитики Rideshare запущен на порту {}", grpcPort);
            log.info("Сервис: RideAnalytics.AnalyzeRide()");

        } catch (IOException e) {
            throw new RuntimeException("Не удалось запустить gRPC-сервер аналитики на порту " + grpcPort, e);
        }
    }

    /**
     * Корректная остановка (Graceful shutdown).
     * Перестаем принимать новые RPC-запросы, но даем завершиться тем, которые уже в обработке.
     */
    @Override
    public void stop() {
        if (server != null) {
            log.info("Остановка gRPC-сервера аналитики");
            server.shutdown();
            running = false;
            log.info("gRPC-сервер аналитики успешно остановлен");
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Фаза запуска. Указываем Integer.MAX_VALUE, чтобы gRPC-сервер начинал слушать сокеты
     * в самый последний момент, когда все остальные Spring-компоненты бэкенда уже полностью готовы к работе.
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
