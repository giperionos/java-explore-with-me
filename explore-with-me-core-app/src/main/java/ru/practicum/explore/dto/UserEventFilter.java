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
public class UserEventFilter {
    private String textSearch;
    private List<Long> categoriesIds;
    private Boolean paid;
    private String rangeStartEncoded;
    private String rangeEndEncoded;
    private Boolean onlyAvailableByRequestLimit;
    private EventSortType sortType;
    private Integer from;
    private Integer size;
}

