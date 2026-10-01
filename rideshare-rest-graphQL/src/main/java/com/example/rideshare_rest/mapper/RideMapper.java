package com.example.rideshare_rest.mapper;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_rest.entity.RideEntity;
import com.example.rideshare_rest.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RideMapper {

    private final UserMapper userMapper;

    @Autowired
    public RideMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public RideResponse toResponse(RideEntity entity) {
        if (entity == null) {
            return null;
        }

        return RideResponse.builder()
                .id(entity.getId())
                .driver(userMapper.toResponse(entity.getDriver()))
                .departureCity(entity.getDepartureCity())
                .arrivalCity(entity.getArrivalCity())
                .departureTime(entity.getDepartureTime())
                .arrivalTime(entity.getArrivalTime())
                .totalSeats(entity.getTotalSeats())
                .freeSeats(entity.getFreeSeats())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .build();
    }

    public RideEntity toEntity(RideRequest request, UserEntity driver) {
        if (request == null) {
            return null;
        }

        return RideEntity.builder()
                .driver(driver)
                .departureCity(request.departureCity())
                .arrivalCity(request.arrivalCity())
                .departureTime(request.departureTime())
                .arrivalTime(request.arrivalTime())
                .totalSeats(request.totalSeats())
                .freeSeats(request.freeSeats())
                .status(RideStatus.ACTIVE)
                .price(request.price())
                .build();
    }

    public void updateEntityFromRequest(UpdateRideRequest request, RideEntity entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setDepartureCity(request.departureCity());
        entity.setArrivalCity(request.arrivalCity());
        entity.setDepartureTime(request.departureTime());
        entity.setArrivalTime(request.arrivalTime());
        entity.setTotalSeats(request.totalSeats());
        entity.setFreeSeats(request.freeSeats());
        entity.setStatus(request.status());
        entity.setPrice(request.price());
    }

    public void updateEntityFromPatch(PatchRideRequest request, RideEntity entity) {
        if (request == null || entity == null) {
            return;
        }

        if (request.departureCity() != null) {
            entity.setDepartureCity(request.departureCity());
        }
        if (request.arrivalCity() != null) {
            entity.setArrivalCity(request.arrivalCity());
        }
        if (request.departureTime() != null) {
            entity.setDepartureTime(request.departureTime());
        }
        if (request.arrivalTime() != null) {
            entity.setArrivalTime(request.arrivalTime());
        }
        if (request.totalSeats() != null) {
            entity.setTotalSeats(request.totalSeats());
        }
        if (request.freeSeats() != null) {
            entity.setFreeSeats(request.freeSeats());
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (request.price() != null) {
            entity.setPrice(request.price());
        }
    }
}