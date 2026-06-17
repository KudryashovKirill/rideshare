package com.example.rideshare_rest.service;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_api_contract.exceptions.ResourceNotFoundException;
import com.example.rideshare_rest.event.BookingEventPublisher;
import com.example.rideshare_rest.event.RideEventPublisher;
import com.example.rideshare_rest.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class RideService {
    private final InMemoryStorage storage;
    private final UserService userService;
    private final RideEventPublisher eventPublisher;

    @Autowired
    public RideService(InMemoryStorage storage, UserService userService, RideEventPublisher eventPublisher) {
        this.storage = storage;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    public PagedResponse<RideResponse> getAllRides(Long driverId, String departureCity, String arrivalCity,
                                                   Integer freeSeats, int page, int size) {
        Stream<RideResponse> stream = storage.rides.values().stream()
                .sorted((r1, r2) -> r1.getId().compareTo(r2.getId()));
        if (driverId != null) {
            stream = stream.filter(rideResponse -> rideResponse.getDriver().getId().equals(driverId));
        }
        if (departureCity != null && !departureCity.isBlank()) {
            stream = stream.filter(rideResponse -> rideResponse.getDepartureCity().equals(departureCity));
        }
        if (arrivalCity != null && !arrivalCity.isBlank()) {
            stream = stream.filter(rideResponse -> rideResponse.getArrivalCity().equals(arrivalCity));
        }
        if (freeSeats != null) {
            stream = stream.filter(rideResponse -> rideResponse.getFreeSeats().equals(freeSeats));
        }

        List<RideResponse> all = stream.toList();
        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<RideResponse> content = (from >= totalElements) ? List.of() : all.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public RideResponse getRideById(Long id) {
        return Optional.ofNullable(storage.rides.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Ride", id));
    }

    public RideResponse create(RideRequest request) {
        long id = storage.rideSequence.incrementAndGet();
        RideResponse ride = RideResponse.builder()
                .id(id)
                .driver(userService.getUserById(request.driverId()))
                .departureCity(request.departureCity())
                .arrivalCity(request.arrivalCity())
                .departureTime(request.departureTime())
                .arrivalTime(request.arrivalTime())
                .totalSeats(request.totalSeats())
                .freeSeats(request.freeSeats())
                .status(RideStatus.ACTIVE)
                .price(request.price())
                .build();
        storage.rides.put(id, ride);
        eventPublisher.publishCreated(ride);
        return ride;
    }

    public RideResponse updateRide(Long id, UpdateRideRequest request) {
        RideResponse existing = getRideById(id);
        RideResponse updatedRide = RideResponse.builder()
                .id(id)
                .driver(existing.getDriver())
                .departureCity(request.departureCity())
                .arrivalCity(request.arrivalCity())
                .departureTime(request.departureTime())
                .arrivalTime(request.arrivalTime())
                .totalSeats(request.totalSeats())
                .freeSeats(request.freeSeats())
                .status(request.status())
                .price(request.price())
                .build();
        storage.rides.put(id, updatedRide);
        eventPublisher.publishUpdated(updatedRide);
        return updatedRide;
    }

    public RideResponse patchRide(Long id, PatchRideRequest request) {
        RideResponse existing = getRideById(id);
        RideResponse updatedRide = RideResponse.builder()
                .id(id)
                .driver(existing.getDriver())
                .departureCity(request.departureCity() != null ? request.departureCity() : existing.getDepartureCity())
                .arrivalCity(request.arrivalCity() != null ? request.arrivalCity() : existing.getArrivalCity())
                .departureTime(request.departureTime() != null ? request.departureTime() : existing.getDepartureTime())
                .arrivalTime(request.arrivalTime() != null ? request.arrivalTime() : existing.getArrivalTime())
                .totalSeats(request.totalSeats() != null ? request.totalSeats() : existing.getTotalSeats())
                .freeSeats(request.freeSeats() != null ? request.freeSeats() : existing.getFreeSeats())
                .status(request.status() != null ? request.status() : existing.getStatus())
                .price(request.price() != null ? request.price() : existing.getPrice())
                .build();
        storage.rides.put(id, updatedRide);
        return updatedRide;
    }

    public RideResponse patchRideStatus(Long id, RideStatus status) {
        RideResponse existing = getRideById(id);
        RideResponse updatedRide = RideResponse.builder()
                .id(id)
                .driver(existing.getDriver())
                .departureCity(existing.getDepartureCity())
                .arrivalCity(existing.getArrivalCity())
                .departureTime(existing.getDepartureTime())
                .arrivalTime(existing.getArrivalTime())
                .totalSeats(existing.getTotalSeats())
                .freeSeats(existing.getFreeSeats())
                .status(status != null ? status : existing.getStatus())
                .price(existing.getPrice())
                .build();
        storage.rides.put(id, updatedRide);
        return updatedRide;
    }

    public void delete(Long id) {
        RideResponse rideResponse = getRideById(id);
        storage.rides.remove(id);
        eventPublisher.publishDeleted(rideResponse);
    }

    public PagedResponse<BookingResponse> getAllBookingsByRideId(Long id, int page, int size) {
        List<BookingResponse> all = storage.bookings.values().stream()
                .filter(booking -> Objects.equals(booking.getRide().getId(), id))
                .toList();
        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<BookingResponse> content = (from >= totalElements) ? List.of() : all.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }
}
