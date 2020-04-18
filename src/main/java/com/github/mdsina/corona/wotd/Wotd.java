package com.github.mdsina.corona.wotd;

import lombok.Data;

@Data
public class Wotd {

    private final String wotd;
    private final String wotdLink;
    private final String meaning;
    private final String example;
}
