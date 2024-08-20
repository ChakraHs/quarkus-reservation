package com.ev.pcs.domain;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import org.bson.types.ObjectId;
import java.time.Instant;

@Data
@MongoEntity(collection="reservations")
public class Reservation {
    private ObjectId id;
    private String userId;
    private String chargingStationId;
    private String chargingSlotId;
    private VehicleType vehicleType;
    private String vehicleId;
    private Instant startTime;
    private Instant endTime;
    private ReservationStatus status;
}

enum VehicleType {
    CAR, BIKE, TRUCK
}

enum ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}
