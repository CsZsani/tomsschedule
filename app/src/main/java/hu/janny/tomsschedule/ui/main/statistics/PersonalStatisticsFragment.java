package hu.janny.tomsschedule.ui.main.statistics;

import static java.time.temporal.ChronoUnit.DAYS;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentPersonalStatisticsBinding;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.viewmodel.StatisticsViewModel;

public class PersonalStatisticsFragment extends Fragment {

    private FragmentPersonalStatisticsBinding binding;
    private StatisticsViewModel viewModel;
    public static final String PERIOD_TYPE = "period_type";
    public static final String ACTIVITY_NUM = "activity_num";
    public static final String REQUEST_KEY = "p_filter_data";
    public static final String ACTIVITIES = "activities_array";

    private int periodType = 0;
    private int activityNum = 0;
    private long fromMillis = 0L;
    private long toMillis = 0L;
    private List<Long> activities = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    private List<String> names = new ArrayList<>();

    public static PersonalStatisticsFragment newInstance() {
        return new PersonalStatisticsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_personal_statistics, container, false);
        binding = FragmentPersonalStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        binding.personalFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(root).navigate(R.id.action_nav_statistics_to_personalFilterFragment);
            }
        });

        /*getParentFragmentManager().setFragmentResultListener(REQUEST_KEY, this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                // We use a String here, but any type that can be put in a Bundle is supported
                periodType = bundle.getInt(PERIOD_TYPE);
                activityNum = bundle.getInt(ACTIVITY_NUM);
                List<String> activitiesString = bundle.getStringArrayList(ACTIVITIES);
                List<Long> acts = new ArrayList<>();
                for (int i = 0; i<activities.size(); i++) {
                    acts.add(Long.getLong(activitiesString.get(i)));
                }
                activities = acts;
                System.out.println(periodType + " " + activityNum + " " + activities + " get bundle");
                // Do something with the result
            }
        });*/

        viewModel.getTimesList().observe(getViewLifecycleOwner(), new Observer<List<ActivityTime>>() {
            @Override
            public void onChanged(List<ActivityTime> activityTimes) {
                binding.pleaseFilter.setVisibility(View.GONE);
                chartsGoToGone();
                periodType = viewModel.getpPeriodType();
                activityNum = viewModel.getpActivityNum();
                activities = viewModel.getActsList();
                colors = viewModel.getColors();
                names = viewModel.getNames();
                fromMillis = viewModel.getFromTime();
                toMillis = viewModel.getToTime();
                System.out.println(activityNum + " " + periodType);
                if(activityNum == 0) {
                    if(activityTimes.isEmpty()) {
                        Toast.makeText(getActivity(), "No data available in this period!", Toast.LENGTH_LONG).show();
                        binding.pleaseFilter.setVisibility(View.VISIBLE);
                    } else {
                        if(fromMillis == 0L) {
                            setUpAllBarChart(activityTimes);
                            binding.allBarChart.setVisibility(View.VISIBLE);
                            setUpAllPieChartForSingleDays(activityTimes);
                        } else {
                            setUpAllBarChartLonger(activityTimes, fromMillis, toMillis);
                            binding.allStackedBarChart.setVisibility(View.VISIBLE);
                            setUpAllPieChartForLonger(activityTimes);
                        }
                        binding.allPieChart.setVisibility(View.VISIBLE);
                    }
                } else if(activityNum == 1) {
                    if(activityTimes.isEmpty()) {
                        Toast.makeText(getActivity(), "No data available in this period!", Toast.LENGTH_LONG).show();
                        binding.pleaseFilter.setVisibility(View.VISIBLE);
                    } else {
                        if(fromMillis == 0L) {
                            binding.timeSpentText.setVisibility(View.VISIBLE);
                            binding.timeSpent.setText(DateConverter.durationConverterFromLongToStringForADay(activityTimes.get(0).getT()));
                            binding.timeSpent.setVisibility(View.VISIBLE);
                        } else {
                            setUpOneBarChart(activityTimes, fromMillis, toMillis);
                            binding.oneBarChart.setVisibility(View.VISIBLE);
                        }
                    }
                } else if(activityNum >= 2) {
                    if(activityTimes.isEmpty()) {
                        Toast.makeText(getActivity(), "No data available in this period!", Toast.LENGTH_LONG).show();
                        binding.pleaseFilter.setVisibility(View.VISIBLE);
                    } else {
                        if(fromMillis == 0L) {
                            setUpMoreBarChart(activityTimes);
                            binding.moreBarChart.setVisibility(View.VISIBLE);
                            setUpMorePieChartForSingleDays(activityTimes);
                        } else {
                            setUpMoreStackedBarChartLonger(activityTimes, fromMillis, toMillis);
                            binding.moreStackedChart.setVisibility(View.VISIBLE);
                            setUpMoreGroupBarChartLonger(activityTimes, fromMillis, toMillis);
                            binding.moreGroupChart.setVisibility(View.VISIBLE);
                            setUpMorePieChartForLonger(activityTimes);
                        }
                        binding.morePieChart.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        return root;
    }

    private void chartsGoToGone() {
        binding.allStackedBarChart.setVisibility(View.GONE);
        binding.allPieChart.setVisibility(View.GONE);
        binding.allBarChart.setVisibility(View.GONE);
        binding.oneBarChart.setVisibility(View.GONE);
        binding.moreBarChart.setVisibility(View.GONE);
        binding.moreGroupChart.setVisibility(View.GONE);
        binding.morePieChart.setVisibility(View.GONE);
        binding.moreStackedChart.setVisibility(View.GONE);
        binding.timeSpentText.setVisibility(View.GONE);
        binding.timeSpent.setVisibility(View.GONE);
    }

    private void setUpAllBarChart(List<ActivityTime> activityTimes) {
        BarChart chart = binding.allBarChart;

        int MAX_X_VALUE = activityTimes.size();
        String SET_LABEL = "Time spent each this activity on " + DateConverter.longMillisToStringForSimpleDateDialog(activityTimes.get(0).getD());
        String[] NAMES = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);

        ArrayList<BarEntry> values = new ArrayList<>();
        for (int i = 0; i<activityTimes.size(); i++) {
            values.add(new BarEntry(i, DateConverter.durationConverterFromLongToBarChart(activityTimes.get(i).getT())));
            NAMES[i] = names.get(activities.indexOf(activityTimes.get(i).getaId()));
        }

        Legend legend = chart.getLegend();
        LegendEntry[] legendEntries = new LegendEntry[activityTimes.size()];
        for(int i = 0; i< legendEntries.length; i++) {
            LegendEntry legendEntry = new LegendEntry();
            legendEntry.formColor = colors.get(i);
            legendEntry.label = names.get(i);
            legendEntries[i] = legendEntry;
        }
        legend.setCustom(legendEntries);

        XAxis xAxis = chart.getXAxis();
        /*xAxis.setValueFormatter(new IndexAxisValueFormatter(NAMES));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);*/
        xAxis.setEnabled(false);

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(0.25f);
        axisLeft.setAxisMinimum(0);
//        axisLeft.setAxisMaximum(12.0f);
        axisLeft.setDrawTopYLabelEntry(true);

        YAxis axisRight = chart.getAxisRight();
        axisRight.setGranularity(0.25f);
        axisRight.setAxisMinimum(0);
//        axisRight.setAxisMinimum(12.0f);
        axisRight.setDrawTopYLabelEntry(true);

        BarDataSet set1 = new BarDataSet(values, SET_LABEL);
        set1.setColors(colors);
        /*String[] labels = new String[names.size()];
        for(int k = 0; k<names.size(); k++) {
            labels[k] = names.get(k);
        }
        set1.setStackLabels(labels);*/

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);

        data.setValueTextSize(12f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return super.getBarLabel(barEntry);
            }
        });
        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.invalidate();
    }

    private class HourValueFormatter implements IValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return null;
        }
    }

    private void setUpAllPieChartForSingleDays(List<ActivityTime> activityTimes) {
        PieChart chart = binding.allPieChart;

        chart.getDescription().setEnabled(false);

        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i<activityTimes.size(); i++) {
            values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(activityTimes.get(i).getT()), names.get(activities.indexOf(activityTimes.get(i).getaId()))));
        }

        PieDataSet set1 = new PieDataSet(values, "Time spent in minutes");
        set1.setColors(colors);

        PieData pieData = new PieData(set1);
        pieData.setValueTextSize(12f);
        chart.setData(pieData);
        chart.invalidate();
    }

    private void setUpMorePieChartForSingleDays(List<ActivityTime> activityTimes) {
        PieChart chart = binding.morePieChart;

        chart.getDescription().setEnabled(false);

        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i<activityTimes.size(); i++) {
            values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(activityTimes.get(i).getT()), names.get(activities.indexOf(activityTimes.get(i).getaId()))));
        }

        PieDataSet set1 = new PieDataSet(values, "Time spent in minutes");
        set1.setColors(colors);

        PieData pieData = new PieData(set1);
        pieData.setValueTextSize(12f);
        chart.setData(pieData);
        chart.invalidate();
    }

    private void setUpAllBarChartLonger(List<ActivityTime> activityTimes, long from, long to) {
        BarChart chart = binding.allStackedBarChart;

        /*LocalDate localDate = LocalDate.now();
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        long millis = instant.toEpochMilli();*/

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter);

        int MAX_X_VALUE = (int) daysBetween;
        String[] NAMES = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Legend legend = chart.getLegend();

        ArrayList<BarEntry> values = new ArrayList<>();
        int i = 0;

        LocalDate localDate = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        long millis = to;
        while(millis != from) {
            float[] list = new float[activities.size()];
            for(int j = 0; j < activities.size(); j++) {
                list[j] = containsName(activityTimes, activities.get(j), millis);
            }
            values.add(new BarEntry(i, list));
            NAMES[i] = String.format(Locale.getDefault(), "%02d.%02d.", localDate.getMonthValue(), localDate.getDayOfMonth());
            i++;
            localDate = localDate.minusDays(1);
            millis = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return NAMES[(int) value];
            }
        });

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(0.5f);
        axisLeft.setAxisMinimum(0);

        YAxis axisRight = chart.getAxisRight();
        axisRight.setGranularity(0.5f);
        axisRight.setAxisMinimum(0);

        BarDataSet set1 = new BarDataSet(values, "Time spent in hours");
        set1.setColors(colors);
        String[] labels = new String[names.size()];
        for(int k = 0; k<names.size(); k++) {
            labels[k] = names.get(k);
        }
        set1.setStackLabels(labels);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        //BarData data = new BarData(set1);
        BarData data = new BarData(dataSets);

        data.setValueTextSize(12f);
        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.invalidate();
    }

    private void setUpAllPieChartForLonger(List<ActivityTime> activityTimes) {
        PieChart chart = binding.allPieChart;

        chart.getDescription().setEnabled(false);

        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i<activities.size(); i++) {
            final int j = i;
            long sum = activityTimes.stream().filter(a -> a.getaId() == activities.get(j)).mapToLong(ActivityTime::getT).sum();
            values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(sum), names.get(i)));
        }

        PieDataSet set1 = new PieDataSet(values, "All time spent in minutes");
        set1.setColors(colors);

        PieData pieData = new PieData(set1);
        pieData.setValueTextSize(12f);
        chart.setData(pieData);
        chart.invalidate();
    }

    private void setUpOneBarChart(List<ActivityTime> activityTimes, long from, long to) {
        BarChart chart = binding.oneBarChart;

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter);

        int MAX_X_VALUE = (int) daysBetween;
        String[] DAYS = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Legend legend = chart.getLegend();

        ArrayList<BarEntry> values = new ArrayList<>();
        int i = 0;

        LocalDate localDate = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        long millis = to;
        while(millis != from) {
            values.add(new BarEntry(i, containsName(activityTimes, millis)));
            DAYS[i] = String.format(Locale.getDefault(), "%02d.%02d.", localDate.getMonthValue(), localDate.getDayOfMonth());
            i++;
            localDate = localDate.minusDays(1);
            millis = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return DAYS[(int) value];
            }
        });

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(0.5f);
        axisLeft.setAxisMinimum(0);

        YAxis axisRight = chart.getAxisRight();
        axisRight.setGranularity(0.5f);
        axisRight.setAxisMinimum(0);

        BarDataSet set1 = new BarDataSet(values, "Time spent in hours");
        set1.setColor(colors.get(0));

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        //BarData data = new BarData(set1);
        BarData data = new BarData(dataSets);

        data.setValueTextSize(12f);
        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.invalidate();
    }

    private void setUpMoreBarChart(List<ActivityTime> activityTimes) {
        BarChart chart = binding.moreBarChart;

        int MAX_X_VALUE = activities.size();
        String[] NAMES = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        ArrayList<BarEntry> values = new ArrayList<>();
        for (int i = 0; i<activities.size(); i++) {
            values.add(new BarEntry(i, containsId(activityTimes, activities.get(i))));
            NAMES[i] = names.get(i);
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return NAMES[(int) value];
            }
        });

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(0.5f);
        axisLeft.setAxisMinimum(0);

        YAxis axisRight = chart.getAxisRight();
        axisRight.setGranularity(0.5f);
        axisRight.setAxisMinimum(0);

        BarDataSet set1 = new BarDataSet(values, "Time spent in hours");
        set1.setColors(colors);
        String[] labels = new String[names.size()];
        for(int k = 0; k<names.size(); k++) {
            labels[k] = names.get(k);
        }
        set1.setStackLabels(labels);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        //BarData data = new BarData(set1);
        BarData data = new BarData(dataSets);

        data.setValueTextSize(12f);
        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.invalidate();
    }

    private void setUpMoreStackedBarChartLonger(List<ActivityTime> activityTimes, long from, long to) {
        BarChart chart = binding.moreStackedChart;

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter);

        int MAX_X_VALUE = (int) daysBetween;
        String[] NAMES = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Legend legend = chart.getLegend();

        ArrayList<BarEntry> values = new ArrayList<>();
        int i = 0;

        LocalDate localDate = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        long millis = to;
        while(millis != from) {
            float[] list = new float[activities.size()];
            for(int j = 0; j < activities.size(); j++) {
                list[j] = containsName(activityTimes, activities.get(j), millis);
            }
            values.add(new BarEntry(i, list));
            NAMES[i] = String.format(Locale.getDefault(), "%02d.%02d.", localDate.getMonthValue(), localDate.getDayOfMonth());
            i++;
            localDate = localDate.minusDays(1);
            millis = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return NAMES[(int) value];
            }
        });

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(0.5f);
        axisLeft.setAxisMinimum(0);

        YAxis axisRight = chart.getAxisRight();
        axisRight.setGranularity(0.5f);
        axisRight.setAxisMinimum(0);

        BarDataSet set1 = new BarDataSet(values, "Time spent in hours");
        set1.setColors(colors);
        String[] labels = new String[names.size()];
        for(int k = 0; k<names.size(); k++) {
            labels[k] = names.get(k);
        }
        set1.setStackLabels(labels);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        //BarData data = new BarData(set1);
        BarData data = new BarData(dataSets);

        data.setValueTextSize(12f);
        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.invalidate();
    }

    private void setUpMoreGroupBarChartLonger(List<ActivityTime> activityTimes, long from, long to) {
        BarChart chart = binding.moreGroupChart;

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter);

        int MAX_X_VALUE = (int) daysBetween;
        String[] NAMES = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Legend legend = chart.getLegend();

        ArrayList<ArrayList<BarEntry>> values = new ArrayList<>();
        for(int k = 0; k<activities.size(); k++) {
            values.add(new ArrayList<>());
        }
        int i = 0;

        LocalDate localDate = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        long millis = to;
        while(millis != from) {
            for(int j = 0; j < activities.size(); j++) {
                values.get(j).add(new BarEntry(i, containsName(activityTimes, activities.get(j), millis)));
            }
            NAMES[i] = String.format(Locale.getDefault(), "%02d.%02d.", localDate.getMonthValue(), localDate.getDayOfMonth());
            i++;
            localDate = localDate.minusDays(1);
            millis = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(NAMES));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(0.5f);
        axisLeft.setAxisMinimum(0);

        YAxis axisRight = chart.getAxisRight();
        axisRight.setGranularity(0.5f);
        axisRight.setAxisMinimum(0);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();

        int l = 0;
        for(ArrayList<BarEntry> be : values) {
            BarDataSet set1 = new BarDataSet(be, names.get(l));
            set1.setColors(colors.get(l));
            l++;
            dataSets.add(set1);
        }

        BarData data = new BarData(dataSets);

        float barSpace = 0.2f;
        float groupSpace = 0.8f;

        data.setValueTextSize(12f);
        data.setBarWidth(0.15f);
        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);

        chart.groupBars(0, groupSpace, barSpace);
        chart.invalidate();
    }

    private void setUpMorePieChartForLonger(List<ActivityTime> activityTimes) {
        PieChart chart = binding.morePieChart;

        chart.getDescription().setEnabled(false);

        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i<activities.size(); i++) {
            final int j = i;
            long sum = activityTimes.stream().filter(a -> a.getaId() == activities.get(j)).mapToLong(ActivityTime::getT).sum();
            values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(sum), names.get(i)));
        }

        PieDataSet set1 = new PieDataSet(values, "- all time spent in minutes");
        set1.setColors(colors);

        PieData pieData = new PieData(set1);
        pieData.setValueTextSize(12f);

        chart.setData(pieData);
        chart.invalidate();
    }

    private float containsId(final List<ActivityTime> list, final long id){
        ActivityTime activityTime = list.stream()
                .filter(at -> at.getaId() == id)
                .findAny()
                .orElse(null);
        if(activityTime != null) {
            return DateConverter.durationConverterFromLongToBarChart(activityTime.getT());
        } else {
            return 0f;
        }
    }

    private float containsName(final List<ActivityTime> list, final long date){
        ActivityTime activityTime = list.stream()
                .filter(at -> at.getD() == date)
                .findAny()
                .orElse(null);
        if(activityTime != null) {
            return DateConverter.durationConverterFromLongToBarChart(activityTime.getT());
        } else {
            return 0f;
        }
    }

    private float containsName(final List<ActivityTime> list, final long id, final long date){
        ActivityTime activityTime = list.stream()
                .filter(at -> at.getaId() == id && at.getD() == date)
                .findAny()
                .orElse(null);
        if(activityTime != null) {
            return DateConverter.durationConverterFromLongToBarChart(activityTime.getT());
        } else {
            return 0f;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }

}