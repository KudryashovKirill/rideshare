package com.example.rideshare_rest.service;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_api_contract.exceptions.NoFreeSeatsException;
import com.example.rideshare_api_contract.exceptions.ResourceNotFoundException;
import com.example.rideshare_rest.entity.BookingEntity;
import com.example.rideshare_rest.entity.RideEntity;
import com.example.rideshare_rest.entity.UserEntity;
import com.example.rideshare_rest.event.BookingEventPublisher;
import com.example.rideshare_rest.mapper.BookingMapper;
import com.example.rideshare_rest.storage.BookingRepository;
import com.example.rideshare_rest.storage.RideRepository;
import com.example.rideshare_rest.storage.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final RideService rideService;
    private final UserService userService;
    private final BookingEventPublisher eventPublisher;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          RideRepository rideRepository,
                          UserRepository userRepository,
                          BookingMapper bookingMapper,
                          RideService rideService,
                          UserService userService,
                          BookingEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
        this.rideService = rideService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getAllBookings(BookingStatus status, int page, int size) {
        List<BookingEntity> entities = bookingRepository.findAll();

        Stream<BookingEntity> baseStream = entities.stream()
                .sorted((b1, b2) -> b1.getId().compareTo(b2.getId()));

        if (status != null) {
            baseStream = baseStream.filter(booking -> booking.getStatus().equals(status));
        }
        List<BookingResponse> all = baseStream.map(bookingMapper::toResponse).toList();

        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<BookingResponse> content = (from >= totalElements) ? List.of() : all.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .map(bookingMapper::toResponse)
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

        RideEntity rideEntity = rideRepository.findById(updatedRide.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ride", updatedRide.getId()));
        rideEntity.setFreeSeats(updatedRide.getFreeSeats());
        rideEntity.setStatus(updatedRide.getStatus());
        rideRepository.save(rideEntity);

        UserResponse passenger = userService.getUserById(request.passengerId());
        UserEntity passengerEntity = userRepository.findById(passenger.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", passenger.getId()));

        BookingEntity bookingEntity = BookingEntity.builder()
                .ride(rideEntity)
                .passenger(passengerEntity)
                .status(request.status())
                .requestedSeats(request.requestedSeats())
                .build();

        BookingEntity savedBooking = bookingRepository.save(bookingEntity);
        BookingResponse booking = bookingMapper.toResponse(savedBooking);

        eventPublisher.publishCreated(booking);
        return booking;
    }

    public BookingResponse updateBooking(UUID id, UpdateBookingRequest request) {
        BookingResponse existing = getBookingById(id);
        RideResponse currentRide = rideService.getRideById(existing.getRide().getId());

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

            RideEntity rideEntity = rideRepository.findById(currentRide.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ride", currentRide.getId()));
            rideEntity.setFreeSeats(updatedFreeSeats);
            rideEntity.setStatus(newRideStatus);
            rideRepository.save(rideEntity);
        }

        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        bookingEntity.setStatus(request.status());
        bookingEntity.setRequestedSeats(request.requestedSeats());

        BookingEntity savedBooking = bookingRepository.save(bookingEntity);
        BookingResponse updatedBooking = bookingMapper.toResponse(savedBooking);

        eventPublisher.publishUpdated(updatedBooking);
        return updatedBooking;
    }

    public BookingResponse patchBooking(UUID id, PatchBookingRequest request) {
        BookingResponse existing = getBookingById(id);
        RideResponse currentRide = rideService.getRideById(existing.getRide().getId());

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

            RideEntity rideEntity = rideRepository.findById(currentRide.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ride", currentRide.getId()));
            rideEntity.setFreeSeats(updatedFreeSeats);
            rideEntity.setStatus(newRideStatus);
            rideRepository.save(rideEntity);
        }

        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        if (request.status() != null) {
            bookingEntity.setStatus(request.status());
        }
        if (request.requestedSeats() != null) {
            bookingEntity.setRequestedSeats(request.requestedSeats());
        }

        BookingEntity savedBooking = bookingRepository.save(bookingEntity);
        return bookingMapper.toResponse(savedBooking);
    }

    public BookingResponse patchBookingStatus(UUID id, BookingStatus status) {
        BookingResponse existing = getBookingById(id);
        if (status == null || status.equals(existing.getStatus())) {
            return existing;
        }
        RideResponse currentRide = rideService.getRideById(existing.getRide().getId());

        if (status == BookingStatus.REJECTED &&
                (existing.getStatus() == BookingStatus.PENDING || existing.getStatus() == BookingStatus.CONFIRMED)) {

            RideStatus restoredStatus = (currentRide.getFreeSeats() + existing.getRequestedSeats() > 0)
                    ? RideStatus.ACTIVE : currentRide.getStatus();

            RideEntity rideEntity = rideRepository.findById(currentRide.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ride", currentRide.getId()));
            rideEntity.setFreeSeats(currentRide.getFreeSeats() + existing.getRequestedSeats());
            rideEntity.setStatus(restoredStatus);
            rideRepository.save(rideEntity);
        }

        BookingEntity bookingEntity = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        bookingEntity.setStatus(status);

        BookingEntity savedBooking = bookingRepository.save(bookingEntity);
        return bookingMapper.toResponse(savedBooking);
    }

    public void delete(UUID id) {
        BookingResponse existing = getBookingById(id);
        if (existing.getStatus() != BookingStatus.REJECTED) {
            RideResponse ride = rideService.getRideById(existing.getRide().getId());
            RideStatus restoredStatus = (ride.getFreeSeats() + existing.getRequestedSeats() > 0)
                    ? RideStatus.ACTIVE : ride.getStatus();

            RideEntity rideEntity = rideRepository.findById(ride.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ride", ride.getId()));
            rideEntity.setFreeSeats(ride.getFreeSeats() + existing.getRequestedSeats());
            rideEntity.setStatus(restoredStatus);
            rideRepository.save(rideEntity);
        }

        bookingRepository.deleteById(id);
        eventPublisher.publishDeleted(existing);
    }
}