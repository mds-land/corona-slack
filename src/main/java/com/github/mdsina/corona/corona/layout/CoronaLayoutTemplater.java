package com.github.mdsina.corona.corona.layout;

import lombok.Builder;
import lombok.Getter;

public interface CoronaLayoutTemplater<T> {

    T divider();

    T funnySection(String tpl, String country);

    T countrySection(CoronaTemplateData data);

    @Getter
    @Builder
    class CoronaTemplateData {
        private final String flagUrl;
        private final String country;
        private final String countryCode;
        private final String todayCases;
        private final String yesterdayCases;
        private final String todayDeaths;
        private final String yesterdayDeaths;
        private final String totalCases;
        private final String totalDeaths;
    }
}
