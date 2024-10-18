package com.giovds.poc.github.model.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@ToString
public class Bucket {
    private OffsetDateTime start;
    private OffsetDateTime end;
}
