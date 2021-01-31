package com.github.mdsina.corona.corona.slack;


import static com.slack.api.model.block.Blocks.context;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;

import com.github.mdsina.corona.corona.layout.CoronaLayoutTemplater;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.BlockElements;
import java.util.List;
import javax.inject.Singleton;

@Singleton
public class CoronaSlackLayoutTemplater implements CoronaLayoutTemplater<LayoutBlock> {

    @Override
    public LayoutBlock divider() {
        return Blocks.divider();
    }

    @Override
    public LayoutBlock funnySection(String tpl, String country) {
        return section(s -> s.text(markdownText(String.format(tpl, country))));
    }

    @Override
    public LayoutBlock countrySection(CoronaTemplateData data) {
        return context(List.of(
            BlockElements.image(s -> s.imageUrl(data.getFlagUrl()).altText("Country Flag")),
            markdownText(String.format(
                "*%s*: :pill: *%s* / %s  :skull_and_crossbones: *%s* / %s  :yin_yang:  %s / %s",
                data.getCountry(),
                data.getTodayCases(),
                data.getYesterdayCases(),
                data.getTodayDeaths(),
                data.getYesterdayDeaths(),
                data.getTotalCases(),
                data.getTotalDeaths()
            ))
        ));
    }
}
