package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ItemDto {

    private Long id;

    @NotBlank(groups = OnCreate.class)
    @Pattern(regexp = ".*\\S.*", groups = OnUpdate.class)
    private String name;

    @NotBlank(groups = OnCreate.class)
    @Pattern(regexp = ".*\\S.*", groups = OnUpdate.class)
    private String description;

    @NotNull(groups = OnCreate.class)
    private Boolean available;

    private List<CommentDto> comments = new ArrayList<>();

    public ItemDto(
            Long id,
            String name,
            String description,
            Boolean available
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
    }
}
