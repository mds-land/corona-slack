package com.github.mdsina.corona.corona;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
@Singleton
public class CoronaDataProvider {

    private final CoronaStatsClient coronaStatsClient;

    public Mono<Map<String, Map<String, Map>>> get2DaysData() {
        return Mono.zip(
            coronaStatsClient.getAllCountriesStat()
                .subscribeOn(Schedulers.elastic()),
            coronaStatsClient.getWorldStat()
                .subscribeOn(Schedulers.elastic()),
            coronaStatsClient.getAllCountriesStat(true)
                .subscribeOn(Schedulers.elastic()),
            coronaStatsClient.getWorldStat(true)
                .subscribeOn(Schedulers.elastic())
        ).map(t -> Map.of(
            "todayStat", processStat(t.getT1(), t.getT2()),
            "yesterdayStat",  processStat(t.getT3(), t.getT4())
        ));
    }

    public Mono<Map<String, Map<String, Map<String, Map<String, Integer>>>>> getHistoricalAllData(
        List<String> countries
    ) {
        return Mono.zip(
            coronaStatsClient.getHistoricalStatForCountries(countries)
                .subscribeOn(Schedulers.elastic()),
            coronaStatsClient.getHistoricalWorldStat()
                .subscribeOn(Schedulers.elastic()),
            coronaStatsClient.getAllCountriesStat()
                .subscribeOn(Schedulers.elastic())
        )
            .map(t -> Map.of("t1", t.getT1(), "t2", t.getT2(), "t3", t.getT3()))
            .flatMap(o -> Mono.fromCallable(() -> processHistoricalStat(
                (List<Map<String, ?>>) o.get("t1"),
                (Map<String, Map<String, Integer>>) o.get("t2"),
                (List<Map>) o.get("t3")
            )).subscribeOn(Schedulers.elastic()));
    }

    private Map<String, Map<String, Map<String, Map<String, Integer>>>> processHistoricalStat(
        List<Map<String, ?>> historicalStat,
        Map<String, Map<String, Integer>> worldHistoricalStat,
        List<Map> countriesStat
    ) {
        Map<String, Map<String, Map<String, Integer>>> namedMap = historicalStat.stream()
            .filter(v -> v.get("country") != null) // for unknown countries
            .collect(Collectors.toMap(
                v -> ((String) v.get("country")).toLowerCase(),
                v -> (Map<String, Map<String, Integer>>) v.get("timeline")
            ));

        var iso2Map = new HashMap<String, Map<String, Map<String, Integer>>>(countriesStat.size());
        var iso3Map = new HashMap<String, Map<String, Map<String, Integer>>>(countriesStat.size());

        for (var cStat : countriesStat) {
            String countryName = ((String) cStat.get("country")).toLowerCase();
            var stat = namedMap.get(countryName);

            Map info = (Map) cStat.get("countryInfo");
            Object iso2 = info.get("iso2");
            Object iso3 = info.get("iso3");

            if (iso2 != null) {
                iso2Map.put(((String) iso2).toLowerCase(), stat);
            }
            if (iso3 != null) {
                iso3Map.put(((String) iso3).toLowerCase(), stat);
            }
        }

        namedMap.put("world", worldHistoricalStat);
        iso2Map.put("world", worldHistoricalStat);
        iso3Map.put("world", worldHistoricalStat);

        return Map.of(
            "named", namedMap,
            "iso2", iso2Map,
            "iso3", iso3Map
        );
    }

    private Map<String, Map> processStat(List<Map> stats, Map worldStat) {
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
