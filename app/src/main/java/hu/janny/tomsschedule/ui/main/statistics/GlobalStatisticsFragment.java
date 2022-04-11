package hu.janny.tomsschedule.ui.main.statistics;

import static java.time.temporal.ChronoUnit.DAYS;

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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentGlobalStatisticsBinding;
import hu.janny.tomsschedule.model.entities.ActivityTimeFirebase;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.model.helper.InternetConnectionHelper;
import hu.janny.tomsschedule.viewmodel.GlobalStatisticsViewModel;

/**
 * This fragment displays the data we have filtered in global filter fragment.
 */
public class GlobalStatisticsFragment extends Fragment {

    private FragmentGlobalStatisticsBinding binding;
    private GlobalStatisticsViewModel viewModel;

    private final int[] allColors = new int[]{Color.parseColor("#FFB5E6"), Color.parseColor("#FF96DC"), Color.parseColor("#FF74D0"),
            Color.parseColor("#FF54C5"), Color.parseColor("#FF29B7"), Color.parseColor("#FF00A9"),
            Color.parseColor("#A7C5FF"), Color.parseColor("#84AEFF"), Color.parseColor("#6297FF"),
            Color.parseColor("#4A88FF"), Color.parseColor("#256FFF"), Color.parseColor("#0057FF")};
    private String[] labelAll;

    private long from = 0L;
    private long to = 0L;
    private int gender = 0;
    private int ageGroup = -1;
    private String name = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Binds layout
        binding = FragmentGlobalStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets a GlobasStatisticsViewModel instance
        viewModel = new ViewModelProvider(requireActivity()).get(GlobalStatisticsViewModel.class);

        labelAll = new String[]{getString(R.string.fe_0), getString(R.string.fe_1), getString(R.string.fe_2),
                getString(R.string.fe_3), getString(R.string.fe_4), getString(R.string.fe_5),
                getString(R.string.ma_0), getString(R.string.ma_1), getString(R.string.ma_2),
                getString(R.string.ma_3), getString(R.string.ma_4), getString(R.string.ma_5)};

        initFilterButton(view);

        observeDataChanges();

    }

    @Override
    public void onStart() {
        super.onStart();

        if (viewModel.isLoading()) {
            progressVisible();
        } else {
            progressGone();
        }
    }

    /**
     * Sets the progress bar to visible and the linear layout to gone.
     */
    private void progressVisible() {
        binding.gStatisticsScrollView.setVisibility(View.GONE);
        binding.gStatisticsLoadingText.setVisibility(View.VISIBLE);
        binding.gStatisticsProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the progress bar to gone and the linear layout to visible.
     */
    private void progressGone() {
        binding.gStatisticsScrollView.setVisibility(View.VISIBLE);
        binding.gStatisticsLoadingText.setVisibility(View.GONE);
        binding.gStatisticsProgressBar.setVisibility(View.GONE);
    }

    /**
     * Sets the charts and they description to gone.
     */
    private void chartsGone() {
        binding.gDayBarChart.setVisibility(View.GONE);
        binding.gAverageGenderAndAge.setVisibility(View.GONE);
        binding.gLongerBarChart.setVisibility(View.GONE);
        binding.gAverageDaily.setVisibility(View.GONE);
        binding.gLongerPieChart.setVisibility(View.GONE);
    }

    /**
     * Observer of list of times that we have searched for in global filter fragment
     */
    private void observeDataChanges() {
        viewModel.getTimesList().observe(getViewLifecycleOwner(), new Observer<List<ActivityTimeFirebase>>() {
            @Override
            public void onChanged(List<ActivityTimeFirebase> activityTimeFirebases) {
                viewModel.setLoading(false);
                progressGone();
                binding.gPleaseFilter.setVisibility(View.GONE);
                chartsGone();
                // Gets the parameters from filtering to know what charts should be displayed
                from = viewModel.getFrom();
                to = viewModel.getTo();
                ageGroup = viewModel.getAgeGroup();
                gender = viewModel.getGender();
                name = viewModel.getName();
                System.out.println(from + " " + to + " " + gender + " " + ageGroup + " " + name);
                // When the times list is empty, we display that there is no data in the given period
                if (activityTimeFirebases.isEmpty()) {
                    Toast.makeText(getActivity(), getString(R.string.no_data_available_in_period), Toast.LENGTH_LONG).show();
                    binding.gPleaseFilter.setVisibility(View.VISIBLE);
                } else {
                    setUpCharts(activityTimeFirebases);
                }
            }
        });
    }

    /**
     * Shows the charts based on the filtering.
     *
     * @param list the list of times
     */
    private void setUpCharts(List<ActivityTimeFirebase> list) {
        if (from == 0L) {
            setUpDayBarChart(list, to);
            binding.gAverageGenderAndAge.setVisibility(View.VISIBLE);
            binding.gDayBarChart.setVisibility(View.VISIBLE);
        } else {
            setUpLongerBarChart(list, from, to, gender, ageGroup);
            binding.gAverageDaily.setVisibility(View.VISIBLE);
            binding.gLongerBarChart.setVisibility(View.VISIBLE);
            if (gender == 0 || ageGroup == -1) {
                setUpLongerPieChart(list, from, to, gender, ageGroup);
                binding.gLongerPieChart.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * Sets up the bar chart when we searched for the data of just one day.
     *
     * @param list list of times
     * @param date day in millis
     */
    private void setUpDayBarChart(List<ActivityTimeFirebase> list, long date) {
        BarChart chart = binding.gDayBarChart;

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        ArrayList<BarEntry> values = new ArrayList<>();
        int a = 0;
        for (int i = 2; i > 0; i--) {
            for (int j = 0; j < 6; j++) {
                values.add(new BarEntry(a, containsType(list, i, j)));
                a++;
            }
        }

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelAll));
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
        set1.setColors(allColors);
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
            if (label.equals("0h 0m")) {
                return "0h";
            } else {
                return label;
            }
        }
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
     * Sets up the bar chart when we searched for an activity but for an interval.
     *
     * @param list list of times
     * @param from beginning of interval in long millis
     * @param to   end of interval in long millis
     */
    private void setUpLongerBarChart(List<ActivityTimeFirebase> list, long from, long to, int gender, int ageGroup) {
        BarChart chart = binding.gLongerBarChart;

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.of("Europe/Budapest")).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter) + 1;

        int MAX_X_VALUE = (int) daysBetween;
        String[] NAMES = new String[MAX_X_VALUE];

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
            values.add(new BarEntry(i, containsDate(list, millis, gender, ageGroup)));
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

        BarDataSet set1 = new BarDataSet(values, "");
        set1.setColor(Objects.requireNonNull(getActivity()).getResources().getColor(R.color.toms_400, null));
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
     * Sets up the pie chart when we searched for an activity but for an interval.
     *
     * @param list list of times
     */
    private void setUpLongerPieChart(List<ActivityTimeFirebase> list, long from, long to, int gender, int ageGroup) {
        PieChart chart = binding.gLongerPieChart;

        chart.getDescription().setEnabled(true);
        chart.getDescription().setText(getString(R.string.pie_description_global_average));

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        List<Integer> colors = new ArrayList<>();
        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 2; i > 0; i--) {
            for (int j = 0; j < 6; j++) {
                float result = containsGroup(list, i, j, from, gender, ageGroup);
                if (result != 0f) {
                    int a = i == 1 ? 6 : 0;
                    values.add(new PieEntry(DateConverter.durationConverterForPieChart(result), labelAll[a + j]));
                    colors.add(allColors[a + j]);
                }
            }
        }

        PieDataSet set1 = new PieDataSet(values, "");
        set1.setColors(colors);
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
        chart.setCenterTextSize(16f);
        chart.setCenterText(
                DateConverter.longMillisToStringForSimpleDateDialog(from) + "\n" +
                        DateConverter.longMillisToStringForSimpleDateDialog(to)
                        + "\n" + getString(R.string.pie_average)
        );

        chart.invalidate();
    }

    /**
     * Returns the average time of activity with the given gender and age group if the list includes it,
     * otherwise returns 0. It divides time with count.
     *
     * @param list     list of time
     * @param gender   gender we search for
     * @param ageGroup age group we search0
     * @return the average time of activity with the given gender and age group if the list includes it, otherwise 0
     */
    private float containsType(List<ActivityTimeFirebase> list, int gender, int ageGroup) {
        ActivityTimeFirebase activityTime = list.stream()
                .filter(at -> at.getG() == gender && at.getA() == ageGroup)
                .findAny()
                .orElse(null);
        if (activityTime != null) {
            return DateConverter.durationConverterFromLongToBarChart(activityTime.getT() / activityTime.getC());
        } else {
            return 0f;
        }
    }

    /**
     * Returns the average time of activity in the list with the given date if the list includes it,
     * otherwise returns 0. It divides the sum of time with the sum of count.
     *
     * @param list     list of times
     * @param date     date we search for
     * @param gender   gender we searched for
     * @param ageGroup age group we searched for
     * @return the average time of activities with the given date if the list includes it, otherwise 0
     */
    private float containsDate(List<ActivityTimeFirebase> list, long date, int gender, int ageGroup) {
        long sum = 0L;
        long count = 0L;
        if (gender != 0) {
            if (ageGroup != -1) {
                sum = list.stream().filter(a -> a.getD() == date && a.getG() == gender && a.getA() == ageGroup).mapToLong(ActivityTimeFirebase::getT).sum();
                count = list.stream().filter(a -> a.getD() == date && a.getG() == gender && a.getA() == ageGroup).mapToInt(ActivityTimeFirebase::getC).sum();
            } else {
                sum = list.stream().filter(a -> a.getD() == date && a.getG() == gender).mapToLong(ActivityTimeFirebase::getT).sum();
                count = list.stream().filter(a -> a.getD() == date && a.getG() == gender).mapToInt(ActivityTimeFirebase::getC).sum();
            }
        } else {
            if (ageGroup != -1) {
                sum = list.stream().filter(a -> a.getD() == date && a.getA() == ageGroup).mapToLong(ActivityTimeFirebase::getT).sum();
                count = list.stream().filter(a -> a.getD() == date && a.getA() == ageGroup).mapToInt(ActivityTimeFirebase::getC).sum();
            } else {
                sum = list.stream().filter(a -> a.getD() == date).mapToLong(ActivityTimeFirebase::getT).sum();
                count = list.stream().filter(a -> a.getD() == date).mapToInt(ActivityTimeFirebase::getC).sum();
            }
        }
        if (sum != 0L && count != 0) {
            return DateConverter.durationConverterFromLongToBarChart(sum / count);
        } else {
            return 0f;
        }
    }

    /**
     * Returns the average time of activity in the list with the given gender and age group if the list includes it
     * and is later then the given date,
     * otherwise returns 0. It divides the sum of time with the sum of count.
     *
     * @param list     list of times
     * @param gender   gender we want to return
     * @param ageGroup age group we want to return
     * @param from     date after which we searched for
     * @param g        gender we searched for
     * @param ag       age group we searched for
     * @return the average time of activities with the given gender and age group if the list includes it, otherwise 0
     */
    private float containsGroup(List<ActivityTimeFirebase> list, int gender, int ageGroup, long from, int g, int ag) {
        long sum = 0L;
        long count = 0L;
        if (g == 0 && ag == -1) {
            sum = list.stream().filter(a -> a.getG() == gender && a.getA() == ageGroup && a.getD() >= from)
                    .mapToLong(ActivityTimeFirebase::getT).sum();
            count = list.stream().filter(a -> a.getG() == gender && a.getA() == ageGroup && a.getD() >= from)
                    .mapToInt(ActivityTimeFirebase::getC).sum();
        } else if (g != 0) {
            sum = list.stream().filter(a -> a.getG() == gender && a.getG() == g && a.getA() == ageGroup && a.getD() >= from)
                    .mapToLong(ActivityTimeFirebase::getT).sum();
            count = list.stream().filter(a -> a.getG() == gender && a.getG() == g && a.getA() == ageGroup && a.getD() >= from)
                    .mapToInt(ActivityTimeFirebase::getC).sum();
        } else if (ag != 0) {
            sum = list.stream().filter(a -> a.getG() == gender && a.getA() == ag && a.getA() == ageGroup && a.getD() >= from)
                    .mapToLong(ActivityTimeFirebase::getT).sum();
            count = list.stream().filter(a -> a.getG() == gender && a.getA() == ag && a.getA() == ageGroup && a.getD() >= from)
                    .mapToInt(ActivityTimeFirebase::getC).sum();
        }
        if (sum != 0L && count != 0) {
            return (float) (sum / count);
        } else {
            return 0f;
        }
    }

    /**
     * Initializes filter button which navigates to global filter fragment.
     *
     * @param fragView root view of the fragment
     */
    private void initFilterButton(View fragView) {
        binding.gFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!InternetConnectionHelper.hasInternetConnection()) {
                    Toast.makeText(getContext(), R.string.connect_to_stable_internet, Toast.LENGTH_SHORT).show();
                } else {
                    Navigation.findNavController(fragView).navigate(R.id.action_nav_statistics_to_globalFilterFragment);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}