package com.saeed.movie.moviessearch.dto.coincidence;


import com.saeed.movie.moviessearch.dto.core.Title;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class FindCoincidenceResult {

    @Singular
    private List<String> executedForNames;

    @Singular
    private List<String> names;

    private long noOfSharedTitles;

    @Singular
    private List<Title> sharedTitles;
}

