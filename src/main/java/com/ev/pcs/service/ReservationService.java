package com.ev.pcs.service;

import com.ev.pcs.domain.Reservation;
import org.bson.types.ObjectId;

import java.util.List;

public interface ReservationService {

    List<Reservation> getAllReservations();

    Reservation getReservationById(ObjectId id);

    Reservation createReservation(Reservation reservation);

    Reservation updateReservation(ObjectId id, Reservation reservation);

    boolean deleteReservation(ObjectId id);
}
