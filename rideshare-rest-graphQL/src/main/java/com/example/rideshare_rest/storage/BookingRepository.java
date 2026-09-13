package com.example.rideshare_rest.storage;

import com.example.rideshare_api_contract.dto.BookingStatus;
import com.example.rideshare_rest.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    Page<BookingEntity> findByStatus(BookingStatus status, Pageable pageable);

    Page<BookingEntity> findByRideId(Long rideId, Pageable pageable);

    Page<BookingEntity> findByPassengerId(Long passengerId, Pageable pageable);
}
