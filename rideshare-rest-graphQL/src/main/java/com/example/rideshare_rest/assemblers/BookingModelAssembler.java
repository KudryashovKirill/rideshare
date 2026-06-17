package com.example.rideshare_rest.assemblers;

import com.example.rideshare_api_contract.dto.BookingResponse;
import com.example.rideshare_rest.controllers.BookingController;
import com.example.rideshare_rest.controllers.RideController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookingModelAssembler implements
        RepresentationModelAssembler<BookingResponse, EntityModel<BookingResponse>> {
    @Override
    public EntityModel<BookingResponse> toModel(BookingResponse booking) {
        EntityModel<BookingResponse> model = EntityModel.of(booking,
                linkTo(methodOn(BookingController.class).getBookingById(booking.getId())).withSelfRel(),
                linkTo(methodOn(BookingController.class).getAllBookings(null, 0, 20)).withRel("collection")
        );
        if (booking.getRide() != null) {
            model.add(linkTo(methodOn(RideController.class)
                    .getRideById(booking.getRide().getId())).withRel("ride"));
        }
        return model;
    }
}
