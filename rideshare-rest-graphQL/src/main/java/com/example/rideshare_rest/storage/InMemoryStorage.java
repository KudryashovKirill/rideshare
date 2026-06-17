package com.example.rideshare_rest.storage;

import com.example.rideshare_api_contract.dto.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryStorage {
    public final Map<Long, UserResponse> users = new ConcurrentHashMap<>();
    public final Map<Long, RideResponse> rides = new ConcurrentHashMap<>();
    public final Map<Long, BookingResponse> bookings = new ConcurrentHashMap<>();

    public final AtomicLong userSequence = new AtomicLong(0);
    public final AtomicLong rideSequence = new AtomicLong(0);
    public final AtomicLong bookingSequence = new AtomicLong(0);

    @PostConstruct
    public void init() {
        UserResponse user1 = UserResponse.builder()
                .id(userSequence.incrementAndGet())
                .firstName("Василий")
                .lastName("Васильев")
                .fullName("Василий Васильев")
                .email("Vasiliy.v@gmail.com")
                .birthDate(LocalDate.of(2005, 1, 14))
                .build();

        UserResponse user2 = UserResponse.builder()
                .id(userSequence.incrementAndGet())
                .firstName("Петр")
                .lastName("Петров")
                .fullName("Петр Петров")
                .email("Petr.p@example.com")
                .birthDate(LocalDate.of(2004, 5, 20))
                .build();

        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);

        long rideId1 = rideSequence.incrementAndGet();
        RideResponse ride1 = RideResponse.builder()
                .id(rideId1)
                .driver(user1)
                .departureCity("Москва")
                .arrivalCity("Казань")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(12))
                .totalSeats(4)
                .freeSeats(3)
                .price(1500)
                .status(RideStatus.ACTIVE)
                .build();

        long rideId2 = rideSequence.incrementAndGet();
        RideResponse ride2 = RideResponse.builder()
                .id(rideId2)
                .driver(user2)
                .departureCity("Санкт-Петербург")
                .arrivalCity("Москва")
                .departureTime(LocalDateTime.now().plusDays(2))
                .arrivalTime(LocalDateTime.now().plusDays(2).plusHours(9))
                .totalSeats(5)
                .freeSeats(1)
                .price(2000)
                .status(RideStatus.ACTIVE)
                .build();

        rides.put(ride1.getId(), ride1);
        rides.put(ride2.getId(), ride2);


        long bookingId1 = bookingSequence.incrementAndGet();
        bookings.put(bookingId1, BookingResponse.builder()
                .id(bookingId1)
                .ride(ride1)
                .passenger(user2)
                .status(BookingStatus.CONFIRMED)
                .requestedSeats(1)
                .build());

        long bookingId2 = bookingSequence.incrementAndGet();
        bookings.put(bookingId2, BookingResponse.builder()
                .id(bookingId2)
                .ride(ride2)
                .passenger(user1)
                .status(BookingStatus.PENDING)
                .requestedSeats(2)
                .build());

        long bookingId3 = bookingSequence.incrementAndGet();
        bookings.put(bookingId3, BookingResponse.builder()
                .id(bookingId3)
                .ride(ride1)
                .passenger(UserResponse.builder()
                        .id(99L)
                        .firstName("Иван")
                        .lastName("Иванов")
                        .fullName("Иван Иванов")
                        .build())
                .status(BookingStatus.REJECTED)
                .requestedSeats(1)
                .build());
    }
}
