package com.github.mdsina.corona.wotd;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Wotd {

    private final String dictionary;
    private final String wotd;
    private final String wotdLink;
    private final String meaning;
    private final String example;
}
