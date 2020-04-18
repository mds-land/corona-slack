package com.github.mdsina.corona.slack;

import com.slack.api.model.block.LayoutBlock;
import java.util.List;
import java.util.Map;

public interface SlackLayoutBuilder {

    List<LayoutBlock> buildBlocks(Map<Object, ?> data);
    Object getType();
}
