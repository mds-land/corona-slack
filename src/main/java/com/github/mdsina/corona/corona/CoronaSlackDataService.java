package com.github.mdsina.corona.corona;

import com.github.mdsina.corona.corona.chart.CoronaChartLayoutBuilder;
import com.github.mdsina.corona.slack.SlackLayoutApplier;
import com.github.mdsina.corona.slack.SlackLayoutEntity;
import com.slack.api.model.block.LayoutBlock;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class CoronaSlackDataService {

    private static final List<String> MAIN_COUNTRIES = List.of("russia", "ukraine", "new zealand", "uzbekistan");
    private static final List<String> TOP_COUNTRIES = List.of("world", "usa", "spain", "italy");

    private final SlackLayoutApplier layoutApplier;
    private final CoronaDataProvider dataProvider;

    public CoronaSlackDataService(
        SlackLayoutApplier layoutApplier,
        CoronaDataProvider dataProvider
    ) {
        this.layoutApplier = layoutApplier;
        this.dataProvider = dataProvider;
    }

    public Single<List<LayoutBlock>> getActualStatsBlocks(List<String> countries) {
        return dataProvider.get2DaysData()
            .flatMap(daysData -> layoutApplier.getBlocksFromEntity(
                SlackLayoutEntity.builder()
                    .layoutBuilderType(CoronaSlackLayoutBuilder.TYPE)
                    .layoutData(Map.of(
                        "todayStat", daysData.get("todayStat"),
                        "yesterdayStat", daysData.get("yesterdayStat"),
                        "countries", getCountries(countries)
                    ))
                    .build()
            ));
    }

    public Single<Map<String, Object>> getActualStatsResponse(List<String> countries, Object channelId) {
        return getActualStatsBlocks(countries)
            .map(blocks -> Map.of(
                "blocks", blocks,
                "response_type", "in_channel",
                "channel", channelId
            ));
    }

    public Single<List<LayoutBlock>> getHistoricalStatsBlocks(List<String> countries) {
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

    public Single<Map<String, Object>> getHistoricalStatsResponse(List<String> countries, Object channelId) {
        return getHistoricalStatsBlocks(countries)
            .map(blocks -> Map.of(
                "blocks", blocks,
                "response_type", "in_channel",
                "channel", channelId
            ));
    }

    private List<List<String>> getCountries(List<String> countries) {
        return countries == null || countries.isEmpty()
            ? List.of(MAIN_COUNTRIES, TOP_COUNTRIES)
            : List.of(countries);
    }
}
