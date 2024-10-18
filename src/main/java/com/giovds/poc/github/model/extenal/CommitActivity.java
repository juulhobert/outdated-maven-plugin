package com.giovds.poc.github.model.extenal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CommitActivity {
    /**
     * The total number of commits for the week.
     */
    @Setter
    private int total;

    /**
     * The start of the week as a UNIX timestamp.
     */
    @Setter
    private long week;

    /**
     * The number of commits for each day of the week. 0 = Sunday, 1 = Monday, etc.
     */
    @Nonnull
    private final List<Integer> days = List.of();
}
