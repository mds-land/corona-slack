package com.github.mdsina.corona.corona.discord;

import com.github.mdsina.corona.corona.layout.CoronaLayoutTemplater;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class CoronaDiscordLayoutTemplater implements CoronaLayoutTemplater<Map<String, String>> {

    @Override
    public Map<String, String> divider() {
        return null;
    }

    @Override
    public Map<String, String> funnySection(String tpl, String country) {
        return Map.of(
            "name", country,
            "value", String.format(tpl, country)
        );
    }

    @Override
    public Map<String, String> countrySection(CoronaTemplateData data) {
        String flagEmoji = data.getCountryCode().equalsIgnoreCase("world")
            ? ":globe_with_meridians:"
            : ":flag_" + data.getCountryCode() + ":";

        return Map.of(
            "name", flagEmoji + " " + data.getCountry(),
            "value", String.format(
                ":pill: **%s** / %s  :skull_and_crossbones: **%s** / %s  :yin_yang:  %s / %s",
                data.getTodayCases(),
                data.getYesterdayCases(),
                data.getTodayDeaths(),
                data.getYesterdayDeaths(),
                data.getTotalCases(),
                data.getTotalDeaths()
            )
        );
    }
}
