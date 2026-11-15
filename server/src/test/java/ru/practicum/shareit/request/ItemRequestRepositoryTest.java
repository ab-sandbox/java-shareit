package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;

    private User requester;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requester = userRepository.save(
                new User(null, "Alexey", "alexey@example.com")
        );

        otherUser = userRepository.save(
                new User(null, "Ivan", "ivan@example.com")
        );
    }

    @Test
    void shouldFindRequestsByRequesterOrderedByCreatedDesc() {
        ItemRequest older = saveRequest(
                "Нужна дрель",
                requester,
                LocalDateTime.of(2026, 11, 1, 10, 0)
        );

        ItemRequest newer = saveRequest(
                "Нужна лестница",
                requester,
                LocalDateTime.of(2026, 11, 2, 10, 0)
        );

        saveRequest(
                "Нужен велосипед",
                otherUser,
                LocalDateTime.of(2026, 11, 3, 10, 0)
        );

        List<ItemRequest> result =
                requestRepository.findByRequester(requester.getId());

        assertEquals(2, result.size());
        assertEquals(newer.getId(), result.get(0).getId());
        assertEquals(older.getId(), result.get(1).getId());
    }

    @Test
    void shouldFindOtherUsersRequestsOrderedByCreatedDesc() {
        saveRequest(
                "Собственный запрос",
                requester,
                LocalDateTime.of(2026, 11, 3, 10, 0)
        );

        ItemRequest older = saveRequest(
                "Нужна дрель",
                otherUser,
                LocalDateTime.of(2026, 11, 1, 10, 0)
        );

        ItemRequest newer = saveRequest(
                "Нужна лестница",
                otherUser,
                LocalDateTime.of(2026, 11, 2, 10, 0)
        );

        List<ItemRequest> result =
                requestRepository.findAllExceptRequester(requester.getId());

        assertEquals(2, result.size());
        assertEquals(newer.getId(), result.get(0).getId());
        assertEquals(older.getId(), result.get(1).getId());
    }

    @Test
    void shouldReturnEmptyListWhenRequesterHasNoRequests() {
        saveRequest(
                "Нужна дрель",
                otherUser,
                LocalDateTime.of(2026, 11, 1, 10, 0)
        );

        List<ItemRequest> result =
                requestRepository.findByRequester(requester.getId());

        assertTrue(result.isEmpty());
    }

    private ItemRequest saveRequest(
            String description,
            User user,
            LocalDateTime created) {

        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(user);
        request.setCreated(created);

        return requestRepository.save(request);
    }
}
