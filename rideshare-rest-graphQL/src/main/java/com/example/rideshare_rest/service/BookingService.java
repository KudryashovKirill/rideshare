package com.example.rideshare_rest.service;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_api_contract.exceptions.NoFreeSeatsException;
import com.example.rideshare_api_contract.exceptions.ResourceNotFoundException;
import com.example.rideshare_rest.event.BookingEventPublisher;
import com.example.rideshare_rest.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class BookingService {
    private final InMemoryStorage storage;
    private final RideService rideService;
    private final UserService userService;
    private final BookingEventPublisher eventPublisher;

    @Autowired
    public BookingService(InMemoryStorage storage, RideService rideService,
                          UserService userService, BookingEventPublisher eventPublisher) {
        this.storage = storage;
        this.rideService = rideService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    public PagedResponse<BookingResponse> getAllBookings(BookingStatus status, int page, int size) {
        Stream<BookingResponse> stream = storage.bookings.values().stream()
                .sorted((b1, b2) -> b1.getId().compareTo(b2.getId()));
        if (status != null) {
            stream = stream.filter(bookingResponse -> bookingResponse.getStatus().equals(status));
        }
        List<BookingResponse> all = stream.toList();
        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<BookingResponse> content = (from >= totalElements) ? List.of() : all.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public BookingResponse getBookingById(Long id) {
        return Optional.ofNullable(storage.bookings.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    public BookingResponse createBooking(BookingRequest request) {
        RideResponse ride = rideService.getRideById(request.rideId());

        if (ride.getFreeSeats() < request.requestedSeats()) {
            throw new NoFreeSeatsException("Недостаточно свободных мест в поездке. Доступно: "
                    + ride.getFreeSeats());
        }

        RideStatus newRideStatus = (ride.getFreeSeats() - request.requestedSeats() == 0)
                ? RideStatus.FULL : ride.getStatus();

        RideResponse updatedRide = RideResponse.builder()
                .id(ride.getId())
                .driver(ride.getDriver())
                .departureCity(ride.getDepartureCity())
                .arrivalCity(ride.getArrivalCity())
                .departureTime(ride.getDepartureTime())
                .arrivalTime(ride.getArrivalTime())
                .totalSeats(ride.getTotalSeats())
                .freeSeats(ride.getFreeSeats() - request.requestedSeats())
                .status(newRideStatus)
                .price(ride.getPrice())
                .build();
        storage.rides.put(updatedRide.getId(), updatedRide);

        long id = storage.bookingSequence.incrementAndGet();
        UserResponse passenger = userService.getUserById(request.passengerId());
        BookingResponse booking = BookingResponse.builder()
                .id(id)
                .ride(updatedRide)
                .passenger(passenger)
                .status(request.status())
                .requestedSeats(request.requestedSeats())
                .build();
        storage.bookings.put(id, booking);
        eventPublisher.publishCreated(booking);
        return booking;
    }

    public BookingResponse updateBooking(Long id, UpdateBookingRequest request) {
        BookingResponse existing = getBookingById(id);
        RideResponse currentRide = rideService.getRideById(existing.getRide().getId());
        RideResponse rideToSave = currentRide;

        if (request.requestedSeats() != null && !request.requestedSeats().equals(existing.getRequestedSeats())) {
            int oldSeats = existing.getRequestedSeats();
            int newSeats = request.requestedSeats();
            int difference = newSeats - oldSeats;

            if (difference > 0 && currentRide.getFreeSeats() < difference) {
                throw new NoFreeSeatsException("Недостаточно свободных мест для изменения брони. Доступно: "
                        + currentRide.getFreeSeats());
            }

            int updatedFreeSeats = currentRide.getFreeSeats() - difference;

            RideStatus newRideStatus = (updatedFreeSeats == 0) ? RideStatus.FULL : RideStatus.ACTIVE;

            rideToSave = RideResponse.builder()
                    .id(currentRide.getId())
                    .driver(currentRide.getDriver())
                    .departureCity(currentRide.getDepartureCity())
                    .arrivalCity(currentRide.getArrivalCity())
                    .departureTime(currentRide.getDepartureTime())
                    .arrivalTime(currentRide.getArrivalTime())
                    .totalSeats(currentRide.getTotalSeats())
                    .freeSeats(updatedFreeSeats)
                    .status(newRideStatus)
                    .price(currentRide.getPrice())
                    .build();

            storage.rides.put(rideToSave.getId(), rideToSave);
        }
        BookingResponse updatedBooking = BookingResponse.builder()
                .id(id)
                .ride(rideToSave)
                .passenger(existing.getPassenger())
                .status(request.status())
                .requestedSeats(request.requestedSeats())
                .build();
        storage.bookings.put(id, updatedBooking);
        eventPublisher.publishUpdated(updatedBooking);
        return updatedBooking;
    }

    public BookingResponse patchBooking(Long id, PatchBookingRequest request) {
        BookingResponse existing = getBookingById(id);
        RideResponse currentRide = rideService.getRideById(existing.getRide().getId());
        RideResponse rideToSave = currentRide;

        if (request.requestedSeats() != null && !request.requestedSeats().equals(existing.getRequestedSeats())) {
            int oldSeats = existing.getRequestedSeats();
            int newSeats = request.requestedSeats();
            int difference = newSeats - oldSeats;

            if (difference > 0 && currentRide.getFreeSeats() < difference) {
                throw new NoFreeSeatsException("Недостаточно свободных мест для изменения брони. Доступно: "
                        + currentRide.getFreeSeats());
            }

            int updatedFreeSeats = currentRide.getFreeSeats() - difference;

            RideStatus newRideStatus = (updatedFreeSeats == 0) ? RideStatus.FULL : RideStatus.ACTIVE;

            rideToSave = RideResponse.builder()
                    .id(currentRide.getId())
                    .driver(currentRide.getDriver())
                    .departureCity(currentRide.getDepartureCity())
                    .arrivalCity(currentRide.getArrivalCity())
                    .departureTime(currentRide.getDepartureTime())
                    .arrivalTime(currentRide.getArrivalTime())
                    .totalSeats(currentRide.getTotalSeats())
                    .freeSeats(updatedFreeSeats)
                    .status(newRideStatus)
                    .price(currentRide.getPrice())
                    .build();

            storage.rides.put(rideToSave.getId(), rideToSave);
        }

        BookingResponse updatedBooking = BookingResponse.builder()
                .id(id)
                .ride(rideToSave)
                .passenger(existing.getPassenger())
                .status(request.status() != null ? request.status() : existing.getStatus())
                .requestedSeats(request.requestedSeats() != null ?
                        request.requestedSeats() : existing.getRequestedSeats())
                .build();

        storage.bookings.put(id, updatedBooking);
        return updatedBooking;
    }

    public BookingResponse patchBookingStatus(Long id, BookingStatus status) {
        BookingResponse existing = getBookingById(id);
        if (status == null || status.equals(existing.getStatus())) {
            return existing;
        }
        RideResponse currentRide = rideService.getRideById(existing.getRide().getId());
        RideResponse rideToSave = currentRide;

        if (status == BookingStatus.REJECTED &&
                (existing.getStatus() == BookingStatus.PENDING || existing.getStatus() == BookingStatus.CONFIRMED)) {

            RideStatus restoredStatus = (currentRide.getFreeSeats() + existing.getRequestedSeats() > 0)
                    ? RideStatus.ACTIVE : currentRide.getStatus();
            rideToSave = RideResponse.builder()
                    .id(currentRide.getId())
                    .driver(currentRide.getDriver())
                    .departureCity(currentRide.getDepartureCity())
                    .arrivalCity(currentRide.getArrivalCity())
                    .departureTime(currentRide.getDepartureTime())
                    .arrivalTime(currentRide.getArrivalTime())
                    .totalSeats(currentRide.getTotalSeats())
                    .freeSeats(currentRide.getFreeSeats() + existing.getRequestedSeats())
                    .status(restoredStatus)
                    .price(currentRide.getPrice())
                    .build();

            storage.rides.put(rideToSave.getId(), rideToSave);
        }

        BookingResponse updatedBooking = BookingResponse.builder()
                .id(id)
                .ride(rideToSave)
                .passenger(existing.getPassenger())
                .status(status)
                .requestedSeats(existing.getRequestedSeats())
                .build();

        storage.bookings.put(id, updatedBooking);
        return updatedBooking;
    }

    public void delete(Long id) {
        BookingResponse existing = getBookingById(id);
        if (existing.getStatus() != BookingStatus.REJECTED) {
            RideResponse ride = rideService.getRideById(existing.getRide().getId());
            RideStatus restoredStatus = (ride.getFreeSeats() + existing.getRequestedSeats() > 0)
                    ? RideStatus.ACTIVE : ride.getStatus();
            RideResponse updatedRide = RideResponse.builder()
                    .id(ride.getId())
                    .driver(ride.getDriver())
                    .departureCity(ride.getDepartureCity())
                    .arrivalCity(ride.getArrivalCity())
                    .departureTime(ride.getDepartureTime())
                    .arrivalTime(ride.getArrivalTime())
                    .totalSeats(ride.getTotalSeats())
                    .freeSeats(ride.getFreeSeats() + existing.getRequestedSeats())
                    .status(restoredStatus)
                    .price(ride.getPrice())
                    .build();
            storage.rides.put(updatedRide.getId(), updatedRide);
        }
        storage.bookings.remove(id);
        eventPublisher.publishDeleted(existing);
    }
}
