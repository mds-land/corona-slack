package com.github.mdsina.corona.corona.chart;

import com.github.mdsina.corona.slack.SlackLayoutBuilder;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import io.micronaut.context.annotation.Value;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Singleton;

@Singleton
public class CoronaChartLayoutBuilder implements SlackLayoutBuilder {

    public static final String TYPE = "corona-chart";

    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("M/d/yyyy", Locale.US);

    private final ChartCreator chartCreator;
    private final String chartUrl;

    public CoronaChartLayoutBuilder(ChartCreator chartCreator, @Value("${corona.chart.url:}") String chartUrl) {
        this.chartCreator = chartCreator;
        this.chartUrl = chartUrl;
    }

    @Override
    public List<LayoutBlock> buildBlocks(Map<?, ?> data) {
        var historicalData = (Map<String, Map<String, Map<String, Map<String, Integer>>>>) data.get("data");
        var countriesList = (List<List<String>>) data.get("countries");

        List<LayoutBlock> blocks = new ArrayList<>();

        countriesList.forEach(countries -> {
            var xData = new ArrayList<Date>();
            var yData = new HashMap<String, List<Integer>>();

            for (String c : countries) {
                String lcCountry = c.toLowerCase();
                Optional<Map<String, Map<String, Integer>>> countryStat = Optional
                    .ofNullable(historicalData.get("named").get(lcCountry))
                    .or(() -> Optional.ofNullable(historicalData.get("iso2").get(lcCountry)))
                    .or(() -> Optional.ofNullable(historicalData.get("iso3").get(lcCountry)));

                if (countryStat.isEmpty()) {
                    continue;
                }

                Map<String, Map<String, Integer>> stat = countryStat.get();
                Map<String, Integer> cases = stat.get("cases");

                boolean requiresX = xData.isEmpty();
                if (requiresX) {
                    Set<String> dates = cases.keySet();
                    try {
                        for (String dateStr : dates) {
                            xData.add(DATE_TIME_FORMATTER.parse(dateStr));
                        }
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                yData.put(lcCountry, new ArrayList<>(cases.values()));
            }

            String fileName;
            try {
                fileName = chartCreator.createChartFile(xData, yData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            blocks.add(Blocks.image(b ->
                b.imageUrl(chartUrl + fileName)
                    .altText("Corona Stats")
                    .imageWidth(360)
                    .imageHeight(246)
            ));
        });

        return blocks;
    }

    @Override
    public Object getType() {
        return TYPE;
    }
}
