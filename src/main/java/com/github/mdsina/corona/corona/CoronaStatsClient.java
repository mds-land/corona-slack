package com.github.mdsina.corona.corona;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@Retryable(delay = "2s")
@Client("https://corona.lmao.ninja/v2")
public interface CoronaStatsClient {

    @Get(value = "/countries/{country}", processes = MediaType.APPLICATION_JSON)
    Single<Map> getCountryStat(@PathVariable String country);

    @Get(value = "/countries/{?yesterday}", processes = MediaType.APPLICATION_JSON)
    Single<List<Map>> getAllCountriesStat(@Nullable Boolean yesterday);

    default Single<List<Map>> getAllCountriesStat() {
        return getAllCountriesStat(false);
    }

    @Get(value = "/all{?yesterday}", processes = MediaType.APPLICATION_JSON)
    Single<Map> getWorldStat(@Nullable Boolean yesterday);

    default Single<Map> getWorldStat() {
        return getWorldStat(false);
    }
}
