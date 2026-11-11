package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody ItemRequestCreateDto dto) {
        return requestService.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> findByRequester(
            @RequestHeader(USER_ID_HEADER) Long userId) {
        return requestService.findByRequester(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAllExceptRequester(
            @RequestHeader(USER_ID_HEADER) Long userId) {
        return requestService.findAllExceptRequester(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long requestId) {
        return requestService.getById(userId, requestId);
    }
}
