package com.github.mdsina.corona.slack;

import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.inject.Singleton;

@Singleton
public class SlackMessageSender {

    private final AsyncMethodsClient slackMethodsClient;
    private final SlackLayoutApplier layoutApplier;

    public SlackMessageSender(AsyncMethodsClient slackMethodsClient, SlackLayoutApplier layoutApplier) {
        this.layoutApplier = layoutApplier;
        this.slackMethodsClient = slackMethodsClient;
    }

    public Single<ChatPostMessageResponse> sendMessage(String channel, List<LayoutBlock> blocks) {
        return Single
            .fromFuture(slackMethodsClient.chatPostMessage(
                ChatPostMessageRequest.builder()
                    .channel(channel)
                    .blocks(blocks)
                    .build()
            ))
            .subscribeOn(Schedulers.newThread())
            .flatMap(res -> {
                if (res.getError() != null) {
                    return Single.error(new RuntimeException(res.toString()));
                }
                return Single.just(res);
            });
    }

    public Single<ChatPostMessageResponse> sendMessage(String channel, SlackLayoutEntity layoutEntity) {
        return layoutApplier.getBlocksFromEntity(layoutEntity)
            .flatMap(b -> sendMessage(channel, b));
    }
}
