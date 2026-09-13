package com.example.rideshare_rest.service;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_api_contract.exceptions.ResourceNotFoundException;
import com.example.rideshare_rest.entity.RideEntity;
import com.example.rideshare_rest.entity.UserEntity;
import com.example.rideshare_rest.event.RideEventPublisher;
import com.example.rideshare_rest.mapper.BookingMapper;
import com.example.rideshare_rest.mapper.RideMapper;
import com.example.rideshare_rest.storage.BookingRepository;
import com.example.rideshare_rest.storage.RideRepository;
import com.example.rideshare_rest.storage.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@Transactional
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final RideMapper rideMapper;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final RideEventPublisher eventPublisher;

    @Autowired
    public RideService(RideRepository rideRepository,
                       UserRepository userRepository,
                       BookingRepository bookingRepository,
                       RideMapper rideMapper,
                       BookingMapper bookingMapper,
                       UserService userService,
                       RideEventPublisher eventPublisher) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.rideMapper = rideMapper;
        this.bookingMapper = bookingMapper;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public PagedResponse<RideResponse> getAllRides(Long driverId, String departureCity, String arrivalCity,
                                                   Integer freeSeats, int page, int size) {
        Stream<RideResponse> stream = rideRepository.findAll().stream()
                .map(rideMapper::toResponse)
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

    @Transactional(readOnly = true)
    public RideResponse getRideById(Long id) {
        return rideRepository.findById(id)
                .map(rideMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", id));
    }

    public RideResponse create(RideRequest request) {
        UserResponse driverDto = userService.getUserById(request.driverId());
        UserEntity driverEntity = userRepository.findById(driverDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", driverDto.getId()));

        RideEntity entity = rideMapper.toEntity(request, driverEntity);
        RideEntity savedEntity = rideRepository.save(entity);

        RideResponse ride = rideMapper.toResponse(savedEntity);
        eventPublisher.publishCreated(ride);
        return ride;
    }

    public RideResponse updateRide(Long id, UpdateRideRequest request) {
        RideEntity existingEntity = rideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", id));

        rideMapper.updateEntityFromRequest(request, existingEntity);
        RideEntity savedEntity = rideRepository.save(existingEntity);

        RideResponse updatedRide = rideMapper.toResponse(savedEntity);
        eventPublisher.publishUpdated(updatedRide);
        return updatedRide;
    }

    public RideResponse patchRide(Long id, PatchRideRequest request) {
        RideEntity existingEntity = rideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", id));

        rideMapper.updateEntityFromPatch(request, existingEntity);
        RideEntity savedEntity = rideRepository.save(existingEntity);

        return rideMapper.toResponse(savedEntity);
    }

    public RideResponse patchRideStatus(Long id, RideStatus status) {
        RideEntity existingEntity = rideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ride", id));

        if (status != null) {
            existingEntity.setStatus(status);
        }

        RideEntity savedEntity = rideRepository.save(existingEntity);
        return rideMapper.toResponse(savedEntity);
    }

    public void delete(Long id) {
        RideResponse rideResponse = getRideById(id);
        rideRepository.deleteById(id);
        eventPublisher.publishDeleted(rideResponse);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getAllBookingsByRideId(Long id, int page, int size) {
        List<BookingResponse> all = bookingRepository.findAll().stream()
                .map(bookingMapper::toResponse)
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