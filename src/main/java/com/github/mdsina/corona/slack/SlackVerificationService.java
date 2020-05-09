package com.github.mdsina.corona.slack;

import com.github.mdsina.corona.util.HexUtil;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class SlackVerificationService {

    private final SlackProperties properties;

    // Verifying for requests like in https://api.slack.com/authentication/verifying-requests-from-slack
    public void verifyRequest(String rawBody, String timestamp, String signature) {
        String basestring = String.join(":", "v0", timestamp, rawBody);

        Instant timeInstant = Instant.ofEpochSecond(Long.parseLong(timestamp));
        if (timeInstant.plus(5, ChronoUnit.MINUTES).compareTo(Instant.now()) < 0) {
            throw new RuntimeException("Timestamp too old.");
        }

        String computedDigest;

        try {
            Key signingKey = new SecretKeySpec(
                properties.getSigningSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            computedDigest = HexUtil.byteArrayToHexString(
                mac.doFinal(basestring.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        computedDigest = "v0=" + computedDigest;

        if (!signature.equals(computedDigest)) {
            throw new RuntimeException("Incorrect request digest or verification");
        }
    }
}
