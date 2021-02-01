package com.github.mdsina.corona.corona;

import com.github.mdsina.corona.corona.chart.CoronaChartLayoutBuilder;
import com.github.mdsina.corona.corona.discord.CoronaDiscordLayoutTemplater;
import com.github.mdsina.corona.corona.layout.CoronaLayoutBuilder;
import com.github.mdsina.corona.corona.slack.CoronaSlackLayoutBuilder;
import com.github.mdsina.corona.slack.SlackLayoutApplier;
import com.github.mdsina.corona.slack.SlackLayoutEntity;
import com.slack.api.model.block.LayoutBlock;
import io.micronaut.core.util.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Singleton
public class CoronaSlackDataService {

    private static final List<String> MAIN_COUNTRIES = List.of("russia", "ukraine", "new zealand", "uzbekistan");
    private static final List<String> TOP_COUNTRIES = List.of("world", "usa", "spain", "italy");

    private final SlackLayoutApplier layoutApplier;
    private final CoronaStatsDataProvider dataProvider;
    private final CoronaDiscordLayoutTemplater discordLayoutTemplater;

    public CoronaSlackDataService(
        SlackLayoutApplier layoutApplier,
        CoronaStatsDataProvider dataProvider,
        CoronaDiscordLayoutTemplater discordLayoutTemplater
    ) {
        this.layoutApplier = layoutApplier;
        this.dataProvider = dataProvider;
        this.discordLayoutTemplater = discordLayoutTemplater;
    }

    // TODO: refactor
    public Mono<List<Map<String, String>>> getDiscordSectionedActualStats(List<String> sections) {
        List<List<String>> processedSections = getProcessedSections(sections);
        if (processedSections.isEmpty()) {
            return Mono.just(List.of());
        }
        return dataProvider.get2DaysData()
            .flatMap(daysData ->
                Mono.fromCallable(() ->
                    CoronaLayoutBuilder.withTemplater(discordLayoutTemplater)
                        .buildLayout(Map.of(
                            "todayStat", daysData.get("todayStat"),
                            "yesterdayStat", daysData.get("yesterdayStat"),
                            "countries", processedSections
                        ))
                ).subscribeOn(Schedulers.elastic())
            );
    }

    public Mono<List<LayoutBlock>> getSectionedActualStatsBlocks(List<String> sections) {
        return getActualStatsBlocksInternal(getProcessedSections(sections));
    }

    public Mono<List<LayoutBlock>> getActualStatsBlocks(List<String> countries) {
        return getActualStatsBlocksInternal(getCountries(countries));
    }

    private Mono<List<LayoutBlock>> getActualStatsBlocksInternal(List<List<String>> sections) {
        if (sections.isEmpty()) {
            return Mono.just(List.of());
        }
        return dataProvider.get2DaysData()
            .flatMap(daysData -> layoutApplier.getBlocksFromEntity(
                SlackLayoutEntity.builder()
                    .layoutBuilderType(CoronaSlackLayoutBuilder.TYPE)
                    .layoutData(Map.of(
                        "todayStat", daysData.get("todayStat"),
                        "yesterdayStat", daysData.get("yesterdayStat"),
                        "countries", sections
                    ))
                    .build()
            ));
    }

    public Mono<List<LayoutBlock>> getHistoricalStatsBlocks(List<String> countries) {
        List<List<String>> cList = getCountries(countries);
        return dataProvider.getHistoricalAllData(cList.stream().flatMap(List::stream).collect(Collectors.toList()))
            .flatMap(daysData -> layoutApplier.getBlocksFromEntity(
                SlackLayoutEntity.builder()
                    .layoutBuilderType(CoronaChartLayoutBuilder.TYPE)
                    .layoutData(Map.of(
                        "data", daysData,
                        "countries", cList
                    ))
                    .build()
            ));
    }

    private List<List<String>> getCountries(List<String> countries) {
        return countries == null || countries.isEmpty()
            ? List.of(MAIN_COUNTRIES, TOP_COUNTRIES)
            : List.of(countries);
    }

    private List<List<String>> getProcessedSections(List<String> rawSections) {
        // TODO: do that more efficient
        List<List<String>> tokenizedSections = new ArrayList<>();

        HashSet<String> tokens = new HashSet<>();

        for (String section : rawSections) {
            String[] tokenizedSection = StringUtils.tokenizeToStringArray(section, ",");
            if (tokenizedSection.length == 0) {
                continue;
            }

            var res = new ArrayList<String>();

            for (String token : tokenizedSection) {
                String lcToken = token.toLowerCase();
                if (!tokens.contains(lcToken)) {
                    tokens.add(lcToken);
                    res.add(lcToken);
                }
            }

            tokenizedSections.add(res);
        }

        return tokenizedSections;
    }
}
