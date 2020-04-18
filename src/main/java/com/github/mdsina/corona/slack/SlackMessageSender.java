package com.github.mdsina.corona.slack;

import static java.util.Objects.requireNonNull;

import com.slack.api.methods.AsyncMethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class SlackMessageSender {

    private final AsyncMethodsClient slackMethodsClient;
    private final Map<Object, SlackLayoutBuilder> layoutBuilders;

    public SlackMessageSender(AsyncMethodsClient slackMethodsClient, List<SlackLayoutBuilder> builders) {
        this.layoutBuilders = builders.stream().collect(Collectors.toMap(SlackLayoutBuilder::getType, o -> o));
        this.slackMethodsClient = slackMethodsClient;
    }

    public ChatPostMessageResponse sendMessage(String channel, List<LayoutBlock> blocks) {
        try {
            ChatPostMessageResponse res = slackMethodsClient.chatPostMessage(
                ChatPostMessageRequest.builder()
                    .channel(channel)
                    .blocks(blocks)
                    .build()
            ).get();

            if (res.getError() != null) {
                throw new RuntimeException(res.toString());
            }

            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ChatPostMessageResponse sendMessage(String channel, SlackLayoutEntity layoutEntity) {
        SlackLayoutBuilder layoutBuilder = requireNonNull(layoutBuilders.get(layoutEntity.getLayoutBuilderType()));
        return sendMessage(channel, layoutBuilder.buildBlocks(layoutEntity.getLayoutData()));
    }
}
