package com.ev.pcs.resource;

import com.ev.pcs.domain.Reservation;
import com.ev.pcs.service.ReservationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

import java.util.List;

@Path("/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservationResource {

    @Inject
    ReservationService reservationService;

    @GET
    @Path("/test")
    public Response getTest() {
        return Response.ok("Test passed").build();
    }

    @POST
    public Response create(Reservation reservation) {
        Reservation createdReservation = reservationService.createReservation(reservation);
        return Response.status(Response.Status.CREATED)
                .entity(createdReservation)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") String id) {
        ObjectId objectId = new ObjectId(id);
        Reservation reservation = reservationService.getReservationById(objectId);

        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Reservation not found")
                    .build();
        }

        return Response.ok(reservation).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") String id, Reservation reservation) {
        ObjectId objectId = new ObjectId(id);
        Reservation updatedReservation = reservationService.updateReservation(objectId, reservation);

        if (updatedReservation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Reservation not found")
                    .build();
        }

        return Response.ok(updatedReservation).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        ObjectId objectId = new ObjectId(id);
        boolean deleted = reservationService.deleteReservation(objectId);

        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Reservation not found")
                    .build();
        }
    }

    @GET
    public Response getAll() {
        List<Reservation> reservations = reservationService.getAllReservations();
        return Response.ok(reservations).build();
    }
}
