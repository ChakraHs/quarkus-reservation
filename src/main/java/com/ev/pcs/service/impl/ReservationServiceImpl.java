package com.ev.pcs.service.impl;

import com.ev.pcs.client.SearchServiceClient;
import com.ev.pcs.domain.Reservation;
import com.ev.pcs.kafka.KafkaProducer;
import com.ev.pcs.repository.ReservationRepository;
import com.ev.pcs.service.ReservationService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class ReservationServiceImpl implements ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    @Inject
    ReservationRepository reservationRepository;

    @Inject
    KafkaProducer kafkaProducer;

    @Inject
    @RestClient
    SearchServiceClient searchServiceClient;

    @Override
    public Multi<Reservation> getAllReservations() {
        return reservationRepository.findAll().stream();
    }

    @Override
    public Uni<Reservation> getReservationById(ObjectId id) {
        return reservationRepository.findById(id);
    }

//    @Override
//    public Reservation createReservation(Reservation reservation) {
//        reservationRepository.persist(reservation);
//        kafkaProducer.sendReservation(reservation); // Send the reservation to Kafka
//        return reservation;
//    }

    @Override
    public Uni<Reservation> createReservation(Reservation reservation) {
        logger.info("Starting reservation process for slotId: {}, startTime: {}, endTime: {}",
                reservation.getChargingSlotId(), reservation.getStartTime(), reservation.getEndTime());

        return checkSlotAvailability(reservation.getChargingSlotId(), reservation.getStartTime(), reservation.getEndTime())
                .onItem().transformToUni(isAvailable -> {
                    if (!isAvailable) {
                        logger.warn("Slot {} is not available for the requested time range.", reservation.getChargingSlotId());
                        return Uni.createFrom().failure(new IllegalStateException("Charging slot is not available at the requested time"));
                    }

                    // Proceed with persisting the reservation if the slot is available
                    return reservationRepository.persist(reservation)
                            .onItem().transformToUni(ignored ->
                                    kafkaProducer.sendReservation(reservation)
                                            .onItem().transform(ignoredResult -> {
                                                logger.info("Reservation ID {} sent to Kafka successfully.", reservation.getId());
                                                return reservation;
                                            })
                                            .onFailure().recoverWithUni(e -> {
                                                logger.warn("Failed to send reservation to Kafka for reservation ID {}: {}", reservation.getId(), e.getMessage());
                                                return Uni.createFrom().failure(new RuntimeException("Failed to send reservation to Kafka", e));
                                            })
                            )
                            .onFailure().recoverWithUni(e -> {
                                logger.error("Error persisting reservation for reservation ID {}: {}", reservation.getId(), e.getMessage());
                                return Uni.createFrom().failure(new RuntimeException("Error persisting reservation", e));
                            });
                })
                .onFailure().recoverWithUni(e -> {
                    logger.warn("Unexpected error during reservation creation: ");
                    return Uni.createFrom().failure(new IllegalStateException("Charging slot is not available at the requested time", e));
                });
    }


        // Call the reactive client method
        @CircuitBreaker(requestVolumeThreshold = 5, failureRatio = 0.5, delay = 1000L, successThreshold = 3)
        @Fallback(fallbackMethod = "fallbackSlotAvailability")
        public Uni<Boolean> checkSlotAvailability(String slotId, ZonedDateTime startTime, ZonedDateTime endTime) {
            return searchServiceClient.checkAvailability(slotId, startTime.toString(), endTime.toString())
                    .onItem().transform(response -> {
                        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                            logger.debug("Availability check returned: {}", true);
                            return true;
                        } else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                            logger.warn("Slot {} is not available for the requested time range.", slotId);
                            return false;
                        } else {
                            String errorMsg = "Error checking slot availability: " + response.getStatusInfo();
                            logger.error(errorMsg);
                            throw new RuntimeException(errorMsg);
                        }
                    })
                    .onFailure().recoverWithUni(e -> {
                        // Log the failure and return false if there's an error
                        logger.warn("Failed to check slot availability");
                        return Uni.createFrom().item(false); // Return false in case of failure
                    });
        }

        private Uni<Boolean> fallbackSlotAvailability(String slotId, String startTime, String endTime) {
            logger.warn("Fallback: Assuming slot {} is unavailable due to service failure.", slotId);
            return Uni.createFrom().item(false);
        }




        @Override
    public Uni<Reservation> updateReservation(ObjectId id, Reservation reservation) {
        return reservationRepository.findById(id)
                .onItem().transformToUni(existingReservation -> {
                    if (existingReservation != null) {
                        existingReservation.setUserId(reservation.getUserId());
                        existingReservation.setChargingSlotId(reservation.getChargingSlotId());
                        existingReservation.setVehicleType(reservation.getVehicleType());
                        existingReservation.setVehicleId(reservation.getVehicleId());
                        existingReservation.setStartTime(reservation.getStartTime());
                        existingReservation.setEndTime(reservation.getEndTime());
                        existingReservation.setStatus(reservation.getStatus());
                        return reservationRepository.persist(existingReservation)
                                .onItem().transform(ignored -> existingReservation);
                    } else {
                        return Uni.createFrom().failure(new RuntimeException("Reservation not found"));
                    }
                });
    }

    @Override
    public Uni<Boolean> deleteReservation(ObjectId id) {
        return reservationRepository.deleteById(id);
    }
}
