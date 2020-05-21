package com.github.mdsina.corona.admin.persistence;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.corona.admin.ConfigParameters;
import com.github.mdsina.corona.admin.TeamConfig;
import com.github.mdsina.corona.admin.TeamConfig.TeamConfigBuilder;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jooq.Condition;
import org.jooq.DSLContext;
import reactor.core.publisher.Mono;

// TODO: maybe some jpa?
@RequiredArgsConstructor
@Singleton
public class TeamConfigRepository {

    private final DSLContext ctx;
    private final ObjectMapper objectMapper;

    public Mono<List<TeamConfig>> getConfig(Condition... conditions) {
        return Mono.fromCompletionStage(
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
        ).map(r ->
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
        );
    }

    public Mono<TeamConfig> getConfig(String team) {
        return getConfig(field("t.TEAM_ID").eq(team)).flatMap(r -> {
            if (r.isEmpty()) {
                return Mono.empty();
            }

            return Mono.just(r.get(0));
        });
    }

    public Mono<List<TeamConfig>> getEnabledConfigs() {
        return getConfig(field("tc.ENABLED").isTrue());
    }

    @SneakyThrows
    public Mono<Void> addOrUpdateConfig(String team, ConfigParameters request) {
        String config = objectMapper.writeValueAsString(request);
        return Mono.fromCompletionStage(
            ctx.insertInto(table("TEAM_CONFIG"), field("TEAM_ID"), field("CONFIG"), field("ENABLED"))
                .values(team, config, request.isDailyEnabled())
                .onConflict(field("TEAM_ID"))
                .doUpdate()
                .set(field("CONFIG"), config)
                .set(field("ENABLED"), request.isDailyEnabled())
                .executeAsync()
        ).then();
    }
}
