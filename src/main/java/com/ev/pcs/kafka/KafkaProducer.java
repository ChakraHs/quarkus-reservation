package com.ev.pcs.kafka;

import com.ev.pcs.domain.Reservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class KafkaProducer {
    @Inject
    @Channel("reservation-channel")
    Emitter<String> emitter;

    @Inject
    ObjectMapper objectMapper;

    public Uni<Object> sendReservation(Reservation reservation) {
        return Uni.createFrom().item(reservation)
                .onItem().transformToUni(res -> {
                    try {
                        // Convert Reservation to JSON
                        String jsonString = objectMapper.writeValueAsString(res);

                        // Send JSON string to Kafka and adapt CompletionStage to Uni
                        return Uni.createFrom().completionStage(emitter.send(jsonString))
                                .onItem().transform(success -> {
                                    System.out.println("Reservation sent to Kafka");
                                    return null; // Return null as we are using Uni<Void>
                                })
                                .onFailure().recoverWithUni(failure -> {
                                    System.err.println("Failed to send reservation to Kafka: " + failure.getMessage());
                                    return Uni.createFrom().voidItem(); // Recover with an empty Uni
                                });
                    } catch (Exception e) {
                        System.err.println("Error serializing reservation to JSON: " + e.getMessage());
                        return Uni.createFrom().failure(e); // Return a failed Uni
                    }
                });
//        try {
//            // Convert ReservationDto to JSON
//            String jsonString = objectMapper.writeValueAsString(reservation);
//
//            // Send JSON string to Kafka
//            emitter.send(jsonString)
//                    .thenAccept(success -> System.out.println("Reservation sent to Kafka"))
//                    .exceptionally(failure -> {
//                        System.err.println("Failed to send reservation to Kafka: " + failure.getMessage());
//                        return null;
//                    });
//        } catch (Exception e) {
//            System.err.println("Error serializing reservation to JSON: " + e.getMessage());
//        }
    }
}
