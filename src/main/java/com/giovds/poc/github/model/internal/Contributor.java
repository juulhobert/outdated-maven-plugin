package com.giovds.poc.github.model.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class Contributor {
    private String username;
    private int commitsCount;
}
