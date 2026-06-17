package com.example.rideshare_rest.graphql.fetcher;

import com.example.rideshare_api_contract.dto.BookingResponse;
import com.example.rideshare_api_contract.dto.RideResponse;
import com.example.rideshare_api_contract.dto.UserResponse;
import com.example.rideshare_rest.service.RideService;
import com.example.rideshare_rest.service.UserService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

@DgsComponent
public class BookingRelationDataFetcher {
    private final RideService rideService;
    private final UserService userService;

    @Autowired
    public BookingRelationDataFetcher(RideService rideService, UserService userService) {
        this.rideService = rideService;
        this.userService = userService;
    }

    @DgsData(parentType = "Booking", field = "ride")
    public RideResponse ride(DgsDataFetchingEnvironment dfe) {
        BookingResponse booking = dfe.getSource();
        return rideService.getRideById(booking.getRide().getId());
    }

    @DgsData(parentType = "Booking", field = "passenger")
    public UserResponse passenger(DgsDataFetchingEnvironment dfe) {
        BookingResponse booking = dfe.getSource();

        if (booking.getPassenger() != null) {
            return userService.getUserById(booking.getPassenger().getId());
        }
        return null;
    }
}
