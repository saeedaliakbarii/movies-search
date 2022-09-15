package com.saeed.movie.moviessearch.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode


public class PrincipalId implements Serializable {

    private String tconst;
    private Long ordering;
}
