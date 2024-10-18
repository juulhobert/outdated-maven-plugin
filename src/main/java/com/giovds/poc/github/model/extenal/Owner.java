package com.giovds.poc.github.model.extenal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Owner {
    @Nullable
    private String name;

    @Nullable
    private String email;

    @Nonnull
    private String login = "";

    private long id;

    @Nonnull
    private String nodeId = "";

    @Nonnull
    private String avatarUrl = "";

    @Nullable
    private String gravatarId;

    @Nonnull
    private String url = "";

    @Nonnull
    private String htmlUrl = "";

    @Nonnull
    private String followersUrl = "";

    @Nonnull
    private String followingUrl = "";

    @Nonnull
    private String gistsUrl = "";

    @Nonnull
    private String starredUrl = "";

    @Nonnull
    private String subscriptionsUrl = "";

    @Nonnull
    private String organizationsUrl = "";

    @Nonnull
    private String reposUrl = "";

    @Nonnull
    private String eventsUrl = "";

    @Nonnull
    private String receivedEventsUrl = "";

    @Nonnull
    private String type = "";

    private boolean siteAdmin;

    @Nullable
    private OffsetDateTime starredAt;
}
