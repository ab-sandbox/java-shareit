package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody ItemRequestCreateDto dto
    ) {
        return requestClient.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> findByRequester(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        return requestClient.findByRequester(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllExceptRequester(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        return requestClient.findAllExceptRequester(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long requestId
    ) {
        return requestClient.getById(userId, requestId);
    }
}
