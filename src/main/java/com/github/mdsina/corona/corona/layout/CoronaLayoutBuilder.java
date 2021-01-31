package com.github.mdsina.corona.corona.layout;

import static java.util.Objects.requireNonNull;

import com.github.mdsina.corona.corona.layout.CoronaLayoutTemplater.CoronaTemplateData;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class CoronaLayoutBuilder<T> {

    private static final List<String> FUNNY_TPLS = List.of(
        "%s is no more...",
        "%s was burnt to ashes",
        "%s was destroyed to ruins",
        "%s is desolated by corona",
        "Unknown country %s",
        "Your %s is in another castle",
        "Say hello to my little %s"
    );

    private final CoronaLayoutTemplater<T> templater;

    private CoronaLayoutBuilder(CoronaLayoutTemplater<T> templater) {
        this.templater = templater;
    }

    public static <G> CoronaLayoutBuilder<G> withTemplater(CoronaLayoutTemplater<G> templater) {
        return new CoronaLayoutBuilder<>(templater);
    }

    public List<T> buildLayout(Map<?, ?> data) {
        var todayStat = (Map<String, Map>) requireNonNull(
            data.get("todayStat"),
            "todayStat is required"
        );
        var yesterdayStat = (Map<String, Map>) requireNonNull(
            data.get("yesterdayStat"),
            "yesterdayStat is required"
        );
        var countries = (List<List<String>>) data.get("countries");

        List<T> blocks = new ArrayList<>();
        countries.forEach(c -> blocks.addAll(blocks.size(), getBlocksLayoutForCountries(c, todayStat, yesterdayStat)));
        return blocks;
    }

    public List<T> getBlocksLayoutForCountries(
        List<String> countries,
        Map<String, Map> todayStat,
        Map<String, Map> yesterdayStat
    ) {
        List<T> blocks = createBlocks(countries, todayStat, yesterdayStat);
        T divider = templater.divider();
        if (divider != null) {
            blocks.add(blocks.size(), divider);
        }

        return blocks;
    }

    private List<T> createBlocks(
        List<String> countries,
        Map<String, Map> todayStat,
        Map<String, Map> yesterdayStat
    ) {
        var actualOrderedKeys = new ArrayList<>();
        var blocks = new HashMap<Object, T>();

        for (String country : countries) {
            String lcCountry = country.toLowerCase();

            Optional<Object> countryStats = Optional
                .ofNullable(todayStat.get("named").get(lcCountry))
                .or(() -> Optional.ofNullable(todayStat.get("iso2").get(lcCountry)))
                .or(() -> Optional.ofNullable(todayStat.get("iso3").get(lcCountry)));

            if (countryStats.isEmpty()) {
                if (blocks.containsKey(lcCountry)) {
                    continue;
                }
                String tpl = FUNNY_TPLS.get(new Random().nextInt(FUNNY_TPLS.size()));
                blocks.put(lcCountry, templater.funnySection(tpl, country));
                actualOrderedKeys.add(lcCountry);
                continue;
            }

            Map stats = (Map) countryStats.get();
            Object realCountryName = stats.get("country");

            if (blocks.containsKey(realCountryName)) {
                continue;
            }

            Map yesterdayCountryStat = (Map) yesterdayStat.get("named").get(((String) realCountryName).toLowerCase());

            String flagUrl = (String) ((Map) stats.get("countryInfo")).get("flag");
            var updated = LocalDate.ofInstant(
                Instant.ofEpochMilli(Long.parseLong(stats.get("updated").toString())),
                ZoneOffset.UTC
            );
            boolean isNotUpdated = updated.getDayOfMonth() != LocalDate.now().getDayOfMonth();

            String todayCases;
            String todayDeaths;
            if (isNotUpdated) {
                todayCases = "-";
                todayDeaths = "-";
            } else {
                todayCases = "+" + stats.get("todayCases");
                todayDeaths = "+" + stats.get("todayDeaths");
            }

            var templateData = CoronaTemplateData.builder()
                .flagUrl(flagUrl)
                .country(stats.get("country").toString())
                .todayCases(todayCases)
                .yesterdayCases(yesterdayCountryStat.get("todayCases").toString())
                .todayDeaths(todayDeaths)
                .yesterdayDeaths(yesterdayCountryStat.get("todayDeaths").toString())
                .totalCases(stats.get("cases").toString())
                .totalDeaths(stats.get("deaths").toString())
                .build();

            blocks.put(realCountryName, templater.countrySection(templateData));
            actualOrderedKeys.add(realCountryName);
        }

        var result = new ArrayList<T>();
        actualOrderedKeys.forEach(k -> result.add(blocks.get(k)));

        return result;
    }
}
