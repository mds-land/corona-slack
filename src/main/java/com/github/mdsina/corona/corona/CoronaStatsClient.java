package com.github.mdsina.corona.corona;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;

@Retryable(delay = "2s")
@Client("https://corona.lmao.ninja/v2")
public interface CoronaStatsClient {

    @Get(value = "/countries/{country}", processes = MediaType.APPLICATION_JSON)
    Single<Map> getCountryStat(@PathVariable String country);

    @Get(value = "/countries/", processes = MediaType.APPLICATION_JSON)
    Single<List<Map>> getAllCountriesStat();

    @Get(value = "/all", processes = MediaType.APPLICATION_JSON)
    Single<Map> getWorldStat();
}
