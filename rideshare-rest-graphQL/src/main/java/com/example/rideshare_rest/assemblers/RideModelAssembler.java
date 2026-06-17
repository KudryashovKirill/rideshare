package com.example.rideshare_rest.assemblers;

import com.example.rideshare_api_contract.dto.RideResponse;
import com.example.rideshare_api_contract.dto.RideStatus;
import com.example.rideshare_rest.controllers.BookingController;
import com.example.rideshare_rest.controllers.RideController;
import com.example.rideshare_rest.controllers.UserController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RideModelAssembler implements RepresentationModelAssembler<RideResponse,
        EntityModel<RideResponse>> {
    @Override
    public EntityModel<RideResponse> toModel(RideResponse ride) {
        EntityModel<RideResponse> model = EntityModel.of(ride,
                linkTo(methodOn(RideController.class).getRideById(ride.getId())).withSelfRel(),
                linkTo(methodOn(RideController.class).getAllRides(null, null, null, null, 0, 20)).withRel("collection")
        );
        if (ride.getDriver() != null) {
            model.add(linkTo(methodOn(UserController.class)
                    .getUserById(ride.getDriver().getId())).withRel("driver"));
        }
        if (RideStatus.ACTIVE.equals(ride.getStatus())) {
            model.add(linkTo(methodOn(BookingController.class)
                    .createBooking(null)).withRel("book_ride"));
        }
        return model;
    }
}
