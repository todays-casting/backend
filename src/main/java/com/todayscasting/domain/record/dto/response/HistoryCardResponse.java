package com.todayscasting.domain.record.dto.response;

import java.time.LocalDate;
import java.util.List;

public record HistoryCardResponse (

    Long recordId,
    LocalDate recordDate,
    List<String> mood,
    String content,
    String title,
    String genre,
    String roleName,
    String highlight,
    String oneLineComment,
    Boolean isFavorite

) {}
