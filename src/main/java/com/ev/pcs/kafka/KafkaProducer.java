package com.ev.pcs.kafka;

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

    public void sendReservation(String reservation) {
        emitter.send(reservation)
                .thenAccept(success -> System.out.println("Reservation sent to Kafka"))
                .exceptionally(failure -> {
                    System.err.println("Failed to send reservation to Kafka: " + failure.getMessage());
                    return null;
                });
    }
}
