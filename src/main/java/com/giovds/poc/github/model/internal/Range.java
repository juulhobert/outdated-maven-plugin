package com.giovds.poc.github.model.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class Range {
    private OffsetDateTime start;
    private OffsetDateTime end;
    private List<Point> points;
}
