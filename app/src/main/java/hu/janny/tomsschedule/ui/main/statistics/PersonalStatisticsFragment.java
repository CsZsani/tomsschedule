package hu.janny.tomsschedule.ui.main.statistics;

import static java.time.temporal.ChronoUnit.DAYS;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentResultListener;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentPersonalStatisticsBinding;
import hu.janny.tomsschedule.databinding.FragmentStatisticsBinding;
import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.DateConverter;
import hu.janny.tomsschedule.ui.main.MainViewModel;
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
                periodType = viewModel.getpPeriodType();
                activityNum = viewModel.getpActivityNum();
                activities = viewModel.getActsList();
                colors = viewModel.getColors();
                names = viewModel.getNames();
                System.out.println(activityNum + " " + periodType);
                if(activityNum == 0 && (periodType == 0 || periodType == 1 || periodType == 6)) {
                    if(activityTimes.isEmpty()) {
                        Toast.makeText(getActivity(), "No data available on the given day!", Toast.LENGTH_LONG).show();
                    } else {
                        setUpAllBarChart(activityTimes);
                        binding.allBarChart.setVisibility(View.VISIBLE);
                        setUpAllPieChartForSingleDays(activityTimes);
                        binding.allPieChart.setVisibility(View.VISIBLE);
                    }
                }
                if(activityNum == 0 && periodType == 2) {
                    if(activityTimes.isEmpty()) {
                        Toast.makeText(getActivity(), "No data available on the given day!", Toast.LENGTH_LONG).show();
                    } else {
                        setUpAllBarChartLonger(activityTimes, CustomActivityHelper.minusWeekMillis(1));
                        binding.allStackedBarChart.setVisibility(View.VISIBLE);
                        setUpAllPieChartForLonger(activityTimes);
                        binding.allPieChart.setVisibility(View.VISIBLE);
                    }
                }
                if(activityNum == 0 && periodType == 3) {
                    if(activityTimes.isEmpty()) {
                        Toast.makeText(getActivity(), "No data available on the given day!", Toast.LENGTH_LONG).show();
                    } else {
                        setUpAllBarChartLonger(activityTimes, CustomActivityHelper.minusWeekMillis(2));
                        binding.allStackedBarChart.setVisibility(View.VISIBLE);
                        setUpAllPieChartForLonger(activityTimes);
                        binding.allPieChart.setVisibility(View.VISIBLE);
                    }
                }
                if(activityNum == 0 && periodType == 4) {
                    if(activityTimes.isEmpty()) {
                        Toast.makeText(getActivity(), "No data available on the given day!", Toast.LENGTH_LONG).show();
                        System.out.println(CustomActivityHelper.minusMonthMillis(1));
                        System.out.println(CustomActivityHelper.todayMillis());
                    } else {
                        setUpAllBarChartLonger(activityTimes, CustomActivityHelper.minusMonthMillis(1));
                        System.out.println(CustomActivityHelper.minusMonthMillis(1));
                        binding.allStackedBarChart.setVisibility(View.VISIBLE);
                        setUpAllPieChartForLonger(activityTimes);
                        binding.allPieChart.setVisibility(View.VISIBLE);
                    }
                }
                if(activityNum == 0 && periodType == 5) {
                    if(activityTimes.isEmpty()) {
                        Toast.makeText(getActivity(), "No data available on the given day!", Toast.LENGTH_LONG).show();
                    } else {
                        setUpAllBarChartLonger(activityTimes, CustomActivityHelper.minusMonthMillis(3));
                        binding.allStackedBarChart.setVisibility(View.VISIBLE);
                        setUpAllPieChartForLonger(activityTimes);
                        binding.allPieChart.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        return root;
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

        BarDataSet set1 = new BarDataSet(values, SET_LABEL);
        set1.setColors(colors);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);

        data.setValueTextSize(12f);
        chart.setData(data);
        chart.setScaleEnabled(true);
        chart.setDragEnabled(true);
        chart.setPinchZoom(false);
        chart.invalidate();
    }

    private void setUpAllPieChartForSingleDays(List<ActivityTime> activityTimes) {
        PieChart chart = binding.allPieChart;

        chart.getDescription().setEnabled(false);

        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 0; i<activityTimes.size(); i++) {
            values.add(new PieEntry(DateConverter.durationConverterFromLongToChartInt(activityTimes.get(i).getT()), names.get(activities.indexOf(activityTimes.get(i).getaId()))));
        }

        PieDataSet set1 = new PieDataSet(values, "");
        set1.setColors(colors);

        PieData pieData = new PieData(set1);
        pieData.setValueTextSize(12f);
        chart.setData(pieData);
        chart.invalidate();
    }

    private void setUpAllBarChartLonger(List<ActivityTime> activityTimes, long from) {
        BarChart chart = binding.allStackedBarChart;

        LocalDate localDate = LocalDate.now();
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        long millis = instant.toEpochMilli();

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateAfter = LocalDate.now();
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

        BarDataSet set1 = new BarDataSet(values, "");
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

        PieDataSet set1 = new PieDataSet(values, "");
        set1.setColors(colors);

        PieData pieData = new PieData(set1);
        pieData.setValueTextSize(12f);
        chart.setData(pieData);
        chart.invalidate();
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