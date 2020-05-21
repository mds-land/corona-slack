package com.github.mdsina.corona.slack.persistence;


import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import reactor.core.publisher.Mono;

@Singleton
@RequiredArgsConstructor
public class SlackTokensRepository {

    private final DSLContext ctx;

    public Mono<Void> addOrUpdateToken(String team, String token) {
        return Mono.fromCompletionStage(
            ctx.insertInto(table("TOKENS"), field("TEAM_ID"), field("TOKEN"))
                .values(team, token)
                .onConflict(field("TEAM_ID"))
                .doUpdate()
                .set(field("TOKEN"), token)
                .executeAsync()
        ).then();
    }

    public Mono<String> getTeamToken(String team) {
        return Mono.fromCompletionStage(
            ctx.select(field("TOKEN"))
                .from(table("TOKENS"))
                .where(field("TEAM_ID").eq(team))
                .fetchAsync()
        ).flatMap(r ->
            r.isEmpty()
                ? Mono.empty()
                : Mono.just((String) r.getValue(0, 0))
        ).switchIfEmpty(Mono.error(new RuntimeException("Token not found")));
    }
}
