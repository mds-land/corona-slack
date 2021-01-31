package com.github.mdsina.corona.corona.slack;

import com.github.mdsina.corona.corona.layout.CoronaLayoutBuilder;
import com.github.mdsina.corona.slack.SlackLayoutBuilder;
import com.slack.api.model.block.LayoutBlock;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class CoronaSlackLayoutBuilder implements SlackLayoutBuilder {

    public static final String TYPE = "corona";

    private final CoronaSlackLayoutTemplater templater;

    public CoronaSlackLayoutBuilder(CoronaSlackLayoutTemplater templater) {
        this.templater = templater;
    }

    @Override
    public List<LayoutBlock> buildBlocks(Map<?, ?> data) {
        return CoronaLayoutBuilder.withTemplater(templater).buildLayout(data);
    }

    @Override
    public Object getType() {
        return TYPE;
    }
}
