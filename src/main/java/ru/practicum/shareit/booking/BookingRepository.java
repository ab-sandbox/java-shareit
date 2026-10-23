
package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.booker.id = :bookerId
            ORDER BY b.start DESC
            """)
    List<Booking> findByBooker(@Param("bookerId") Long bookerId);

    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.item.owner.id = :ownerId
            ORDER BY b.start DESC
            """)
    List<Booking> findByOwner(@Param("ownerId") Long ownerId);
}
