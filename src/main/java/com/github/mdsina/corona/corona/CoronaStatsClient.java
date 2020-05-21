package com.github.mdsina.corona.corona;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import reactor.core.publisher.Mono;

@Retryable(delay = "2s")
@Client("https://corona.lmao.ninja/v2")
public interface CoronaStatsClient {

    @Get(value = "/countries/{country}", processes = MediaType.APPLICATION_JSON)
    Mono<Map> getCountryStat(@PathVariable String country);

    @Get(value = "/countries/{?yesterday}", processes = MediaType.APPLICATION_JSON)
    Mono<List<Map>> getAllCountriesStat(@Nullable Boolean yesterday);

    default Mono<List<Map>> getAllCountriesStat() {
        return getAllCountriesStat(false);
    }

    @Get(value = "/all{?yesterday}", processes = MediaType.APPLICATION_JSON)
    Mono<Map> getWorldStat(@Nullable Boolean yesterday);

    default Mono<Map> getWorldStat() {
        return getWorldStat(false);
    }

    @Get(value = "/historical/{?lastdays}", processes = MediaType.APPLICATION_JSON)
    Mono<List<Map<String, ?>>> getHistoricalStat(@QueryValue @Nullable Integer lastdays);

    @Get(value = "/historical/{countries}{?lastdays}", processes = MediaType.APPLICATION_JSON)
    Mono<List<Map<String, ?>>> getHistoricalStatForCountries(
        @PathVariable String countries,
        @QueryValue @Nullable Integer lastdays
    );

    @Get(value = "/historical/{country}{?lastdays}", processes = MediaType.APPLICATION_JSON)
    Mono<Map<String, ?>> getHistoricalStatForCountry(
        @PathVariable String country,
        @QueryValue @Nullable Integer lastdays
    );

    default Mono<Map<String, ?>> getHistoricalStatForCountry(String country) {
        return getHistoricalStatForCountry(country, 30);
    }

    default Mono<List<Map<String, ?>>> getHistoricalStatForCountries(List<String> countries) {
        Objects.requireNonNull(countries);
        if (countries.size() == 1) {
            return getHistoricalStatForCountry(countries.get(0))
                .map(List::of);
        }
        return getHistoricalStatForCountries(String.join(",", countries), 30);
    };

    default Mono<List<Map<String, ?>>> getHistoricalStat() {
        return getHistoricalStat(30);
    }

    @Get(value = "/historical/all{?lastdays}", processes = MediaType.APPLICATION_JSON)
    Mono<Map<String, Map<String, Integer>>> getHistoricalWorldStat(@QueryValue @Nullable Integer lastdays);

    default Mono<Map<String, Map<String, Integer>>> getHistoricalWorldStat() {
        return getHistoricalWorldStat(30);
    }
}
