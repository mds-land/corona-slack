package com.github.mdsina.corona.slack;

import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import java.util.List;
import javax.inject.Singleton;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Singleton
public class SlackMessageSender {

    private final AsyncMethodsClient slackMethodsClient;
    private final SlackLayoutApplier layoutApplier;

    public SlackMessageSender(AsyncMethodsClient slackMethodsClient, SlackLayoutApplier layoutApplier) {
        this.layoutApplier = layoutApplier;
        this.slackMethodsClient = slackMethodsClient;
    }

    public Mono<ChatPostMessageResponse> sendMessage(String channel, List<LayoutBlock> blocks) {
        return sendMessage(channel, blocks, null);
    }

    public Mono<ChatPostMessageResponse> sendMessage(String channel, List<LayoutBlock> blocks, String token) {
        return Mono
            .fromCompletionStage(slackMethodsClient.chatPostMessage(
                ChatPostMessageRequest.builder()
                    .channel(channel)
                    .token(token)
                    .blocks(blocks)
                    .build()
            ))
            .subscribeOn(Schedulers.elastic())
            .flatMap(res -> {
                if (res.getError() != null) {
                    return Mono.error(new RuntimeException(res.toString()));
                }
                return Mono.just(res);
            });
    }

    public Mono<ChatPostMessageResponse> sendMessage(String channel, SlackLayoutEntity layoutEntity) {
        return layoutApplier.getBlocksFromEntity(layoutEntity)
            .flatMap(b -> sendMessage(channel, b));
    }
}
