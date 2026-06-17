package com.example.rideshare_rest.graphql.fetcher;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_rest.graphql.types.*;
import com.example.rideshare_rest.service.BookingService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import org.springframework.beans.factory.annotation.Autowired;

@DgsComponent
public class BookingDataFetcher {
    private final BookingService bookingService;

    @Autowired
    public BookingDataFetcher(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Получение бронирования по идентификатору.
     * Соответствует полю Query.booking(id: ID!) в схеме.
     * Возвращает null если бронирование не найдено (вместо исключения, как принято в GraphQL).
     */
    @DgsQuery
    public BookingResponse booking(@InputArgument String id) {
        return bookingService.getBookingById(Long.parseLong(id));
    }

    /**
     * Список бронирований с фильтрацией и пагинацией.
     * Соответствует полю Query.bookings(filter, page, size) в схеме.
     *
     * @InputArgument автоматически маппит GraphQL-аргументы на Java-параметры.
     * Для сложных типов (input BookingFilter) DGS сам десериализует JSON в объект.
     */
    @DgsQuery
    public BookingConnectionGql bookings(
            @InputArgument BookingFilterGql filter,
            @InputArgument Integer page,
            @InputArgument Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        BookingStatus status = null;

        if (filter != null) {
            status = filter.status() != null ? filter.status() : null;
        }

        PagedResponse<BookingResponse> paged = bookingService.getAllBookings(status, pageNum, pageSize);

        return new BookingConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements()
        );
    }

    /**
     * Создание бронирования.
     * Соответствует полю Mutation.createBooking(input: CreateBookingInput!) в схеме.
     */
    @DgsMutation
    public BookingResponse createBooking(@InputArgument CreateBookingInputGql input) {
        BookingRequest request = new BookingRequest(
                Long.parseLong(input.rideId()),
                Long.parseLong(input.passengerId()),
                input.status(),
                input.requestedSeats()
        );
        return bookingService.createBooking(request);
    }

    /**
     * Обновление бронирования.
     * Соответствует полю Mutation.updateBooking(id, input) в схеме.
     */
    @DgsMutation
    public BookingResponse updateBooking(@InputArgument String id, @InputArgument UpdateBookingInputGql input) {
        UpdateBookingRequest request = new UpdateBookingRequest(
                input.status(),
                input.requestedSeats()
        );
        return bookingService.updateBooking(Long.parseLong(id), request);
    }

    /**
     * Удаление бронирования.
     * Соответствует полю Mutation.deleteBooking(id) в схеме.
     * Возвращает true при успешном удалении.
     */
    @DgsMutation
    public Boolean deleteBooking(@InputArgument String id) {
        bookingService.delete(Long.parseLong(id));
        return true;
    }
}
