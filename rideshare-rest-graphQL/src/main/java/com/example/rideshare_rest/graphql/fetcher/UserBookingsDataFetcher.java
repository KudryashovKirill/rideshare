package com.example.rideshare_rest.graphql.fetcher;

import com.example.rideshare_api_contract.dto.BookingResponse;
import com.example.rideshare_api_contract.dto.PagedResponse;
import com.example.rideshare_api_contract.dto.UserResponse;
import com.example.rideshare_rest.graphql.types.BookingConnectionGql;
import com.example.rideshare_rest.graphql.types.PageInfoGql;
import com.example.rideshare_rest.service.UserService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;

@DgsComponent
public class UserBookingsDataFetcher {
    private final UserService userService;

    public UserBookingsDataFetcher(UserService userService) {
        this.userService = userService;
    }

    @DgsData(parentType = "User", field = "bookings")
    public BookingConnectionGql userBookings(DgsDataFetchingEnvironment dfe,
                                             @InputArgument Integer page,
                                             @InputArgument Integer size) {
        UserResponse user = dfe.getSource();
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<BookingResponse> paged = userService.getAllBookingsByUserId(user.getId(), pageNum, pageSize);

        return new BookingConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements()
        );
    }
}
