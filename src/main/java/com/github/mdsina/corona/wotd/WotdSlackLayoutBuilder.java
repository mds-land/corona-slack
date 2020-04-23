package com.github.mdsina.corona.wotd;

import static com.slack.api.model.block.Blocks.context;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;

import com.github.mdsina.corona.slack.SlackLayoutBuilder;
import com.slack.api.model.block.LayoutBlock;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Singleton;

@Singleton
public class WotdSlackLayoutBuilder implements SlackLayoutBuilder {

    public static final String TYPE = "wotd";

    @Override
    public List<LayoutBlock> buildBlocks(Map<?, ?> data) {
        Wotd wotd = (Wotd) Objects.requireNonNull(data.get("wotd"));

        return List.of(
            section(s -> s.text(markdownText(":symbols: Слово дня по версии Urban Dictionary:"))),
            section(s -> s.text(markdownText(String.format(
                "*<%s|%s>* --> %s\n",
                wotd.getWotdLink(), wotd.getWotd(), wotd.getMeaning()
            )))),
            context(s -> s.elements(List.of(markdownText(String.format(
                "*Пример*: _%s_", wotd.getExample()
            ))))),
            divider()
        );
    }

    @Override
    public Object getType() {
        return TYPE;
    }
}
