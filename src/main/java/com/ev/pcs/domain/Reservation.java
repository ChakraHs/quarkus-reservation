package com.ev.pcs.domain;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import org.bson.types.ObjectId;
import java.time.Instant;
import java.time.ZonedDateTime;

@Data
@MongoEntity(collection="reservations")
public class Reservation {
    private ObjectId id;
    private String userId;
    private String chargingSlotId;
    private VehicleType vehicleType;
    private String vehicleId;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private ReservationStatus status;
}

enum VehicleType {
    CAR, BIKE, TRUCK
}

enum ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}
