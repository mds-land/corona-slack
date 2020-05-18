package com.github.mdsina.corona.slack.persistence;


import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import io.micronaut.transaction.annotation.ReadOnly;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

@Singleton
@RequiredArgsConstructor
public class SlackTokensRepository {

    private final DSLContext ctx;

    @Transactional
    public Completable addOrUpdateToken(String team, String token) {
        return Single.fromFuture(
            ctx.insertInto(table("TOKENS"), field("TEAM_ID"), field("TOKEN"))
                .values(team, token)
                .onConflict(field("TEAM_ID"))
                .doUpdate()
                .set(field("TOKEN"), token)
                .executeAsync()
                .toCompletableFuture()
        ).ignoreElement();
    }

    @ReadOnly
    @Transactional(TxType.REQUIRES_NEW)
    public Maybe<String> getTeamToken(String team) {
        return Maybe.fromFuture(
            ctx.select(field("TOKEN"))
                .from(table("TOKENS"))
                .where(field("TEAM_ID").eq(team))
                .fetchAsync()
                .toCompletableFuture()
        ).flatMap(r ->
            r.isEmpty()
                ? Maybe.empty()
                : Maybe.just((String) r.getValue(0, 0))
        ).switchIfEmpty(Maybe.error(() -> new RuntimeException("Token not found")));
    }
}
