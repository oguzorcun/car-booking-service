package com.velocitymotors.carbooking.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingJpaRepository extends JpaRepository<BookingEntity, String> {

}
