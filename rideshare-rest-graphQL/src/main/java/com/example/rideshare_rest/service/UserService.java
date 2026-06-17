package com.example.rideshare_rest.service;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_api_contract.exceptions.ResourceNotFoundException;
import com.example.rideshare_rest.event.UserEventPublisher;
import com.example.rideshare_rest.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final InMemoryStorage storage;
    private final UserEventPublisher eventPublisher;

    @Autowired
    public UserService(InMemoryStorage storage, UserEventPublisher eventPublisher) {
        this.storage = storage;
        this.eventPublisher = eventPublisher;
    }

    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        List<UserResponse> all = storage.users.values().stream()
                .sorted(Comparator.comparingLong(UserResponse::getId))
                .toList();
        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<UserResponse> content = (from >= totalElements) ? List.of() : all.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public UserResponse getUserById(Long id) {
        return Optional.ofNullable(storage.users.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse create(UserRequest request) {
        long id = storage.userSequence.incrementAndGet();
        String fullName = request.firstName() + " " + request.lastName();
        UserResponse user = UserResponse.builder()
                .id(id)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .fullName(fullName)
                .email(request.email())
                .birthDate(request.birthDate())
                .build();
        storage.users.put(id, user);
        eventPublisher.publishCreated(user);
        return user;
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        UserResponse existing = getUserById(id);
        String fullName = request.firstName() + " " + request.lastName();
        UserResponse user = UserResponse.builder()
                .id(id)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .fullName(fullName)
                .email(request.email())
                .birthDate(request.birthDate())
                .build();
        storage.users.put(id, user);
        eventPublisher.publishUpdated(user);
        return user;
    }

    public UserResponse patchUser(Long id, PatchUserRequest request) {
        UserResponse existing = getUserById(id);
        String newFirstName = request.firstName() != null ? request.firstName() : existing.getFirstName();
        String newLastName = request.lastName() != null ? request.lastName() : existing.getLastName();
        UserResponse updated = UserResponse.builder()
                .id(id)
                .firstName(newFirstName)
                .lastName(newLastName)
                .fullName(newFirstName + " " + newLastName)
                .email(request.email() != null ? request.email() : existing.getEmail())
                .birthDate(request.birthDate() != null ? request.birthDate() : existing.getBirthDate())
                .build();
        storage.users.put(id, updated);
        return updated;
    }

    public void delete(Long id) {
        UserResponse userResponse = getUserById(id);
        storage.users.remove(id);
        eventPublisher.publishDeleted(userResponse);
    }

    public PagedResponse<RideResponse> getRidesByDriver(Long id, int page, int size) {
        List<RideResponse> response = storage.rides.values().stream()
                .filter(rideResponse -> rideResponse.getDriver().getId().equals(id))
                .toList();
        int totalElements = response.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<RideResponse> content = (from >= totalElements) ? List.of() : response.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public PagedResponse<RideResponse> getRidesAsPassenger(Long id, int page, int size) {
        List<RideResponse> response = storage.bookings.values().stream()
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

    public PagedResponse<BookingResponse> getAllBookingsByUserId(Long id, int page, int size) {
        List<BookingResponse> response = storage.bookings.values().stream()
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
