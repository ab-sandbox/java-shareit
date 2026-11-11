package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeRequestWithItems() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(
                1L,
                "Нужна дрель",
                LocalDateTime.of(2026, 11, 11, 10, 30)
        );
        dto.setItems(List.of(
                new ItemRequestResponseDto(2L, "Дрель", 3L)
        ));

        assertThat(json.write(dto))
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);

        assertThat(json.write(dto))
                .extractingJsonPathStringValue("$.description")
                .isEqualTo("Нужна дрель");

        assertThat(json.write(dto))
                .extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-11-11T10:30:00");

        assertThat(json.write(dto))
                .extractingJsonPathNumberValue("$.items[0].id")
                .isEqualTo(2);

        assertThat(json.write(dto))
                .extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo("Дрель");

        assertThat(json.write(dto))
                .extractingJsonPathNumberValue("$.items[0].ownerId")
                .isEqualTo(3);
    }

    @Test
    void shouldDeserializeRequestWithItems() throws Exception {
        String content = "{\n" +
                         "  \"id\": 1,\n" +
                         "  \"description\": \"Нужна дрель\",\n" +
                         "  \"created\": \"2026-11-11T10:30:00\",\n" +
                         "  \"items\": [\n" +
                         "    {\n" +
                         "      \"id\": 2,\n" +
                         "      \"name\": \"Дрель\",\n" +
                         "      \"ownerId\": 3\n" +
                         "    }\n" +
                         "  ]\n" +
                         "}\n";

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Нужна дрель");
        assertThat(dto.getCreated())
                .isEqualTo(LocalDateTime.of(2026, 11, 11, 10, 30));

        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(3L);
    }
}
