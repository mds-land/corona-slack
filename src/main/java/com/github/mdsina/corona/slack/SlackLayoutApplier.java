package com.github.mdsina.corona.slack;

import static java.util.Objects.requireNonNull;

import com.slack.api.model.block.LayoutBlock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Singleton
public class SlackLayoutApplier {

    private final Map<Object, SlackLayoutBuilder> layoutBuilders;

    public SlackLayoutApplier(List<SlackLayoutBuilder> builders) {
        this.layoutBuilders = builders.stream().collect(Collectors.toMap(SlackLayoutBuilder::getType, o -> o));
    }

    public Mono<List<LayoutBlock>> getBlocksFromEntity(SlackLayoutEntity entity) {
        SlackLayoutBuilder layoutBuilder = requireNonNull(layoutBuilders.get(entity.getLayoutBuilderType()));

        return Mono.fromCallable(() -> layoutBuilder.buildBlocks(entity.getLayoutData()))
            .subscribeOn(Schedulers.elastic());
    }
}
