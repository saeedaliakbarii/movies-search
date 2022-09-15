package com.saeed.movie.moviessearch.domain;

import lombok.*;


import java.util.List;

/*
    Contains the principal cast/crew for titles

    tconst (string) - alphanumeric unique identifier of the title
    ordering (integer) – a number to uniquely identify rows for a given titleId

    nconst (string) - alphanumeric unique identifier of the name/person
    category (string) - the category of job that person was in
    job (string) - the specific job title if applicable, else '\N'
    characters (string) - the name of the character played if applicable, else '\N'
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor



public class Principal {


    private PrincipalId id;

    private String nconst;
    private String category;

    private String job;

    private String characters;

    public Principal(List<String> data) {
        this(data.get(0), data.get(1), data.get(2), data.get(3), data.get(4), data.get(5));
    }

    private Principal(String tconst,
                      String ordering,
                      String nconst,
                      String category,
                      String job,
                      String characters) {

        this.id = new PrincipalId(tconst, Long.parseLong(ordering));

        this.nconst = nconst;
        this.category = category;
        this.job = job;
        this.characters = characters;
    }

}
