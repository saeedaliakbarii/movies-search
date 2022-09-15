package com.saeed.movie.moviessearch.dto.links;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.Map;

@Getter
@RequiredArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Link {

    private final String type;
    @Singular
    private final Map<String, Object> properties;
}
