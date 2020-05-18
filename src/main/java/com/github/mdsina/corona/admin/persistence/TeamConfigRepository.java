package com.github.mdsina.corona.admin.persistence;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.corona.admin.ConfigParameters;
import com.github.mdsina.corona.admin.TeamConfig;
import com.github.mdsina.corona.admin.TeamConfig.TeamConfigBuilder;
import io.micronaut.transaction.annotation.ReadOnly;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jooq.Condition;
import org.jooq.DSLContext;

// TODO: maybe some jpa?
@RequiredArgsConstructor
@Singleton
public class TeamConfigRepository {

    private final DSLContext ctx;
    private final ObjectMapper objectMapper;

    @ReadOnly
    @Transactional(TxType.REQUIRES_NEW)
    public Single<List<TeamConfig>> getConfig(Condition... conditions) {
        return Single.fromFuture(
            ctx
                .select(
                    field("t.TEAM_ID", String.class),
                    field("tc.CONFIG", String.class),
                    field("tc.ENABLED", Boolean.class),
                    field("t.TOKEN", String.class)
                )
                .from(table("TOKENS t"))
                .leftJoin(table("TEAM_CONFIG tc")).on(field("t.TEAM_ID").eq(field("tc.TEAM_ID")))
                .where(conditions)
                .fetchAsync()
                .toCompletableFuture()
        ).flatMap(r -> Single.just(
            r.stream()
                .map(rec -> {
                    try {
                        TeamConfigBuilder configBuilder = TeamConfig.builder()
                            .team(rec.component1());
                        if (rec.component2() != null) {
                            configBuilder.config(objectMapper.readValue(rec.component2(), ConfigParameters.class));
                        }
                        if (rec.component3() != null) {
                            configBuilder.enabled(rec.component3());
                        }
                        return configBuilder
                            .token(rec.component4())
                            .build();
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList())
        ));
    }

    @ReadOnly
    @Transactional
    public Maybe<TeamConfig> getConfig(String team) {
        return getConfig(field("t.TEAM_ID").eq(team)).flatMapMaybe(r -> {
            if (r.isEmpty()) {
                return Maybe.empty();
            }

            return Maybe.just(r.get(0));
        });
    }

    @ReadOnly
    @Transactional
    public Single<List<TeamConfig>> getEnabledConfigs() {
        return getConfig(field("tc.ENABLED").isTrue());
    }

    @SneakyThrows
    @Transactional
    public Completable addOrUpdateConfig(String team, ConfigParameters request) {
        String config = objectMapper.writeValueAsString(request);
        return Single.fromFuture(
            ctx.insertInto(table("TEAM_CONFIG"), field("TEAM_ID"), field("CONFIG"), field("ENABLED"))
                .values(team, config, request.isDailyEnabled())
                .onConflict(field("TEAM_ID"))
                .doUpdate()
                .set(field("CONFIG"), config)
                .set(field("ENABLED"), request.isDailyEnabled())
                .executeAsync()
                .toCompletableFuture()
        ).ignoreElement();
    }
}
