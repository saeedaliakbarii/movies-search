package com.saeed.movie.moviessearch.dto.links;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class FindAcquaintanceLinksResult {

    private String resultStatus;

    private String sourceName;
    private String targetName;

    private long degrees;

    @Singular
    private List<Link> links;

}
