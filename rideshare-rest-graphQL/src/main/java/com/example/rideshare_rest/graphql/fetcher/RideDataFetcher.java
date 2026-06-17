package com.example.rideshare_rest.graphql.fetcher;

import com.example.rideshare_api_contract.dto.PagedResponse;
import com.example.rideshare_api_contract.dto.RideRequest;
import com.example.rideshare_api_contract.dto.RideResponse;
import com.example.rideshare_api_contract.dto.UpdateRideRequest;
import com.example.rideshare_rest.graphql.types.*;
import com.example.rideshare_rest.service.RideService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * DataFetcher для операций с поездками.
 * <p>
 * Обрабатывает корневые поля Query и Mutation, связанные с поездками.
 * Вложенные поля (Ride.bookings) обрабатываются в RideBookingsDataFetcher.
 * <p>
 * Принцип разделения: один DataFetcher — одна группа связанных операций.
 * Это делает код более читаемым и тестируемым.
 */
@DgsComponent
public class RideDataFetcher {
    private final RideService rideService;

    @Autowired
    public RideDataFetcher(RideService rideService) {
        this.rideService = rideService;
    }

    /**
     * Получение поездки по идентификатору.
     * Соответствует полю Query.ride(id: ID!) в схеме.
     */
    @DgsQuery
    public RideResponse ride(@InputArgument String id) {
        return rideService.getRideById(Long.parseLong(id));
    }

    /**
     * Список поездок с фильтрацией и пагинацией.
     * Соответствует полю Query.rides(filter, page, size) в схеме.
     *
     * @InputArgument автоматически маппит GraphQL-аргументы на Java-параметры.
     * Для сложных типов (input RideFilter) DGS сам десериализует JSON в объект.
     */
    @DgsQuery
    public RideConnectionGql rides(
            @InputArgument RideFilterGql filter,
            @InputArgument Integer page,
            @InputArgument Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        Long driverId = null;
        String departureCity = null;
        String arrivalCity = null;
        Integer freeSeats = null;

        if (filter != null) {
            driverId = filter.driverId() != null ? Long.parseLong(filter.driverId()) : null;
            departureCity = filter.departureCity() != null ? filter.departureCity() : null;
            arrivalCity = filter.arrivalCity() != null ? filter.arrivalCity() : null;
            freeSeats = filter.freeSeats() != null ? filter.freeSeats() : null;
        }
        PagedResponse<RideResponse> paged = rideService.getAllRides(
                driverId, departureCity, arrivalCity, freeSeats, pageNum, pageSize);

        return new RideConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements()
        );
    }

    /**
     * Создание поездки.
     * Соответствует полю Mutation.createRide(input: CreateRideInput!) в схеме.
     */
    @DgsMutation
    public RideResponse createRide(@InputArgument CreateRideInputGql input) {
        RideRequest request = new RideRequest(
                Long.parseLong(input.driverId()),
                input.departureCity(),
                input.arrivalCity(),
                input.departureTime(),
                input.arrivalTime(),
                input.totalSeats(),
                input.freeSeats(),
                input.status(),
                input.price()
        );
        return rideService.create(request);
    }

    /**
     * Обновление поездки.
     * Соответствует полю Mutation.updateRide(id, input) в схеме.
     */
    @DgsMutation
    public RideResponse updateRide(@InputArgument String id, @InputArgument UpdateRideInputGql input) {
        UpdateRideRequest request = new UpdateRideRequest(
                input.departureCity(),
                input.arrivalCity(),
                input.departureTime(),
                input.arrivalTime(),
                input.totalSeats(),
                input.freeSeats(),
                input.status(),
                input.price()
        );
        return rideService.updateRide(Long.parseLong(id), request);
    }

    /**
     * Удаление поездки.
     * Соответствует полю Mutation.deleteRide(id) в схеме.
     * Возвращает true при успешном удалении.
     */
    @DgsMutation
    public Boolean deleteRide(@InputArgument String id) {
        rideService.delete(Long.parseLong(id));
        return true;
    }
}
