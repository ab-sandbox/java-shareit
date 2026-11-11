package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User requester;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        requester = new User(1L, "Alex", "alex@example.com");

        request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Нужна лестница");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now().minusHours(1));
    }

    @Test
    void shouldCreateRequest() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Нужна лестница");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(requester));

        when(requestRepository.save(any(ItemRequest.class)))
                .thenAnswer(invocation -> {
                    ItemRequest saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        ItemRequestDto result = requestService.create(1L, dto);

        assertEquals(10L, result.getId());
        assertEquals("Нужна лестница", result.getDescription());
        assertNotNull(result.getCreated());

        verify(requestRepository).save(argThat(saved ->
                saved.getRequester().getId().equals(1L)
                && saved.getDescription().equals("Нужна лестница")
                && saved.getCreated() != null
        ));
    }

    @Test
    void shouldFindRequestsByRequester() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findByRequester(1L))
                .thenReturn(List.of(request));

        List<ItemRequestDto> result = requestService.findByRequester(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());

        verify(requestRepository).findByRequester(1L);
    }

    @Test
    void shouldFindRequestsFromOtherUsers() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(requestRepository.findAllExceptRequester(2L))
                .thenReturn(List.of(request));

        List<ItemRequestDto> result =
                requestService.findAllExceptRequester(2L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());

        verify(requestRepository).findAllExceptRequester(2L);
    }

    @Test
    void shouldGetRequestById() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(requestRepository.findById(10L))
                .thenReturn(Optional.of(request));

        ItemRequestDto result = requestService.getById(2L, 10L);

        assertEquals(10L, result.getId());
        assertEquals("Нужна лестница", result.getDescription());
    }

    @Test
    void shouldThrowWhenRequesterNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> requestService.findByRequester(999L));

        verifyNoInteractions(requestRepository);
    }

    @Test
    void shouldThrowWhenRequestNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> requestService.getById(1L, 999L));
    }

    @Test
    void shouldThrowWhenCreatingRequestForUnknownUser() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Нужна лестница");

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> requestService.create(999L, dto));

        verify(requestRepository, never()).save(any());
    }
}
