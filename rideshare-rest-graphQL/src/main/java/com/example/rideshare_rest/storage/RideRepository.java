package com.example.rideshare_rest.storage;

import com.example.rideshare_rest.entity.RideEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@EnableJpaRepositories
public interface RideRepository extends JpaRepository<RideEntity, UUID> {

    Page<RideEntity> findByDriverId(Long driverId, Pageable pageable);

    @Query("SELECT DISTINCT b.ride FROM BookingEntity b WHERE b.passenger.id = :passengerId")
    Page<RideEntity> findRidesByPassengerId(@Param("passengerId") Long passengerId, Pageable pageable);
}
