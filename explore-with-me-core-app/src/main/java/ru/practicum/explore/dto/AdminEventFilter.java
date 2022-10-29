package ru.practicum.explore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEventFilter {
    private Long[] usersIds;
    private List<EventState> states;
    private Long[] categoriesIds;
    private String rangeStartEncoded;
    private String rangeEndEncoded;
    private Integer from;
    private Integer size;
}
