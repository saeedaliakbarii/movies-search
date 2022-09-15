package com.saeed.movie.moviessearch.domain;

import lombok.*;


import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode


public class AkaId implements Serializable {

    private String titleId;
    private Long ordering;
}
