package com.github.mdsina.corona.corona;

import static com.slack.api.model.block.Blocks.context;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static java.util.Objects.requireNonNull;

import com.github.mdsina.corona.slack.SlackLayoutBuilder;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.BlockElements;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javax.inject.Singleton;

@SuppressWarnings("unchecked")
@Singleton
public class CoronaSlackLayoutBuilder implements SlackLayoutBuilder {

    public static final String TYPE = "corona";

    private static final List<String> MAIN_COUNTRIES = List.of("russia", "ukraine", "new zealand", "uzbekistan");
    private static final List<String> TOP_COUNTRIES = List.of("world", "usa", "spain", "italy");

    private static final List<String> FUNNY_TPLS = List.of(
        "%s is no more...",
        "%s was burnt to ashes",
        "%s was destroyed to ruins",
        "%s is desolated by corona",
        "Unknown country %s",
        "Your %s is in another castle",
        "Say hello to my little %s"
    );

    @Override
    public List<LayoutBlock> buildBlocks(Map<Object, ?> data) {
        Map<String, Map> stat = (Map<String, Map>) requireNonNull(
            data.get("allCountriesStat"),
            "Stat data is required"
        );
        List<String> countries = (List<String>) data.get("countries");

        if (countries != null && !countries.isEmpty()) {
            return getBlocksLayoutForCountries(countries, stat);
        }

        List<LayoutBlock> blocks = getBlocksLayoutForCountries(MAIN_COUNTRIES, stat);
        blocks.addAll(blocks.size(), getBlocksLayoutForCountries(TOP_COUNTRIES, stat));

        return blocks;
    }

    @Override
    public Object getType() {
        return TYPE;
    }

    public List<LayoutBlock> getBlocksLayoutForCountries(List<String> countries, Map<String, Map> allCountriesStat) {
        List<LayoutBlock> blocks = createBlocks(countries, allCountriesStat);
        blocks.add(blocks.size(), divider());

        return blocks;
    }

    private static List<LayoutBlock> createBlocks(List<String> countries, Map<String, Map> allStats) {
        var actualOrderedKeys = new ArrayList<>();
        var blocks = new HashMap<Object, LayoutBlock>();

        for (String country : countries) {
            String lcCountry = country.toLowerCase();

            Optional<Object> countryStats = Optional
                .ofNullable(allStats.get("named").get(lcCountry))
                .or(() -> Optional.ofNullable(allStats.get("iso2").get(lcCountry)))
                .or(() -> Optional.ofNullable(allStats.get("iso3").get(lcCountry)));

            if (countryStats.isEmpty()) {
                if (blocks.containsKey(lcCountry)) {
                    continue;
                }
                String tpl = FUNNY_TPLS.get(new Random().nextInt(FUNNY_TPLS.size()));
                blocks.put(lcCountry, section(s -> s.text(markdownText(String.format(tpl, country)))));
                actualOrderedKeys.add(lcCountry);
                continue;
            }

            Map stats = (Map) countryStats.get();
            Object realCountryName = stats.get("country");

            if (blocks.containsKey(realCountryName)) {
                continue;
            }

            String flagUrl = (String) ((Map) stats.get("countryInfo")).get("flag");

            blocks.put(realCountryName, context(List.of(
                BlockElements.image(s -> s.imageUrl(flagUrl).altText("Country Flag")),
                markdownText(String.format(
                    "*%s*: :pill: %s (*+%s*)  :skull_and_crossbones: %s (*+%s*)",
                    stats.get("country"),
                    stats.get("cases"),
                    stats.get("todayCases"),
                    stats.get("deaths"),
                    stats.get("todayDeaths")
                ))
            )));
            actualOrderedKeys.add(realCountryName);
        }

        var result = new ArrayList<LayoutBlock>();
        actualOrderedKeys.forEach(k -> result.add(blocks.get(k)));

        return result;
    }
}
