package com.github.mdsina.corona;

import com.slack.api.Slack;
import com.slack.api.methods.AsyncMethodsClient;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import javax.inject.Singleton;

@Factory
public class SlackApiFactory {

    @Singleton
    AsyncMethodsClient slackMethods(@Value("${slack.token:}") String token) {
        return Slack.getInstance().methodsAsync(token);
    }
}
