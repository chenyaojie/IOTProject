package com.wetter.iotproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;

public class ChartActivity extends AppCompatActivity {

    private LineChartView chartTop;
    private ColumnChartView chartBottom;
    private LineChartData lineData;
    private ColumnChartData columnData;
    private Toolbar mToolbar;

    private int[][] usefulDataSet;
    private int[] averageData;
    private String[] columnTag;

    private int position = 0;
    private final int NAN = 0;
    private int maxData = 0;
    private int minData = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_chats);

        initData();
        initChat();
    }

    private void initData() {
        Intent intent = getIntent();
        position = intent.getIntExtra("pos", 0);
        usefulDataSet = new int[7][96];
        averageData = new int[7];
        columnTag = new String[7];
        mToolbar = (Toolbar) findViewById(R.id.chart_toolbar);

        switch (position) {
            case 0: mToolbar.setTitle("温度趋势图"); break;
            case 1: mToolbar.setTitle("湿度趋势图"); break;
            case 2: mToolbar.setTitle("照度趋势图"); break;
        }

        List<SensorData> mDataList = MainActivity.sDataList;
        // 基准日期，用于判断距离现在的天数，格式为 "yyyy-MM-dd"
        String baseDate = mDataList.get(0).getSensorDate().substring(0, 10);

        // 生成直方图Tag
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date =null;
        try {
            date = formatter.parse(baseDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < columnTag.length; i++) {
            Date limitDate = new Date(date.getTime() - (long)i*24 * 60 * 60 * 1000);
            String tempDate = formatter.format(limitDate);
            columnTag[i] = tempDate.substring(5, 10);
        }

        int day = 0;
        for (SensorData sensor : mDataList) {
            int usefulData = 0;
            switch (position) {
                case 0: usefulData = sensor.getTemperature(); break;
                case 1: usefulData = sensor.getHumidity(); break;
                case 2: usefulData = sensor.getIlluminant(); break;
            }
            // 当前数据的日期，用于计算时间段，格式为 "yyyy-MM-dd"
            String getDate = sensor.getSensorDate().substring(0, 10);

            if (!getDate.equals(baseDate)) {
                baseDate = getDate;
                ++day;
            }

            if (usefulData > maxData) {
                maxData = usefulData;
            }

            if (usefulData < minData) {
                minData = usefulData;
            }

            int hour = Integer.parseInt(sensor.getSensorDate().substring(11,13));
            int minute = Integer.parseInt(sensor.getSensorDate().substring(14,16));

            int index = hour * 4 + minute / 15;

            if(usefulDataSet[day][index]==NAN){
                usefulDataSet[day][index] = usefulData;
            }

        }
        String temp = "";
        int sum = 0,count=0;
        for (int i = 0; i < usefulDataSet.length; i++) {
            for (int j = 0; j < usefulDataSet[0].length; j++) {
                temp+=usefulDataSet[i][j]+" ";
                if (usefulDataSet[i][j] != NAN) {
                    sum += usefulDataSet[i][j];
                    count++;
                }
            }
            temp += "\n";
            if(count!=0){
                averageData[i]=sum/count;
            }
            sum=0;
            count= 0;
            Log.i("Log_Chart", "averageData[i]: "+averageData[i]);
            Log.i("Log_Chart", "columnTag[i]: "+columnTag[i]);
        }
        Log.i("Log_Chart", temp);
        Log.i("Log_Chart", "" + usefulDataSet[0][44]);
    }

    private void initChat() {
        // *** TOP LINE CHART ***
        chartTop = (LineChartView) findViewById(R.id.chart_top);

        // Generate and set data for line chart
        generateInitialLineData();

        // *** BOTTOM COLUMN CHART ***

        chartBottom = (ColumnChartView) findViewById(R.id.chart_bottom);

        generateColumnData();
    }

    private void generateColumnData() {

        int numSubcolumns = 1;
        int numColumns = usefulDataSet.length;

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < numColumns; ++i) {

            values = new ArrayList<SubcolumnValue>();
//            for (int j = 0; j < numSubcolumns; ++j) {
//                values.add(new SubcolumnValue((float) Math.random() * 50f + 5, ChartUtils.pickColor()));
//            }

            for (int j = 0; j < numSubcolumns; ++j) {

                values.add(new SubcolumnValue(averageData[6-i], ChartUtils.pickColor()));
            }

            axisValues.add(new AxisValue(i).setLabel(columnTag[6-i]));

            columns.add(new Column(values).setHasLabelsOnlyForSelected(true));
        }

        columnData = new ColumnChartData(columns);

        columnData.setAxisXBottom(new Axis(axisValues).setHasLines(true).setTextColor(ChartUtils.COLOR_RED));
        columnData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

        chartBottom.setColumnChartData(columnData);

        // Set value touch listener that will trigger changes for chartTop.
        chartBottom.setOnValueTouchListener(new ValueTouchListener());

        // Set selection mode to keep selected month column highlighted.
        chartBottom.setValueSelectionEnabled(true);

        chartBottom.setZoomType(ZoomType.HORIZONTAL);

        // chartBottom.setOnClickListener(new View.OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // SelectedValue sv = chartBottom.getSelectedValue();
        // if (!sv.isSet()) {
        // generateInitialLineData();
        // }
        //
        // }
        // });

    }

    /**
     * Generates initial data for line chart. At the begining all Y values are equals 0. That will change when user
     * will select value on column chart.
     */
    private void generateInitialLineData() {

        // 上方折线图描点初始化
        List<PointValue> values = new ArrayList<PointValue>();
        for (int i = 0; i < 96; ++i) {
            values.add(new PointValue(i, 0));
        }

        Line line = new Line(values);
        line.setColor(ChartUtils.COLOR_GREEN).setCubic(true);
        line.setHasPoints(false);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        // 上方折线图X轴坐标
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int i = 0; i < 96; i++) {
            axisValues.add(new AxisValue(i).setLabel(formatMinutes(i)));
        }
        Axis hourLineXAxis = new Axis(axisValues).setMaxLabelChars(5).setTextColor(ChartUtils.COLOR_RED);

        lineData = new LineChartData(lines);
        lineData.setAxisXBottom(hourLineXAxis);
        lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(3));

        chartTop.setLineChartData(lineData);

        // For build-up animation you have to disable viewport recalculation.
        chartTop.setViewportCalculationEnabled(false);

        // And set initial max viewport and current viewport- remember to set viewports after data.
        Viewport vMax = new Viewport(minData-minData/20, maxData+maxData/20, 96, 0);
        chartTop.setMaximumViewport(vMax);
        Viewport vCur = new Viewport(minData-minData/20, maxData+maxData/20, 48, 0);
        chartTop.setCurrentViewport(vCur);

        chartTop.setZoomType(ZoomType.HORIZONTAL);
    }

    private void generateLineData(int columnIndex,int color, float range) {
        // Cancel last animation if not finished.
        chartTop.cancelDataAnimation();

        // Modify data targets
        Line line = lineData.getLines().get(0);// For this example there is always only one line.
        line.setColor(color);
//        for (PointValue value : line.getValues()) {
//            // Change target only for Y value.
//            value.setTarget(value.getX(), (float) Math.random() * range);
//        }

        for (int i = 0; i < line.getValues().size(); i++) {
            PointValue value = line.getValues().get(i);
            value.setTarget(value.getX(),usefulDataSet[6-columnIndex][i]);
        }

        // Start new data animation with 300ms duration;
        chartTop.startDataAnimation(300);
    }

    private class ValueTouchListener implements ColumnChartOnValueSelectListener {

        @Override
        public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
            generateLineData(columnIndex,value.getColor(), 100);
        }

        @Override
        public void onValueDeselected() {
            //generateLineData(columnIndex,ChartUtils.COLOR_GREEN, 0);
        }
    }

    private String formatMinutes(int num) {
        String format = "";
        int hour = num / 4;
        int minute = (num % 4) * 15;
        if (hour < 10) {
            format += "0" + hour + ":";
        } else {
            format += hour + ":";
        }
        if (minute < 10) {
            format += "00";
        } else {
            format += minute;
        }
        return format;
    }

}
