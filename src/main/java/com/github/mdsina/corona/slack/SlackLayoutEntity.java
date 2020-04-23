package com.github.mdsina.corona.slack;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
public class SlackLayoutEntity {

    private final Object layoutBuilderType;
    private final Map<?, ?> layoutData;
}
