package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("""
            SELECT r
            FROM ItemRequest r
            WHERE r.requester.id = :userId
            ORDER BY r.created DESC
            """)
    List<ItemRequest> findByRequester(@Param("userId") Long userId);

    @Query("""
            SELECT r
            FROM ItemRequest r
            WHERE r.requester.id <> :userId
            ORDER BY r.created DESC
            """)
    List<ItemRequest> findAllExceptRequester(@Param("userId") Long userId);
}
