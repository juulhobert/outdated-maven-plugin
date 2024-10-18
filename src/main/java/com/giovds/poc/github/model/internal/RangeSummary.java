package com.giovds.poc.github.model.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
@Builder
public class RangeSummary {
    private OffsetDateTime start;
    private OffsetDateTime end;
    private int count;
}
