package com.ev.pcs.repository;

import com.ev.pcs.domain.Reservation;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReservationRepository implements PanacheMongoRepository<Reservation> {
}
