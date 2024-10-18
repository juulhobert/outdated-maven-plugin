package com.giovds.poc.github.model.extenal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Repository {
    @Nonnull
    private String contributorsUrl = "";

    private int forksCount;

    @Nonnull
    private String fullName = "";

    private boolean hasIssues;

    private String homepage;

    private long id;

    @Nonnull
    private String nodeId = "";

    @Nonnull
    private String name = "";

    @Nonnull
    private Owner owner = new Owner();

    @JsonProperty("private")
    private boolean _private;

    private int stargazersCount;

    private int subscribersCount;

    @Nonnull
    private String url = "";
}
