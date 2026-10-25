package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b FROM Booking b
            WHERE b.booker.id = :bookerId
            ORDER BY b.start DESC
            """)
    List<Booking> findByBooker(@Param("bookerId") Long bookerId);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.owner.id = :ownerId
            ORDER BY b.start DESC
            """)
    List<Booking> findByOwner(@Param("ownerId") Long ownerId);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.booker.id = :bookerId
              AND b.start <= :now
              AND b.end >= :now
            ORDER BY b.start DESC
            """)
    List<Booking> findCurrentByBooker(
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.booker.id = :bookerId
              AND b.end < :now
            ORDER BY b.start DESC
            """)
    List<Booking> findPastByBooker(
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.booker.id = :bookerId
              AND b.start > :now
            ORDER BY b.start DESC
            """)
    List<Booking> findFutureByBooker(
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.booker.id = :bookerId
              AND b.status = :status
            ORDER BY b.start DESC
            """)
    List<Booking> findByBookerAndStatus(
            @Param("bookerId") Long bookerId,
            @Param("status") BookingStatus status
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.owner.id = :ownerId
              AND b.start <= :now
              AND b.end >= :now
            ORDER BY b.start DESC
            """)
    List<Booking> findCurrentByOwner(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.owner.id = :ownerId
              AND b.end < :now
            ORDER BY b.start DESC
            """)
    List<Booking> findPastByOwner(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.owner.id = :ownerId
              AND b.start > :now
            ORDER BY b.start DESC
            """)
    List<Booking> findFutureByOwner(
            @Param("ownerId") Long ownerId,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.owner.id = :ownerId
              AND b.status = :status
            ORDER BY b.start DESC
            """)
    List<Booking> findByOwnerAndStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") BookingStatus status
    );

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.item.id = :itemId
          AND b.booker.id = :userId
          AND b.status = :status
          AND b.end < :now
        """)
    boolean existsCompletedBooking(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId,
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );
}
