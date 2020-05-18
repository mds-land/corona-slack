package com.github.mdsina.corona.slack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.security.oauth2.endpoint.authorization.state.DefaultState;
import io.micronaut.security.oauth2.endpoint.authorization.state.JacksonStateSerDes;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import java.io.IOException;
import java.util.Base64;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Replaces(JacksonStateSerDes.class)
@Slf4j
public class SlackJacksonStateSerDes extends JacksonStateSerDes {

    private final ObjectMapper objectMapper;

    public SlackJacksonStateSerDes(ObjectMapper objectMapper) {
        super(objectMapper);
        this.objectMapper = objectMapper;
    }

    @Override
    public State deserialize(String base64State) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64State);
            String state = new String(decodedBytes);
            return objectMapper.readValue(state, DefaultState.class);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to deserialize the authorization request state", e);
            }
        }
        return null;
    }
}
