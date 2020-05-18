package com.github.mdsina.corona.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.mdsina.corona.scheduling.jackson.CronExpressionSerializer;
import io.micronaut.scheduling.cron.CronExpression;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfigParameters {

    private static final CronExpression DEFAULT_CRON = CronExpression.create("0 0 5,12 1/1 * ?");

    private boolean dailyEnabled;

    @JsonSerialize(using = CronExpressionSerializer.class)
    private CronExpression dailyCron = DEFAULT_CRON;
    private List<String> dailySections = new ArrayList<>();
    private String dailyChannel = "general";

    @JsonProperty("daily_enabled")
    private void parseDailyEnabled(String dailyEnabled) {
        this.dailyEnabled = dailyEnabled.equalsIgnoreCase("on")
            || dailyEnabled.equalsIgnoreCase("true");
    }

    @JsonProperty("daily_cron")
    private void parseDailyCron(String dailyCron) {
        if (dailyCron == null || dailyCron.isBlank()) {
            return;
        }
        this.dailyCron = CronExpression.create(dailyCron);
    }

    public String getDailyCronString() {
        String expr = dailyCron.toString();
        int s1 = expr.indexOf("<");
        expr = expr.substring(s1 + 1);
        int s2 = expr.indexOf(">");
        expr = expr.substring(0, s2);

        return expr;
    }
}