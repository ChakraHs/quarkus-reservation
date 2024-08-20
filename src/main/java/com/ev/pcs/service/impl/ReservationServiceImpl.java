package com.ev.pcs.service.impl;

import com.ev.pcs.domain.Reservation;
import com.ev.pcs.kafka.KafkaProducer;
import com.ev.pcs.repository.ReservationRepository;
import com.ev.pcs.service.ReservationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class ReservationServiceImpl implements ReservationService {

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    KafkaProducer kafkaProducer;

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.listAll();
    }

    @Override
    public Reservation getReservationById(ObjectId id) {
        return reservationRepository.findById(id);
    }

    @Override
    public Reservation createReservation(Reservation reservation) {
        reservationRepository.persist(reservation);
        kafkaProducer.sendReservation(reservation.toString()); // Send the reservation to Kafka
        return reservation;
    }

    @Override
    public Reservation updateReservation(ObjectId id, Reservation reservation) {
        Reservation existingReservation = reservationRepository.findById(id);
        if (existingReservation != null) {
            existingReservation.setUserId(reservation.getUserId());
            existingReservation.setChargingStationId(reservation.getChargingStationId());
            existingReservation.setVehicleType(reservation.getVehicleType());
            existingReservation.setVehicleId(reservation.getVehicleId());
            existingReservation.setStartTime(reservation.getStartTime());
            existingReservation.setEndTime(reservation.getEndTime());
            existingReservation.setStatus(reservation.getStatus());
            reservationRepository.persist(existingReservation);
        }
        return existingReservation;
    }

    @Override
    public boolean deleteReservation(ObjectId id) {
        return reservationRepository.deleteById(id);
    }
}
