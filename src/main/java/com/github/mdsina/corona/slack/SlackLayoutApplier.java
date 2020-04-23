package com.github.mdsina.corona.slack;

import static java.util.Objects.requireNonNull;

import com.slack.api.model.block.LayoutBlock;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Singleton;

@Singleton
public class SlackLayoutApplier {

    private final Map<Object, SlackLayoutBuilder> layoutBuilders;

    public SlackLayoutApplier(List<SlackLayoutBuilder> builders) {
        this.layoutBuilders = builders.stream().collect(Collectors.toMap(SlackLayoutBuilder::getType, o -> o));
    }

    public Single<List<LayoutBlock>> getBlocksFromEntity(SlackLayoutEntity entity) {
        SlackLayoutBuilder layoutBuilder = requireNonNull(layoutBuilders.get(entity.getLayoutBuilderType()));

        return Single.fromCallable(() -> layoutBuilder.buildBlocks(entity.getLayoutData()))
            .subscribeOn(Schedulers.newThread());
    }
}
