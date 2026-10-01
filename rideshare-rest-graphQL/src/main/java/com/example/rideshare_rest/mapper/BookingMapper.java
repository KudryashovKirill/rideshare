package com.example.rideshare_rest.mapper;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_rest.entity.BookingEntity;
import com.example.rideshare_rest.entity.RideEntity;
import com.example.rideshare_rest.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    private final RideMapper rideMapper;
    private final UserMapper userMapper;

    @Autowired
    public BookingMapper(RideMapper rideMapper, UserMapper userMapper) {
        this.rideMapper = rideMapper;
        this.userMapper = userMapper;
    }

    public BookingResponse toResponse(BookingEntity entity) {
        if (entity == null) {
            return null;
        }

        return BookingResponse.builder()
                .id(entity.getId())
                .ride(rideMapper.toResponse(entity.getRide()))
                .passenger(userMapper.toResponse(entity.getPassenger()))
                .status(entity.getStatus())
                .requestedSeats(entity.getRequestedSeats())
                .build();
    }

    public BookingEntity toEntity(BookingRequest request, RideEntity ride, UserEntity passenger) {
        if (request == null) {
            return null;
        }

        return BookingEntity.builder()
                .ride(ride)
                .passenger(passenger)
                .status(request.status())
                .requestedSeats(request.requestedSeats())
                .build();
    }

    public void updateEntityFromRequest(UpdateBookingRequest request, BookingEntity entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setStatus(request.status());
        entity.setRequestedSeats(request.requestedSeats());
    }

    public void updateEntityFromPatch(PatchBookingRequest request, BookingEntity entity) {
        if (request == null || entity == null) {
            return;
        }

        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (request.requestedSeats() != null) {
            entity.setRequestedSeats(request.requestedSeats());
        }
    }
}