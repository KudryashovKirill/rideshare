package com.example.rideshare_rest.controllers;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_api_contract.endpoints.UserApi;
import com.example.rideshare_rest.assemblers.BookingModelAssembler;
import com.example.rideshare_rest.assemblers.RideModelAssembler;
import com.example.rideshare_rest.assemblers.UserModelAssembler;
import com.example.rideshare_rest.service.BookingService;
import com.example.rideshare_rest.service.RideService;
import com.example.rideshare_rest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UserApi {
    private final UserService userService;
    private final RideService rideService;
    private final BookingService bookingService;
    private final UserModelAssembler userModelAssembler;
    private final RideModelAssembler rideModelAssembler;
    private final BookingModelAssembler bookingModelAssembler;
    private final PagedResourcesAssembler<UserResponse> pagedResourcesUserAssembler;
    private final PagedResourcesAssembler<RideResponse> pagedResourcesRideAssembler;
    private final PagedResourcesAssembler<BookingResponse> pagedResourcesBookingAssembler;

    @Autowired
    public UserController(UserService userService,
                          RideService rideService,
                          BookingService bookingService,
                          UserModelAssembler userModelAssembler,
                          RideModelAssembler rideModelAssembler,
                          BookingModelAssembler bookingModelAssembler,
                          PagedResourcesAssembler<UserResponse> pagedResourcesUserAssembler,
                          PagedResourcesAssembler<RideResponse> pagedResourcesRideAssembler,
                          PagedResourcesAssembler<BookingResponse> pagedResourcesBookingAssembler) {
        this.userService = userService;
        this.rideService = rideService;
        this.bookingService = bookingService;
        this.userModelAssembler = userModelAssembler;
        this.rideModelAssembler = rideModelAssembler;
        this.bookingModelAssembler = bookingModelAssembler;
        this.pagedResourcesUserAssembler = pagedResourcesUserAssembler;
        this.pagedResourcesRideAssembler = pagedResourcesRideAssembler;
        this.pagedResourcesBookingAssembler = pagedResourcesBookingAssembler;
    }

    @Override
    public PagedModel<EntityModel<UserResponse>> getAllUsers(int page, int size) {
        PagedResponse<UserResponse> paged = userService.getAllUsers(page, size);
        Page<UserResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedResourcesUserAssembler.toModel(springPage, userModelAssembler);
    }

    @Override
    public EntityModel<UserResponse> getUserById(Long id) {
        return userModelAssembler.toModel(userService.getUserById(id));
    }

    @Override
    public ResponseEntity<EntityModel<UserResponse>> createUser(UserRequest request) {
        UserResponse created = userService.create(request);
        EntityModel<UserResponse> model = userModelAssembler.toModel(created);
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Override
    public EntityModel<UserResponse> updateUser(Long id, UserRequest request) {
        return userModelAssembler.toModel(userService.updateUser(id, request));
    }

    @Override
    public EntityModel<UserResponse> patchUser(Long id, PatchUserRequest request) {
        return userModelAssembler.toModel(userService.patchUser(id, request));
    }

    @Override
    public void deleteUser(Long id) {
        userService.delete(id);
    }

    @Override
    public PagedModel<EntityModel<RideResponse>> getRidesByDriver(Long id, int page, int size) {
        userService.getUserById(id);
        PagedResponse<RideResponse> paged = userService.getRidesByDriver(id, page, size);
        Page<RideResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedResourcesRideAssembler.toModel(springPage, rideModelAssembler);
    }

    @Override
    public PagedModel<EntityModel<RideResponse>> getRidesAsPassenger(Long id, int page, int size) {
        userService.getUserById(id);
        PagedResponse<RideResponse> paged = userService.getRidesAsPassenger(id, page, size);
        Page<RideResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedResourcesRideAssembler.toModel(springPage, rideModelAssembler);
    }

    @Override
    public PagedModel<EntityModel<BookingResponse>> getAllBookingsByUserId(Long id, int page, int size) {
        userService.getUserById(id);
        PagedResponse<BookingResponse> paged = userService.getAllBookingsByUserId(id, page, size);
        Page<BookingResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedResourcesBookingAssembler.toModel(springPage, bookingModelAssembler);
    }
}
