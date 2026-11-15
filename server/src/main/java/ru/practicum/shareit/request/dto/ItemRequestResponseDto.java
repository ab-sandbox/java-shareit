package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemRequestResponseDto {
    private Long id;
    private String name;
    private Long ownerId;
}
