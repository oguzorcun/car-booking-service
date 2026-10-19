package com.velocitymotors.carbooking.persistence;

import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Mocked vehicle repo. To be swapped with actual vehicle db on production
 */
@Repository
public class InMemoryVehicleRepository {

    private static final Set<String> VALID_VEHICLE_IDS =
            Set.of("VEH-001", "VEH-002", "VEH-003", "VEH-004", "VEH-005");

    public boolean existsById(String vehicleId) {
        return VALID_VEHICLE_IDS.contains(vehicleId);
    }
}
