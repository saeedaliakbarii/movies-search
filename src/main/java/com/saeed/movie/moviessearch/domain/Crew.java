package com.saeed.movie.moviessearch.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
    Contains the director and writer information for all the titles in IMDb. Fields include:

    tconst (string) - alphanumeric unique identifier of the title
    directors (array of nconsts) - director(s) of the given title
    writers (array of nconsts) – writer(s) of the given title
 */

@Getter
@Setter
@ToString
@NoArgsConstructor


public class Crew {


    private String tconst;


    private String directors;


    private String writers;

    public Crew(List<String> data) {
        this(data.get(0), data.get(1), data.get(2));
    }

    private Crew(String tconst, String directors, String writers) {
        this.tconst = tconst;
        this.directors = directors;
        this.writers = writers;
    }

    public List<String> getDirectorsIds() {
        return Collections.unmodifiableList(Arrays.asList(directors.split(",")));
    }

    public List<String> getWritersIds() {
        return Collections.unmodifiableList(Arrays.asList(writers.split(",")));
    }

}
