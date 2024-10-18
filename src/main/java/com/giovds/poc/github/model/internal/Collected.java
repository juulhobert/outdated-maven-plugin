package com.giovds.poc.github.model.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class Collected {
    private String homepage;
    private int starsCount;
    private int forksCount;
    private int subscribersCount;
    private int issues;
    private List<Contributor> contributors;
    private List<RangeSummary> commits;
}
