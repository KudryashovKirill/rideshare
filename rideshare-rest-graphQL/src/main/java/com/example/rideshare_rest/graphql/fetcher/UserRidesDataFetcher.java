package com.example.rideshare_rest.graphql.fetcher;

import com.example.rideshare_api_contract.dto.RideResponse;
import com.example.rideshare_api_contract.dto.UserResponse;
import com.example.rideshare_rest.service.UserService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

@DgsComponent
public class UserRidesDataFetcher {
    private final UserService userService;

    @Autowired
    public UserRidesDataFetcher(UserService userService) {
        this.userService = userService;
    }

    @DgsData(parentType = "Ride", field = "driver")
    public UserResponse driver(DgsDataFetchingEnvironment dfe) {
        RideResponse ride = dfe.getSource();

        if (ride.getDriver() != null) {
            return userService.getUserById(ride.getDriver().getId());
        }

        return null;
    }
}
