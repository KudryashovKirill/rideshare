package com.example.rideshare_rest.graphql.fetcher;

import com.example.rideshare_api_contract.dto.BookingResponse;
import com.example.rideshare_api_contract.dto.PagedResponse;
import com.example.rideshare_api_contract.dto.RideResponse;
import com.example.rideshare_rest.graphql.types.BookingConnectionGql;
import com.example.rideshare_rest.graphql.types.PageInfoGql;
import com.example.rideshare_rest.service.RideService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import org.springframework.beans.factory.annotation.Autowired;

@DgsComponent
public class RideBookingDataFetcher {
    private final RideService rideService;

    @Autowired
    public RideBookingDataFetcher(RideService rideService) {
        this.rideService = rideService;
    }

    @DgsData(parentType = "Ride", field = "bookings")
    public BookingConnectionGql bookings(DgsDataFetchingEnvironment dfe,
                                         @InputArgument Integer page,
                                         @InputArgument Integer size) {
        RideResponse ride = dfe.getSource();

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<BookingResponse> paged = rideService.getAllBookingsByRideId(ride.getId(), pageNum, pageSize);

        return new BookingConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements()
        );
    }
}
