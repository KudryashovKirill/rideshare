package com.example.grpc_server.ride_grpc.service;

import com.example.rideshare.grpc.AnalyzeRideRequest;
import com.example.rideshare.grpc.RideAdditionGrpc;
import com.example.rideshare.grpc.RideAnalysisResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class RideAnalyticsServiceImpl extends RideAdditionGrpc.RideAdditionImplBase {

    private static final Logger log = LoggerFactory.getLogger(RideAnalyticsServiceImpl.class);

    @Override
    public void additionRide(AnalyzeRideRequest request,
                            StreamObserver<RideAnalysisResponse> responseObserver) {

        log.info("gRPC запрос: анализ поездки ID={} [{} -> {}], заявленная цена: {} руб.",
                request.getRideId(), request.getDepartureCity(), request.getArrivalCity(), request.getPriceDeclared());


        int distanceKm = calculateDistance(request.getDepartureCity(), request.getArrivalCity());
        int recommendedPrice = calculateRecommendedPrice(distanceKm, request.getTotalSeats(), request.getDepartureTime());

        String priceDeviation = evaluatePriceDeviation(request.getPriceDeclared(), recommendedPrice);
        String routeDifficulty = evaluateRouteDifficulty(distanceKm, request.getDepartureCity());

        RideAnalysisResponse response = RideAnalysisResponse.newBuilder()
                .setRideId(request.getRideId())
                .setEstimatedDistanceKm(distanceKm)
                .setRecommendedPrice(recommendedPrice)
                .setPriceDeviation(priceDeviation)
                .setRouteDifficulty(routeDifficulty)
                .build();

        log.info("gRPC ответ сформирован: ID={}, дистанция={} км, рек.цена={} руб, оценка: [{}], трасса: [{}]",
                response.getRideId(), distanceKm, recommendedPrice, priceDeviation, routeDifficulty);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    /**
     * Считает расстояние между городами на основе разницы длин их названий.
     * Гарантирует стабильный и логичный результат для одинаковых пар городов.
     */
    private int calculateDistance(String from, String to) {
        if (from.equalsIgnoreCase(to)) return 15;

        int delta = Math.abs(from.length() - to.length());
        int baseDistance = (delta == 0) ? 250 : delta * 95;

        return Math.max(50, Math.min(baseDistance, 1500));
    }

    /**
     * Считает рекомендованную цену на основе километража, мест и ночного тарифа.
     */
    private int calculateRecommendedPrice(int distanceKm, int totalSeats, String departureTimeStr) {
        int costPerKm = 5;

        if (totalSeats > 4) costPerKm -= 1;

        int basePrice = distanceKm * costPerKm;

        try {
            if (departureTimeStr != null && !departureTimeStr.isBlank()) {
                LocalDateTime dateTime = LocalDateTime.parse(departureTimeStr);
                int hour = dateTime.getHour();
                if (hour >= 0 && hour < 6) {
                    basePrice = (int) (basePrice * 1.2);
                }
            }
        } catch (Exception e) {
            log.warn("Не удалось орпеделить дату отправления '{}' для тарификации", departureTimeStr);
        }

        return basePrice;
    }

    /**
     * Сравнивает заявленную водителем цену с рекомендованной.
     */
    private String evaluatePriceDeviation(int declared, int recommended) {
        if (declared < recommended * 0.8) {
            return "Уценка";
        } else if (declared > recommended * 1.25) {
            return "Переплата";
        }
        return "Оптимально";
    }

    /**
     * Определяет сложность маршрута по километражу.
     */
    private String evaluateRouteDifficulty(int distanceKm, String departureCity) {
        if (distanceKm > 400) {
            return "Трудно";
        }
        if (!departureCity.toLowerCase().contains("москва") ||
                !departureCity.toLowerCase().contains("санкт-петербург")) {
            return "Плохие дороги";
        }
        return "Легко";
    }
}
