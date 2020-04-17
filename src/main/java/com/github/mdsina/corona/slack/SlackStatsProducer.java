package com.github.mdsina.corona.slack;

import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;

import com.github.mdsina.corona.CoronaStatsClient;
import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.model.block.LayoutBlock;
import io.micronaut.context.annotation.Value;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.inject.Singleton;

@Singleton
public class SlackStatsProducer {

    private static final Map<String, String> COUNTRY_ICONS = Map.of(
        "Russia", ":flag-ru:",
        "Ukraine", ":flag-ua:",
        "USA", ":flag-us:",
        "Uzbekistan", ":flag-uz:",
        "New Zealand", ":flag-nz:",
        "Spain", ":flag-es:",
        "Italy", ":flag-it:",
        "World", ":globe_with_meridians:"
    );

    private static final List<String> MAIN_COUNTRIES = List.of("Russia", "Ukraine", "New Zealand", "Uzbekistan");
    private static final List<String> TOP_COUNTRIES = List.of("World", "USA", "Spain", "Italy");

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
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void sendStats() throws ExecutionException, InterruptedException {
        slackMethodsClient.chatPostMessage(
            ChatPostMessageRequest.builder()
                .channel(channel)
                .blocks(getSlackBlocksWithData())
//                .blocks(asBlocks(

//                    divider(),
//                    section(s -> s.text(markdownText("Зараженных за сегодня: *+" + stats.get("todayCases") + "*"))),
//                    section(s -> s.text(markdownText("Смертей за сегодня: *+" + stats.get("todayDeaths") + "*"))),
//                    divider(),
//                    section(s -> s.text(markdownText("Всего зараженных: " + stats.get("cases")))),
//                    section(s -> s.text(markdownText("Всего смертей: " + stats.get("deaths")))),
//                    section(s -> s.text(markdownText("Всего излечилось: " + stats.get("recovered")))),
//                    divider(),
//                    section(s -> s.text(markdownText("Количество активно больных: " + stats.get("active")))),
//                    section(s -> s.text(markdownText("В тяжелом состоянии всего: " + stats.get("critical")))),
//                    divider(),
//                    section(s -> s.text(markdownText("Протестировано всего: " + stats.get("tests")))),
//                    divider(),
//                    section(s -> s.text(markdownText(
//                        "Статистика обновлена " + Instant.ofEpochMilli((long) stats.get("updated"))
//                    )))
//                ))
                .build()
        ).get();
    }

    public List<LayoutBlock> getSlackBlocksWithData() {
        Map allCountriesStat = getAllCountriesStat();

        List<LayoutBlock> blocks = createBlocks(MAIN_COUNTRIES, allCountriesStat);
        blocks.add(blocks.size(), divider());
        blocks.addAll(blocks.size(), createBlocks(TOP_COUNTRIES, allCountriesStat));

        return blocks;
    }

    private static List<LayoutBlock> createBlocks(List<String> countries, Map allStats) {
        return countries.stream()
            .map(country -> {
                Map stats = (Map) allStats.get(country);

                return section(s -> s.text(markdownText(String.format(
                    "%s *%s*: :pill: %s (*+%s*)  :skull_and_crossbones: %s (*+%s*)",
                    COUNTRY_ICONS.get(country),
                    country,
                    stats.get("cases"),
                    stats.get("todayCases"),
                    stats.get("deaths"),
                    stats.get("todayDeaths")
                ))));
            })
            .collect(Collectors.toList());
    }

    private Map getAllCountriesStat() {
        List<Map> stats = coronaStatsClient.getAllCountriesStat().blockingGet();
        Map worldStat = coronaStatsClient.getWorldStat().blockingGet();
        worldStat.put("country", "World");
        stats.add(worldStat);

        return stats.stream().collect(Collectors.toMap(v -> v.get("country"), v -> v));
    }
}
