package com.example.rideshare_rest.controllers;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_api_contract.endpoints.BookingApi;
import com.example.rideshare_rest.assemblers.BookingModelAssembler;
import com.example.rideshare_rest.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class BookingController implements BookingApi {
    private final BookingService bookingService;
    private final BookingModelAssembler bookingModelAssembler;
    private final PagedResourcesAssembler<BookingResponse> pagedResourcesAssembler;

    @Autowired
    public BookingController(BookingService bookingService,
                             BookingModelAssembler bookingModelAssembler,
                             PagedResourcesAssembler<BookingResponse> pagedResourcesAssembler) {
        this.bookingService = bookingService;
        this.bookingModelAssembler = bookingModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PagedModel<EntityModel<BookingResponse>> getAllBookings(BookingStatus status, int page, int size) {
        PagedResponse<BookingResponse> paged = bookingService.getAllBookings(status, page, size);
        Page<BookingResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedResourcesAssembler.toModel(springPage, bookingModelAssembler);
    }

    @Override
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN')")
    public EntityModel<BookingResponse> getBookingById(UUID id) {
        return bookingModelAssembler.toModel(bookingService.getBookingById(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    public ResponseEntity<EntityModel<BookingResponse>> createBooking(BookingRequest request) {
        BookingResponse created = bookingService.createBooking(request);
        EntityModel<BookingResponse> model = bookingModelAssembler.toModel(created);
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Override
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    public EntityModel<BookingResponse> updateBooking(UUID id, UpdateBookingRequest request) {
        return bookingModelAssembler.toModel(bookingService.updateBooking(id, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    public EntityModel<BookingResponse> patchBooking(UUID id, PatchBookingRequest request) {
        return bookingModelAssembler.toModel(bookingService.patchBooking(id, request));
    }

    @Override
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN')")
    public EntityModel<BookingResponse> patchBookingStatus(UUID id, BookingStatus status) {
        return bookingModelAssembler.toModel(bookingService.patchBookingStatus(id, status));
    }

    @Override
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    public void deleteBooking(UUID id) {
        bookingService.delete(id);
    }
}