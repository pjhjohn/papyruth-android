package com.montserrat.utils.support.mpandroidchart;

import android.graphics.Color;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.DefaultValueFormatter;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.montserrat.app.R;
import com.montserrat.app.model.unique.Course;
import com.montserrat.app.model.unique.Evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjhjohn on 2015-06-24.
 */
public class ChartUtil {
    public static class ColorPalette {
        public static final int[] VORDIPLOM_COLORS = {
            Color.rgb(192, 255, 140), Color.rgb(255, 247, 140), Color.rgb(255, 208, 140),
            Color.rgb(140, 234, 255), Color.rgb(255, 140, 157)
        };
    }

    public static void bindData(HorizontalBarChart chart, Evaluation evaluation){
        List<BarEntry> yVals1 = new ArrayList<>();
        yVals1.add(new BarEntry(evaluation.getPointOverall(), 0));
        yVals1.add(new BarEntry(evaluation.getPointGpaSatisfaction(), 1));
        yVals1.add(new BarEntry(evaluation.getPointClarity(), 2));
        yVals1.add(new BarEntry(evaluation.getPointEasiness(), 3));
        ChartUtil.bindData(chart, yVals1);
    }
    public static void bindData(HorizontalBarChart chart, Course course){
        List<BarEntry> yVals1 = new ArrayList<>();
        yVals1.add(new BarEntry(course.getPointOverall(), 0));
        yVals1.add(new BarEntry(course.getPointGpaSatisfaction(), 1));
        yVals1.add(new BarEntry(course.getPointClarity(), 2));
        yVals1.add(new BarEntry(course.getPointEasiness(), 3));
        ChartUtil.bindData(chart, yVals1);
    }
    public static void bindData(HorizontalBarChart chart, List<BarEntry> yVals1) {
        List<String> xVals = new ArrayList<>();
        xVals.add(chart.getResources().getString(R.string.label_point_overall));
        xVals.add(chart.getResources().getString(R.string.label_point_gpa_satisfaction));
        xVals.add(chart.getResources().getString(R.string.label_point_clarity));
        xVals.add(chart.getResources().getString(R.string.label_point_easiness));
        BarDataSet set1 = new BarDataSet(yVals1, "Data Set");
        set1.setColors(ColorPalette.VORDIPLOM_COLORS);
        set1.setValueFormatter(value -> String.format("%.1f", value));
        set1.setValueTextSize(12.0f);
        set1.setBarSpacePercent(70f);
        set1.setDrawValues(true);
        List<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        BarData data = new BarData(xVals, dataSets);
        chart.setData(data);
        chart.invalidate();
    }
    public static void init(HorizontalBarChart chart) {
        chart.setDescription("");
        chart.setMaxVisibleValueCount(10);
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
    }
}
