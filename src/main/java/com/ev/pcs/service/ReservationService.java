package com.ev.pcs.service;

import com.ev.pcs.domain.Reservation;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ReservationService {

    Multi<Reservation> getAllReservations();

    Uni<Reservation> getReservationById(ObjectId id);

    Uni<Reservation> createReservation(Reservation reservation);

    Uni<Reservation> updateReservation(ObjectId id, Reservation reservation);

    Uni<Boolean> deleteReservation(ObjectId id);
}
