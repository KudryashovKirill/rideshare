package com.example.rideshare_rest.service;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_api_contract.exceptions.ResourceNotFoundException;
import com.example.rideshare_rest.entity.UserEntity;
import com.example.rideshare_rest.event.UserEventPublisher;
import com.example.rideshare_rest.mapper.BookingMapper;
import com.example.rideshare_rest.mapper.RideMapper;
import com.example.rideshare_rest.mapper.UserMapper;
import com.example.rideshare_rest.storage.BookingRepository;
import com.example.rideshare_rest.storage.RideRepository;
import com.example.rideshare_rest.storage.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final BookingRepository bookingRepository;
    private final UserMapper userMapper;
    private final RideMapper rideMapper;
    private final BookingMapper bookingMapper;
    private final UserEventPublisher eventPublisher;

    @Autowired
    public UserService(UserRepository userRepository,
                       RideRepository rideRepository,
                       BookingRepository bookingRepository,
                       UserMapper userMapper,
                       RideMapper rideMapper,
                       BookingMapper bookingMapper,
                       UserEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.rideRepository = rideRepository;
        this.bookingRepository = bookingRepository;
        this.userMapper = userMapper;
        this.rideMapper = rideMapper;
        this.bookingMapper = bookingMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        List<UserResponse> all = userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .sorted(Comparator.comparingLong(UserResponse::getId))
                .toList();
        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<UserResponse> content = (from >= totalElements) ? List.of() : all.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse create(UserRequest request) {
        UserEntity entity = userMapper.toEntity(request);
        UserEntity savedEntity = userRepository.save(entity);

        UserResponse user = userMapper.toResponse(savedEntity);
        eventPublisher.publishCreated(user);
        return user;
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        UserEntity existingEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        userMapper.updateEntityFromRequest(request, existingEntity);
        UserEntity savedEntity = userRepository.save(existingEntity);

        UserResponse user = userMapper.toResponse(savedEntity);
        eventPublisher.publishUpdated(user);
        return user;
    }

    public UserResponse patchUser(Long id, PatchUserRequest request) {
        UserEntity existingEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        userMapper.updateEntityFromPatch(request, existingEntity);
        UserEntity savedEntity = userRepository.save(existingEntity);

        return userMapper.toResponse(savedEntity);
    }

    public void delete(Long id) {
        UserResponse userResponse = getUserById(id);
        userRepository.deleteById(id);
        eventPublisher.publishDeleted(userResponse);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RideResponse> getRidesByDriver(Long id, int page, int size) {
        List<RideResponse> response = rideRepository.findAll().stream()
                .map(rideMapper::toResponse)
                .filter(rideResponse -> rideResponse.getDriver().getId().equals(id))
                .toList();
        int totalElements = response.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<RideResponse> content = (from >= totalElements) ? List.of() : response.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RideResponse> getRidesAsPassenger(Long id, int page, int size) {
        List<RideResponse> response = bookingRepository.findAll().stream()
                .map(bookingMapper::toResponse)
                .filter(bookingResponse -> bookingResponse.getPassenger() != null
                        && bookingResponse.getPassenger().getId().equals(id))
                .map(BookingResponse::getRide)
                .distinct()
                .toList();
        int totalElements = response.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<RideResponse> content = (from >= totalElements) ? List.of() : response.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getAllBookingsByUserId(Long id, int page, int size) {
        List<BookingResponse> response = bookingRepository.findAll().stream()
                .map(bookingMapper::toResponse)
                .filter(bookingResponse -> bookingResponse.getPassenger().getId().equals(id))
                .toList();
        int totalElements = response.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<BookingResponse> content = (from >= totalElements) ? List.of() : response.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }
}