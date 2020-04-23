package com.github.mdsina.corona.corona.chart;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;

@Singleton
public class ChartCreator {

    public String createChartFile(List<Date> xValues, Map<String, List<Integer>> yValues) throws IOException {
        XYChart chart = new XYChartBuilder()
            .width(1024)
            .height(768)
            .title("COVID-19 Data from the JHU CSSE Data Repository")
            .xAxisTitle("Date")
            .yAxisTitle("Cases")
            .build();

        chart.getStyler().setLegendPosition(LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setYAxisDecimalPattern("#,###.##");
        chart.getStyler().setPlotMargin(0);
        chart.getStyler().setPlotContentSize(.95);

        yValues.forEach((k, v) -> chart.addSeries(k, xValues, v));

        String fileName = "" + System.nanoTime();
        BitmapEncoder.saveBitmap(chart, "/app/data/" + fileName, BitmapFormat.PNG);
        return fileName + ".png";
    }
}
