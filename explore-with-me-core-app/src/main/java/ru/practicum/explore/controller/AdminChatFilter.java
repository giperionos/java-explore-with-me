package ru.practicum.explore.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explore.dto.ChatStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChatFilter {
    private List<Long> eventIds;
    private String rangeStartEncoded;
    private String rangeEndEncoded;
    private ChatStatus status;
    private Integer from;
    private Integer size;
}
