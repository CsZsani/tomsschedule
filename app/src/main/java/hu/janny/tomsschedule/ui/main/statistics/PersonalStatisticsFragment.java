package hu.janny.tomsschedule.ui.main.statistics;

import static java.time.temporal.ChronoUnit.DAYS;

import androidx.annotation.ColorInt;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.StackedValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentPersonalStatisticsBinding;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.viewmodel.StatisticsViewModel;

/**
 * This fragment displays the data we have filtered in personal filter fragment.
 */
public class PersonalStatisticsFragment extends Fragment {

    private FragmentPersonalStatisticsBinding binding;
    private StatisticsViewModel viewModel;

    private int periodType = 0;
    private int activityNum = 0;
    private long fromMillis = 0L;
    private long toMillis = 0L;
    private List<Long> activities = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    private List<String> names = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Binds layout
        binding = FragmentPersonalStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets a StatisticsViewModel instance
        viewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        // Clicking on filter button results in navigating to personal filter fragment
        binding.personalFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_nav_statistics_to_personalFilterFragment);
            }
        });

        // Observer of list of times that we have searched for in personal filter fragment
        viewModel.getTimesList().observe(getViewLifecycleOwner(), new Observer<List<ActivityTime>>() {
            @Override
            public void onChanged(List<ActivityTime> activityTimes) {
                binding.pleaseFilter.setVisibility(View.GONE);
                chartsGoToGone();
                // Gets the parameters from filtering to know what charts should be displayed
                periodType = viewModel.getpPeriodType();
                activityNum = viewModel.getpActivityNum();
                activities = viewModel.getActsList();
                colors = viewModel.getColors();
                names = viewModel.getNames();
                fromMillis = viewModel.getFromTime();
                toMillis = viewModel.getToTime();
                //System.out.println(activityNum + " " + periodType);
                List<ActivityTime> times = activityTimes.stream().filter(a -> a.getT() != 0L).collect(Collectors.toList());
                //System.out.println(activityTimes);
                //System.out.println(times);
                // When the times list is empty, we display that there is no data in the given period
                if (times.isEmpty()) {
                    Toast.makeText(getActivity(), getString(R.string.no_data_available_in_period), Toast.LENGTH_LONG).show();
                    binding.pleaseFilter.setVisibility(View.VISIBLE);
                } else {
                    showCharts(times);
                }
            }
        });
    }

    /**
     * Shows the charts based on the filtering.
     *
     * @param activityTimes the list of times
     */
    private void showCharts(List<ActivityTime> activityTimes) {
        if (activityNum == 0) {
            // All activity is chosen
            showAllActivity(activityTimes);
        } else if (activityNum == 1) {
            // Just one activity is chosen
            showJustOneActivity(activityTimes);
        } else if (activityNum >= 2) {
            // More activities is chosen
            showMoreActivities(activityTimes);
        }
    }

    /**
     * Shows the charts if all the activity was selected.
     *
     * @param activityTimes list of times
     */
    private void showAllActivity(List<ActivityTime> activityTimes) {
        if (fromMillis == 0L) {
            // Just for one day - bar chart and pie chart
            setUpAllBarChart(activityTimes);
            binding.allBarChart.setVisibility(View.VISIBLE);
            setUpAllPieChartForSingleDays(activityTimes);
        } else {
            // For an interval - stacked bar chart and pie chart
            setUpAllBarChartLonger(activityTimes, fromMillis, toMillis);
            binding.allStackedBarChart.setVisibility(View.VISIBLE);
            setUpAllPieChartForLonger(activityTimes);
        }
        binding.allPieChart.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the charts if just one activity was selected.
     *
     * @param activityTimes list of times
     */
    private void showJustOneActivity(List<ActivityTime> activityTimes) {
        if (fromMillis == 0L) {
            // Just for one day - the time spent on the activity this day
            binding.timeSpentText.setVisibility(View.VISIBLE);
            binding.timeSpent.setText(DateConverter.durationConverterFromLongToStringForADay(activityTimes.get(0).getT()));
            binding.timeSpent.setVisibility(View.VISIBLE);
        } else {
            // For an interval - bar chart
            setUpOneBarChart(activityTimes, fromMillis, toMillis);
            binding.oneBarChart.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows the charts if more but not all activity was selected.
     *
     * @param activityTimes list of times
     */
    private void showMoreActivities(List<ActivityTime> activityTimes) {
        if (fromMillis == 0L) {
            // Just for one day - bar chart and pie chart
            setUpMoreBarChart(activityTimes);
            binding.moreBarChart.setVisibility(View.VISIBLE);
            setUpMorePieChartForSingleDays(activityTimes);
        } else {
            // For an interval - stacked bar chart, grouped bar chart and pie chart
            setUpMoreStackedBarChartLonger(activityTimes, fromMillis, toMillis);
            binding.moreStackedChart.setVisibility(View.VISIBLE);
            if(activityNum <= 10) {
                setUpMoreGroupBarChartLonger(activityTimes, fromMillis, toMillis);
                binding.moreGroupChart.setVisibility(View.VISIBLE);
            }
            setUpMorePieChartForLonger(activityTimes);
            setUpMoreAveragePieChartForLonger(activityTimes, fromMillis, toMillis);
            binding.moreAveragePieChart.setVisibility(View.VISIBLE);
        }
        binding.morePieChart.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the visibility of charts to gone.
     */
    private void chartsGoToGone() {
        binding.allStackedBarChart.setVisibility(View.GONE);
        binding.allPieChart.setVisibility(View.GONE);
        binding.allBarChart.setVisibility(View.GONE);
        binding.oneBarChart.setVisibility(View.GONE);
        binding.moreBarChart.setVisibility(View.GONE);
        binding.moreGroupChart.setVisibility(View.GONE);
        binding.morePieChart.setVisibility(View.GONE);
        binding.moreStackedChart.setVisibility(View.GONE);
        binding.moreAveragePieChart.setVisibility(View.GONE);
        binding.timeSpentText.setVisibility(View.GONE);
        binding.timeSpent.setVisibility(View.GONE);
    }

    /**
     * Sets up the bar chart when we searched for all activity but for just one day.
     *
     * @param activityTimes list of times which usually includes max 1 time for an activity
     */
    private void setUpAllBarChart(List<ActivityTime> activityTimes) {
        BarChart chart = binding.allBarChart;

        List<String> n = new ArrayList<>();
        List<Integer> col = new ArrayList<>();

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        ArrayList<BarEntry> values = new ArrayList<>();

        for (int i = 0; i < activityTimes.size(); i++) {
            if(activityTimes.get(i).getT() != 0L) {
                values.add(new BarEntry(i, DateConverter.durationConverterFromLongToBarChart(activityTimes.get(i).getT())));
                n.add(names.get(activities.indexOf(activityTimes.get(i).getaId())));
                col.add(colors.get(activities.indexOf(activityTimes.get(i).getaId())));
            }
        }

        String[] NAMES = new String[n.size()];
        int[] EXACT_COLORS = new int[col.size()];

        for (int i = 0; i< n.size(); i++) {
            NAMES[i] = n.get(i);
            EXACT_COLORS[i] = col.get(i);
        }

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(NAMES));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(290f);
        xAxis.setTextSize(12f);
        xAxis.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(1.0f);
        axisLeft.setAxisMinimum(0);
        axisLeft.setDrawTopYLabelEntry(true);
        axisLeft.setValueFormatter(new HourValueFormatter());
        axisLeft.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisRight = chart.getAxisRight();
        axisRight.setEnabled(false);

        BarDataSet set1 = new BarDataSet(values, "");
        set1.setColors(EXACT_COLORS);
        set1.setValueFormatter(new HourValueFormatter());

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);

        data.setValueTextSize(12f);
        data.setValueFormatter(new HourValueFormatter());

        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.setVisibleXRangeMaximum(7f);

        chart.setDrawBorders(true);
        chart.setBorderColor(Color.parseColor("#973200"));
        chart.setBorderWidth(1);

        chart.setNoDataText(getString(R.string.detail_bar_chart_no_data));
        chart.setFitBars(true);

        chart.invalidate();
    }

    /**
     * Formats the value of bar chart axis and data label from float to hour and minutes.
     * E.g. 1.5f -> 1h 30m
     */
    private static class HourValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return DateConverter.chartTimeConverter(value);
        }

        @Override
        public String getBarLabel(BarEntry barEntry) {
            String label = DateConverter.chartTimeConverter(barEntry.getY());
            if(label.equals("0h 0m")) {
                return "0h";
            } else {
                return label;
            }
        }
    }

    /**
     * Sets up the pie chart when we searched for all activity but for just one day.
     *
     * @param activityTimes list of times which usually includes max 1 time for an activity
     */
    private void setUpAllPieChartForSingleDays(List<ActivityTime> activityTimes) {
        PieChart chart = binding.allPieChart;

        int MAX_X_VALUE = activityTimes.size();
        int[] EXACT_COLORS = new int[MAX_X_VALUE];

        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(getString(R.string.pie_description_one_day));

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i < activityTimes.size(); i++) {
            values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(activityTimes.get(i).getT()), names.get(activities.indexOf(activityTimes.get(i).getaId()))));
            EXACT_COLORS[i] = colors.get(activities.indexOf(activityTimes.get(i).getaId()));
        }

        PieDataSet set1 = new PieDataSet(values, "");
        set1.setColors(EXACT_COLORS);
        set1.setValueTextColor(Color.BLACK);
        set1.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        PieData pieData = new PieData(set1);

        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);
        pieData.setValueFormatter(new PieValueFormatter());

        chart.setData(pieData);
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(14f);
        chart.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        if (periodType == 0) {
            chart.setCenterText(getString(R.string.pie_center_today) + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 1) {
            chart.setCenterText(getString(R.string.pie_center_yesterday) + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 6) {
            chart.setCenterText(DateConverter.longMillisToStringForSimpleDateDialog(toMillis) + "\n" + getString(R.string.pie_sum));
        } else {
            chart.setCenterText("");
        }
        chart.setCenterTextSize(18f);

        chart.invalidate();
    }

    /**
     * Formats the value of pie chart data value from float to hour and minutes.
     * E.g. 1.5f -> 1h 30m
     */
    private static class PieValueFormatter extends ValueFormatter {
        @Override
        public String getPieLabel(float value, PieEntry pieEntry) {
            return DateConverter.chartTimeConverterFromInt(value);
        }
    }

    /**
     * Sets up the stacked bar chart when we searched for all activity but for an interval.
     *
     * @param activityTimes list of times which usually includes more time for an activity
     */
    private void setUpAllBarChartLonger(List<ActivityTime> activityTimes, long from, long to) {
        BarChart chart = binding.allStackedBarChart;

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter) + 1;

        int MAX_X_VALUE = (int) daysBetween;
        String[] NAMES = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        ArrayList<BarEntry> values = new ArrayList<>();
        int i = 0;

        LocalDate localDate = Instant.ofEpochMilli(to).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        long millis = to;
        while (millis >= from) {
            float[] list = new float[activities.size()];
            for (int j = 0; j < activities.size(); j++) {
                list[j] = containsIdAndDate(activityTimes, activities.get(j), millis);
            }
            values.add(new BarEntry(i, list));
            NAMES[i] = String.format(Locale.getDefault(), "%02d.%02d.", localDate.getMonthValue(), localDate.getDayOfMonth());
            i++;
            localDate = localDate.minusDays(1);
            millis = localDate.atStartOfDay(ZoneId.of("Europe/Budapest")).toInstant().toEpochMilli();
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(NAMES));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(1.0f);
        axisLeft.setAxisMinimum(0);
        axisLeft.setDrawTopYLabelEntry(true);
        axisLeft.setValueFormatter(new HourValueFormatter());
        axisLeft.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisRight = chart.getAxisRight();
        axisRight.setEnabled(false);

        BarDataSet set1 = new BarDataSet(values, "");
        set1.setColors(colors);
        set1.setDrawValues(false);
        //set1.setValueFormatter(new HourValueFormatter());
        /*String[] labels = new String[names.size()];
        for (int k = 0; k < names.size(); k++) {
            labels[k] = names.get(k);
        }
        set1.setStackLabels(labels);*/

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        //BarData data = new BarData(set1);
        BarData data = new BarData(dataSets);

        data.setValueTextSize(12f);
        data.setValueFormatter(new StackedValueFormatter(false, "h", 1));

        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.setVisibleXRangeMaximum(7f);

        chart.setDrawBorders(true);
        chart.setBorderColor(Color.parseColor("#973200"));
        chart.setBorderWidth(1);

        chart.setNoDataText(getString(R.string.detail_bar_chart_no_data));
        chart.setFitBars(true);

        chart.invalidate();
    }

    /**
     * Sets up the pie chart when we searched for all activity but for an interval.
     *
     * @param activityTimes list of times which usually includes more time for an activity
     */
    private void setUpAllPieChartForLonger(List<ActivityTime> activityTimes) {
        PieChart chart = binding.allPieChart;

        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(getString(R.string.pie_description_longer));

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        ArrayList<Integer> col = new ArrayList<>();
        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i < activities.size(); i++) {
            final int j = i;
            long sum = activityTimes.stream().filter(a -> a.getaId() == activities.get(j)).mapToLong(ActivityTime::getT).sum();
            if(sum != 0L) {
                values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(sum), names.get(i)));
                col.add(colors.get(i));
            }
        }

        PieDataSet set1 = new PieDataSet(values, "");
        set1.setColors(col);
        set1.setValueTextColor(Color.BLACK);
        set1.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        PieData pieData = new PieData(set1);

        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);
        pieData.setValueFormatter(new PieValueFormatter());

        chart.setData(pieData);
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(14f);
        chart.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        if (periodType == 2) {
            chart.setCenterText(getString(R.string.pie_center_week) + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 3) {
            chart.setCenterText(getString(R.string.pie_center_two_weeks) + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 4) {
            chart.setCenterText(getString(R.string.pie_center_month) + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 5) {
            chart.setCenterText(getString(R.string.pie_center_three_month) + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 7) {
            chart.setCenterText(
                    DateConverter.longMillisToStringForSimpleDateDialog(fromMillis) + "\n" +
                            DateConverter.longMillisToStringForSimpleDateDialog(toMillis)
                            + "\n" + getString(R.string.pie_sum)
            );
        } else {
            chart.setCenterText("");
        }
        chart.setCenterTextSize(16f);

        chart.invalidate();
    }

    /**
     * Sets up the bar chart when we searched for just one activity but for an interval.
     *
     * @param activityTimes list of times which usually includes one time for a day
     */
    private void setUpOneBarChart(List<ActivityTime> activityTimes, long from, long to) {
        BarChart chart = binding.oneBarChart;

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter) + 1;

        int MAX_X_VALUE = (int) daysBetween;
        String[] DAYS = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        ArrayList<BarEntry> values = new ArrayList<>();
        int i = 0;

        LocalDate localDate = Instant.ofEpochMilli(to).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        long millis = to;
        while (millis >= from) {
            values.add(new BarEntry(i, containsDate(activityTimes, millis)));
            DAYS[i] = String.format(Locale.getDefault(), "%02d.%02d.", localDate.getMonthValue(), localDate.getDayOfMonth());
            i++;
            localDate = localDate.minusDays(1);
            millis = localDate.atStartOfDay(ZoneId.of("Europe/Budapest")).toInstant().toEpochMilli();
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(DAYS));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(1.0f);
        axisLeft.setAxisMinimum(0);
        axisLeft.setDrawTopYLabelEntry(true);
        axisLeft.setValueFormatter(new HourValueFormatter());
        axisLeft.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisRight = chart.getAxisRight();
        axisRight.setEnabled(false);

        BarDataSet set1 = new BarDataSet(values, "");
        set1.setColor(colors.get(0));
        set1.setValueFormatter(new HourValueFormatter());

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);

        data.setValueFormatter(new HourValueFormatter());
        data.setValueTextSize(12f);

        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.setVisibleXRangeMaximum(7f);

        chart.setDrawBorders(true);
        chart.setBorderColor(darkenColor(darkenColor(Color.parseColor("#973200"))));
        chart.setBorderWidth(1);

        chart.setNoDataText(getString(R.string.detail_bar_chart_no_data));
        chart.setFitBars(true);

        chart.invalidate();
    }

    /**
     * Sets up the bar chart when we searched for more activities but for one day.
     *
     * @param activityTimes list of times which usually includes one time for a day and activity
     */
    private void setUpMoreBarChart(List<ActivityTime> activityTimes) {
        BarChart chart = binding.moreBarChart;

        int MAX_X_VALUE = activities.size();
        String[] NAMES = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        ArrayList<BarEntry> values = new ArrayList<>();
        for (int i = 0; i < activities.size(); i++) {
            values.add(new BarEntry(i, containsId(activityTimes, activities.get(i))));
            NAMES[i] = names.get(i);
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(NAMES));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setLabelRotationAngle(290f);
        xAxis.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(1.0f);
        axisLeft.setAxisMinimum(0);
        axisLeft.setDrawTopYLabelEntry(true);
        axisLeft.setValueFormatter(new HourValueFormatter());
        axisLeft.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisRight = chart.getAxisRight();
        axisRight.setEnabled(false);

        BarDataSet set1 = new BarDataSet(values, "");
        set1.setColors(colors);
        set1.setValueFormatter(new HourValueFormatter());
        /*String[] labels = new String[names.size()];
        for (int k = 0; k < names.size(); k++) {
            labels[k] = names.get(k);
        }
        set1.setStackLabels(labels);*/

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        //BarData data = new BarData(set1);
        BarData data = new BarData(dataSets);

        data.setValueFormatter(new HourValueFormatter());
        data.setValueTextSize(12f);

        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.setVisibleXRangeMaximum(7f);

        chart.setDrawBorders(true);
        chart.setBorderColor(darkenColor(darkenColor(Color.parseColor("#973200"))));
        chart.setBorderWidth(1);

        chart.setNoDataText(getString(R.string.detail_bar_chart_no_data));
        chart.setFitBars(true);

        chart.invalidate();
    }

    /**
     * Sets up the pie chart when we searched for more activities but for one day.
     *
     * @param activityTimes list of times which usually includes one time for a day and activity
     */
    private void setUpMorePieChartForSingleDays(List<ActivityTime> activityTimes) {
        PieChart chart = binding.morePieChart;

        //int MAX_X_VALUE = activityTimes.size();


        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(getString(R.string.pie_description_one_day));

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        ArrayList<Integer> col = new ArrayList<>();
        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i < activities.size(); i++) {
            final int j = i;
            long sum = activityTimes.stream().filter(a -> a.getaId() == activities.get(j)).mapToLong(ActivityTime::getT).sum();
            if(sum != 0L) {
                values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(sum), names.get(i)));
                col.add(colors.get(i));
            }
        }
        /*ArrayList<Integer> col = new ArrayList<>();
        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i < activityTimes.size(); i++) {
            if(activityTimes.get(i).getT() != 0L) {
                values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(activityTimes.get(i).getT()), names.get(activities.indexOf(activityTimes.get(i).getaId()))));
                col.add(colors.get(activities.indexOf(activityTimes.get(i).getaId())));
            }
        }*/

        int[] EXACT_COLORS = new int[col.size()];
        for (int i = 0; i<col.size(); i++) {
            EXACT_COLORS[i] = col.get(i);
        }

        PieDataSet set1 = new PieDataSet(values, "");
        set1.setColors(EXACT_COLORS);
        set1.setValueTextColor(Color.BLACK);
        set1.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        PieData pieData = new PieData(set1);

        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);
        pieData.setValueFormatter(new PieValueFormatter());

        chart.setData(pieData);
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(14f);
        chart.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        if (periodType == 0) {
            chart.setCenterText(getString(R.string.pie_center_today) + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 1) {
            chart.setCenterText(getString(R.string.pie_center_yesterday) + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 6) {
            chart.setCenterText(DateConverter.longMillisToStringForSimpleDateDialog(toMillis)
                    + "\n" + getString(R.string.pie_sum));
        } else {
            chart.setCenterText("");
        }
        chart.setCenterTextSize(18f);

        chart.invalidate();
    }

    private void setUpMoreStackedBarChartLonger(List<ActivityTime> activityTimes, long from, long to) {
        BarChart chart = binding.moreStackedChart;

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter) + 1;

        int MAX_X_VALUE = (int) daysBetween;
        String[] NAMES = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        ArrayList<BarEntry> values = new ArrayList<>();
        int i = 0;

        LocalDate localDate = Instant.ofEpochMilli(to).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        long millis = to;
        while (millis >= from) {
            float[] list = new float[activities.size()];
            for (int j = 0; j < activities.size(); j++) {
                list[j] = containsIdAndDate(activityTimes, activities.get(j), millis);
            }
            values.add(new BarEntry(i, list));
            NAMES[i] = String.format(Locale.getDefault(), "%02d.%02d.", localDate.getMonthValue(), localDate.getDayOfMonth());
            i++;
            localDate = localDate.minusDays(1);
            millis = localDate.atStartOfDay(ZoneId.of("Europe/Budapest")).toInstant().toEpochMilli();
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(NAMES));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(1.0f);
        axisLeft.setAxisMinimum(0);
        axisLeft.setDrawTopYLabelEntry(true);
        axisLeft.setValueFormatter(new HourValueFormatter());
        axisLeft.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisRight = chart.getAxisRight();
        axisRight.setEnabled(false);

        BarDataSet set1 = new BarDataSet(values, "Time spent in hours");
        set1.setColors(colors);
        set1.setDrawValues(false);
        /*String[] labels = new String[names.size()];
        for (int k = 0; k < names.size(); k++) {
            labels[k] = names.get(k);
        }
        set1.setStackLabels(labels);*/

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);

        data.setValueTextSize(12f);
        data.setValueFormatter(new StackedValueFormatter(false, "h", 1));

        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.setVisibleXRangeMaximum(7f);

        chart.setDrawBorders(true);
        chart.setBorderColor(Color.parseColor("#973200"));
        chart.setBorderWidth(1);

        chart.setNoDataText(getString(R.string.detail_bar_chart_no_data));
        chart.setFitBars(true);

        chart.invalidate();
    }

    private float[] calculateGroupBarSpace(int noOfBars) {
        // (barwidth + barspace) * noofbars + groupspace = 1
        float[] data = new float[3];
        float barWidth;
        float barSpace;
        float groupSpace;
        if(noOfBars <= 5) {
            barWidth = 0.15f;
            groupSpace = 0.14f;
        } else if(noOfBars <= 10) {
            barWidth = 0.09f;
            groupSpace = 0.09f;
        } else {
            barWidth = 0.05f;
            groupSpace = 0.07f;
        }
        barSpace = (1f - groupSpace) / noOfBars - barWidth;
        data[0] = barWidth;
        data[1] = barSpace;
        data[2] = groupSpace;
        return data;
    }

    private void setUpMoreGroupBarChartLonger(List<ActivityTime> activityTimes, long from, long to) {
        BarChart chart = binding.moreGroupChart;

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter) + 1;

        int MAX_X_VALUE = (int) daysBetween;
        String[] DAYS = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        ArrayList<ArrayList<BarEntry>> values = new ArrayList<>();
        for (int k = 0; k < activities.size(); k++) {
            values.add(new ArrayList<>());
        }
        int i = 0;

        LocalDate localDate = Instant.ofEpochMilli(to).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        long millis = to;
        while (millis >= from) {
            for (int j = 0; j < activities.size(); j++) {
                values.get(j).add(new BarEntry(i, containsIdAndDate(activityTimes, activities.get(j), millis)));
            }
            DAYS[i] = String.format(Locale.getDefault(), "%02d.%02d.", localDate.getMonthValue(), localDate.getDayOfMonth());
            i++;
            localDate = localDate.minusDays(1);
            millis = localDate.atStartOfDay(ZoneId.of("Europe/Budapest")).toInstant().toEpochMilli();
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(DAYS));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);
        xAxis.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(1.0f);
        axisLeft.setAxisMinimum(0);
        axisLeft.setDrawTopYLabelEntry(true);
        axisLeft.setValueFormatter(new HourValueFormatter());
        axisLeft.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisRight = chart.getAxisRight();
        axisRight.setEnabled(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();

        int l = 0;
        for (ArrayList<BarEntry> be : values) {
            BarDataSet set1 = new BarDataSet(be, names.get(l));
            set1.setColors(colors.get(l));
            set1.setValueFormatter(new HourValueFormatter());
            l++;
            dataSets.add(set1);
        }

        BarData data = new BarData(dataSets);

        float[] spaces = calculateGroupBarSpace(activities.size());
        /*data.setBarWidth(0.16f);
        float barSpace = 0.05f;
        float groupSpace = 0.16f;*/
        data.setBarWidth(spaces[0]);
        float barSpace = spaces[1];
        float groupSpace = spaces[2];

        data.setValueTextSize(12f);
        data.setValueFormatter(new HourValueFormatter());

        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.setVisibleXRangeMaximum(3f);

        chart.setDrawBorders(true);
        chart.setBorderColor(Color.parseColor("#973200"));
        chart.setBorderWidth(1);

        chart.setNoDataText(getString(R.string.detail_bar_chart_no_data));
        chart.setFitBars(true);

        chart.groupBars(0, groupSpace, barSpace);
        chart.invalidate();
    }

    private void setUpMorePieChartForLonger(List<ActivityTime> activityTimes) {
        PieChart chart = binding.morePieChart;

        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(getString(R.string.pie_description_longer));

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        ArrayList<Integer> col = new ArrayList<>();
        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i < activities.size(); i++) {
            final int j = i;
            long sum = activityTimes.stream().filter(a -> a.getaId() == activities.get(j)).mapToLong(ActivityTime::getT).sum();
            if(sum != 0L) {
                values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(sum), names.get(i)));
                col.add(colors.get(i));
            }
        }

        PieDataSet set1 = new PieDataSet(values, "");
        set1.setColors(col);
        set1.setValueTextColor(Color.BLACK);
        set1.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        PieData pieData = new PieData(set1);

        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);
        pieData.setValueFormatter(new PieValueFormatter());

        chart.setData(pieData);
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(14f);
        chart.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        if (periodType == 2) {
            chart.setCenterText(getString(R.string.pie_center_week)
                    + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 3) {
            chart.setCenterText(getString(R.string.pie_center_two_weeks)
                    + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 4) {
            chart.setCenterText(getString(R.string.pie_center_month)
                    + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 5) {
            chart.setCenterText(getString(R.string.pie_center_three_month)
                    + "\n" + getString(R.string.pie_sum));
        } else if (periodType == 7) {
            chart.setCenterText(
                    DateConverter.longMillisToStringForSimpleDateDialog(fromMillis) + "\n" +
                            DateConverter.longMillisToStringForSimpleDateDialog(toMillis)
                            + "\n" + getString(R.string.pie_sum)
            );
        } else {
            chart.setCenterText("");
        }
        chart.setCenterTextSize(16f);

        chart.invalidate();
    }

    private void setUpMoreAveragePieChartForLonger(List<ActivityTime> activityTimes, long from, long to) {
        PieChart chart = binding.moreAveragePieChart;

        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(getString(R.string.pie_description_average));

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter) + 1;

        ArrayList<Integer> col = new ArrayList<>();
        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i < activities.size(); i++) {
            final int j = i;
            long sum = activityTimes.stream().filter(a -> a.getaId() == activities.get(j)).mapToLong(ActivityTime::getT).sum();
            if(sum != 0L) {
                long average = sum / daysBetween;
                values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(average), names.get(i)));
                col.add(colors.get(i));
            }
        }

        PieDataSet set1 = new PieDataSet(values, getString(R.string.pie_description_average));
        set1.setColors(col);
        set1.setValueTextColor(Color.BLACK);
        set1.setValueTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        PieData pieData = new PieData(set1);

        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);
        pieData.setValueFormatter(new PieValueFormatter());

        chart.setData(pieData);
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(14f);
        chart.setEntryLabelTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        if (periodType == 2) {
            chart.setCenterText(getString(R.string.pie_center_week)
                    + "\n" + getString(R.string.pie_average));
        } else if (periodType == 3) {
            chart.setCenterText(getString(R.string.pie_center_two_weeks)
                    + "\n" + getString(R.string.pie_average));
        } else if (periodType == 4) {
            chart.setCenterText(getString(R.string.pie_center_month)
                    + "\n" + getString(R.string.pie_average));
        } else if (periodType == 5) {
            chart.setCenterText(getString(R.string.pie_center_three_month)
                    + "\n" + getString(R.string.pie_average));
        } else if (periodType == 7) {
            chart.setCenterText(
                    DateConverter.longMillisToStringForSimpleDateDialog(fromMillis) + "\n" +
                            DateConverter.longMillisToStringForSimpleDateDialog(toMillis)
                            + "\n" + getString(R.string.pie_average)
            );
        } else {
            chart.setCenterText("");
        }
        chart.setCenterTextSize(16f);

        chart.invalidate();
    }

    /**
     * Returns the time of activity with the given id if the list includes it, otherwise returns 0.
     *
     * @param list list of times
     * @param id   id of activity
     * @return the time an activity with the given id if the list includes it, otherwise 0
     */
    private float containsId(final List<ActivityTime> list, final long id) {
        ActivityTime activityTime = list.stream()
                .filter(at -> at.getaId() == id)
                .findAny()
                .orElse(null);
        if (activityTime != null) {
            return DateConverter.durationConverterFromLongToBarChart(activityTime.getT());
        } else {
            return 0f;
        }
    }

    /**
     * Returns the time of activity with the given date if the list includes it, otherwise returns 0.
     *
     * @param list list of times
     * @param date date we search for
     * @return the time of activity with the given date if the list includes it, otherwise 0
     */
    private float containsDate(final List<ActivityTime> list, final long date) {
        ActivityTime activityTime = list.stream()
                .filter(at -> at.getD() == date)
                .findAny()
                .orElse(null);
        if (activityTime != null) {
            return DateConverter.durationConverterFromLongToBarChart(activityTime.getT());
        } else {
            return 0f;
        }
    }

    /**
     * Returns the time of activity with the given date and id if the list includes it, otherwise returns 0.
     *
     * @param list list of times
     * @param id   id of activity
     * @param date date we search for
     * @return the time of activity with the given date and id if the list includes it, otherwise 0
     */
    private float containsIdAndDate(final List<ActivityTime> list, final long id, final long date) {
        ActivityTime activityTime = list.stream()
                .filter(at -> at.getaId() == id && at.getD() == date)
                .findAny()
                .orElse(null);
        if (activityTime != null) {
            return DateConverter.durationConverterFromLongToBarChart(activityTime.getT());
        } else {
            return 0f;
        }
    }

    /**
     * Darkens the given colour int.
     *
     * @param color the colour int we want to be darker
     * @return a darker colour int
     */
    @ColorInt
    int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}