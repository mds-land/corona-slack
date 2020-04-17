package com.github.mdsina.corona.slack;

import static com.slack.api.model.block.Blocks.context;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;

import com.github.mdsina.corona.CoronaStatsClient;
import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.BlockElements;
import io.micronaut.context.annotation.Value;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.inject.Singleton;

@Singleton
public class SlackStatsProducer {

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

    private final AsyncMethodsClient slackMethodsClient;
    private final CoronaStatsClient coronaStatsClient;
    private final String channel;

    public SlackStatsProducer(
        AsyncMethodsClient slackMethodsClient,
        CoronaStatsClient coronaStatsClient,
        @Value("${slack.channel:}") String channel
    ) {
        this.slackMethodsClient = slackMethodsClient;
        this.coronaStatsClient = coronaStatsClient;
        this.channel = channel;
    }

    /**
     * Send stats about coronavirus
     */
    public ChatPostMessageResponse sendStats() throws ExecutionException, InterruptedException {
        return slackMethodsClient.chatPostMessage(
            ChatPostMessageRequest.builder()
                .channel(channel)
                .blocks(getSlackBlocksWithDataForTop())
                .build()
        ).get();
    }

    public List<LayoutBlock> getSlackBlocksWithDataForTop() {
        Map<String, Map> allCountriesStat = getAllCountriesStat();

        List<LayoutBlock> blocks = createBlocks(MAIN_COUNTRIES, allCountriesStat);
        blocks.add(blocks.size(), divider());
        blocks.addAll(blocks.size(), createBlocks(TOP_COUNTRIES, allCountriesStat));

        return blocks;
    }

    public List<LayoutBlock> getSlackBlocksWithData(List<String> countries) {
        Map<String, Map> allCountriesStat = getAllCountriesStat();

        List<LayoutBlock> blocks = createBlocks(countries, allCountriesStat);
        blocks.add(blocks.size(), divider());

        return blocks;
    }

    private static List<LayoutBlock> createBlocks(List<String> countries, Map<String, Map> allStats) {
        return countries.stream()
            .map(country -> {
                String lcCountry = country.toLowerCase();

                Optional<Object> countryStats = Optional
                    .ofNullable(allStats.get("named").get(lcCountry))
                    .or(() -> Optional.ofNullable(allStats.get("iso2").get(lcCountry)))
                    .or(() -> Optional.ofNullable(allStats.get("iso3").get(lcCountry)));

                if (countryStats.isEmpty()) {
                    String tpl = FUNNY_TPLS.get(new Random().nextInt(FUNNY_TPLS.size()));
                    return section(s -> s.text(markdownText(String.format(tpl, country))));
                }

                Map stats = (Map) countryStats.get();

                String flagUrl = (String) ((Map) stats.get("countryInfo")).get("flag");

                return context(List.of(
                    BlockElements.image(s -> s.imageUrl(flagUrl).altText("Country Flag")),
                    markdownText(String.format(
                        "*%s*: :pill: %s (*+%s*)  :skull_and_crossbones: %s (*+%s*)",
                        stats.get("country"),
                        stats.get("cases"),
                        stats.get("todayCases"),
                        stats.get("deaths"),
                        stats.get("todayDeaths")
                    ))
                ));
            })
            .collect(Collectors.toList());
    }

    private Map<String, Map> getAllCountriesStat() {
        List<Map> stats = coronaStatsClient.getAllCountriesStat().blockingGet();
        Map worldStat = coronaStatsClient.getWorldStat().blockingGet();
        worldStat.put("country", "World");
        worldStat.put("countryInfo", Map.of(
            "iso2", "World",
            "iso3", "World",
            "flag", "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a9/Emoji_u1f30d.svg/1024px-Emoji_u1f30d.svg.png"
        ));
        stats.add(worldStat);

        var namedMap = new HashMap<>(stats.size());
        var iso2Map = new HashMap<>(stats.size());
        var iso3Map = new HashMap<>(stats.size());

        for (Map stat : stats) {
            namedMap.put(((String) stat.get("country")).toLowerCase(), stat);

            Map info = (Map) stat.get("countryInfo");
            Object iso2 = info.get("iso2");
            Object iso3 = info.get("iso3");

            if (iso2 != null) {
                iso2Map.put(((String) iso2).toLowerCase(), stat);
            }
            if (iso3 != null) {
                iso3Map.put(((String) iso3).toLowerCase(), stat);
            }
        }

        return Map.of(
            "named", namedMap,
            "iso2", iso2Map,
            "iso3", iso3Map
        );
    }
}
