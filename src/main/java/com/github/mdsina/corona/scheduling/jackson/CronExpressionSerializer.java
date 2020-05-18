package com.github.mdsina.corona.scheduling.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.micronaut.scheduling.cron.CronExpression;
import java.io.IOException;

public class CronExpressionSerializer extends JsonSerializer<CronExpression> {

    @Override
    public void serialize(
        CronExpression cronExpression,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider
    ) throws IOException {
        String expr = cronExpression.toString();
        int s1 = expr.indexOf("<");
        expr = expr.substring(s1 + 1);
        int s2 = expr.indexOf(">");
        expr = expr.substring(0, s2);

        jsonGenerator.writeString(expr);
    }
}
