package hu.janny.tomsschedule.ui.main.statistics;

import static java.time.temporal.ChronoUnit.DAYS;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.graphics.Color;
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
import hu.janny.tomsschedule.viewmodel.GlobalStatisticsViewModel;

public class GlobalStatisticsFragment extends Fragment {

    private FragmentGlobalStatisticsBinding binding;
    private GlobalStatisticsViewModel viewModel;
    private Context context;

    /*private final int[] femaleColors = new int[]{ResourcesCompat.getColor(getActivity().getResources(), R.color.female_0, null),
            ResourcesCompat.getColor(getActivity().getResources(), R.color.female_1, null), R.color.female_2, R.color.female_3, R.color.female_4, R.color.female_5};
    private final int[] maleColors = new int[]{R.color.male_0, R.color.male_1, R.color.male_2, R.color.male_3, R.color.male_4, R.color.male_5};
    private final int[] allColors = new int[]{R.color.female_0, R.color.female_1, R.color.female_2, R.color.female_3, R.color.female_4, R.color.female_5,
            R.color.male_0, R.color.male_1, R.color.male_2, R.color.male_3, R.color.male_4, R.color.male_5};*/
    private final int[] femaleColors = new int[]{Color.parseColor("#FFB5E6"), Color.parseColor("#FF96DC"), Color.parseColor("#FF74D0"),
            Color.parseColor("#FF54C5"), Color.parseColor("#FF29B7"), Color.parseColor("#FF00A9")};
    private final int[] maleColors = new int[]{Color.parseColor("#A7C5FF"), Color.parseColor("#84AEFF"), Color.parseColor("#6297FF"),
            Color.parseColor("#4A88FF"), Color.parseColor("#256FFF"), Color.parseColor("#0057FF")};
    private final int[] allColors = new int[]{Color.parseColor("#FFB5E6"), Color.parseColor("#FF96DC"), Color.parseColor("#FF74D0"),
            Color.parseColor("#FF54C5"), Color.parseColor("#FF29B7"), Color.parseColor("#FF00A9"),
            Color.parseColor("#A7C5FF"), Color.parseColor("#84AEFF"), Color.parseColor("#6297FF"),
            Color.parseColor("#4A88FF"), Color.parseColor("#256FFF"), Color.parseColor("#0057FF")};
    private String[] labelFemale;
    private String[] labelMale;
    private String[] labelAll;

    private long from = 0L;
    private long to = 0L;
    private int gender = 0;
    private int ageGroup = -1;
    private String name = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(GlobalStatisticsViewModel.class);
        binding = FragmentGlobalStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        labelFemale = new String[]{getActivity().getString(R.string.fe_0), getActivity().getString(R.string.fe_1), getActivity().getString(R.string.fe_2),
                getActivity().getString(R.string.fe_3), getActivity().getString(R.string.fe_4), getActivity().getString(R.string.fe_5)};
        labelMale = new String[]{getActivity().getString(R.string.ma_0), getActivity().getString(R.string.ma_1), getActivity().getString(R.string.ma_2),
                getActivity().getString(R.string.ma_3), getActivity().getString(R.string.ma_4), getActivity().getString(R.string.ma_5)};
        labelAll = new String[]{getActivity().getString(R.string.fe_0), getActivity().getString(R.string.fe_1), getActivity().getString(R.string.fe_2),
                getActivity().getString(R.string.fe_3), getActivity().getString(R.string.fe_4), getActivity().getString(R.string.fe_5),
                getActivity().getString(R.string.ma_0), getActivity().getString(R.string.ma_1), getActivity().getString(R.string.ma_2),
                getActivity().getString(R.string.ma_3), getActivity().getString(R.string.ma_4), getActivity().getString(R.string.ma_5)};

        initFilterButton(root);

        observeDataChanges();

        return root;
    }

    private void observeDataChanges() {
        viewModel.getTimesList().observe(getViewLifecycleOwner(), new Observer<List<ActivityTimeFirebase>>() {
            @Override
            public void onChanged(List<ActivityTimeFirebase> activityTimeFirebases) {
                from = viewModel.getFrom();
                to = viewModel.getTo();
                ageGroup = viewModel.getAgeGroup();
                gender = viewModel.getGender();
                name = viewModel.getName();
                System.out.println(from + " " + to + " " + gender + " " + ageGroup + " " + name);
                setUpCharts(activityTimeFirebases);
            }
        });
    }

    private void setUpCharts(List<ActivityTimeFirebase> list) {
        if(list.isEmpty()) {
            Toast.makeText(getActivity(), "There is no data!", Toast.LENGTH_LONG).show();
            binding.gPleaseFilter.setVisibility(View.VISIBLE);
            return;
        }
        binding.gDayBarChart.setVisibility(View.GONE);
        binding.gLongerBarChart.setVisibility(View.GONE);
        binding.gLongerPieChart.setVisibility(View.GONE);
        if(from == 0L) {
            setUpDayBarChart(list, to);
            binding.gDayBarChart.setVisibility(View.VISIBLE);
        } else {
            setUpLongerBarChart(list, from, to);
            binding.gLongerBarChart.setVisibility(View.VISIBLE);
            setUpLongerPieChart(list);
            binding.gLongerPieChart.setVisibility(View.VISIBLE);
        }

    }

    private void setUpDayBarChart(List<ActivityTimeFirebase> list, long date) {
        BarChart chart = binding.gDayBarChart;

        int MAX_X_VALUE = 12;
        String SET_LABEL = "Time spent on this activity on " + DateConverter.longMillisToStringForSimpleDateDialog(date);
        String[] NAMES = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(true);

        ArrayList<BarEntry> values = new ArrayList<>();
        int a = 0;
        for (int i = 2; i>0; i--) {
            for(int j = 0; j<6; j++) {
                values.add(new BarEntry(a, containsType(list, i, j)));
                System.out.println("itt");
                //NAMES[i * j + 1] = labelAll[i * j + 1];
                a++;
            }
        }

        Legend legend = chart.getLegend();
        /*LegendEntry[] legendEntries = new LegendEntry[12];
        for(int i = 0; i< legendEntries.length / 2; i++) {
            LegendEntry legendEntry = new LegendEntry();
            legendEntry.formColor = femaleColors[i];
            legendEntry.label = labelFemale[i];
            legendEntries[i] = legendEntry;
            LegendEntry legendEntry2 = new LegendEntry();
            legendEntry.formColor = maleColors[i];
            legendEntry.label = labelMale[i];
            legendEntries[i + 6] = legendEntry2;
        }
        legend.setCustom(legendEntries);*/
        legend.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelAll));
        xAxis.setCenterAxisLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setEnabled(true);

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
        set1.setColors(allColors);
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

    private void setUpLongerBarChart(List<ActivityTimeFirebase> list, long from, long to) {
        BarChart chart = binding.gLongerBarChart;

        LocalDate dateBefore = Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateAfter = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        long daysBetween = DAYS.between(dateBefore, dateAfter);

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
        LocalDate localDate = Instant.ofEpochMilli(to).atZone(ZoneId.systemDefault()).toLocalDate();
        long millis = to;
        while(millis != from) {
            values.add(new BarEntry(i, containsDate(list, millis)));
            NAMES[i] = String.format(Locale.getDefault(), "%02d.%02d.", localDate.getMonthValue(), localDate.getDayOfMonth());
            i++;
            localDate = localDate.minusDays(1);
            millis = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        XAxis xAxis = chart.getXAxis();
        /*xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return NAMES[(int) value];
            }
        });*/
        xAxis.setValueFormatter(new IndexAxisValueFormatter(NAMES));
        xAxis.setCenterAxisLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setEnabled(true);

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(0.5f);
        axisLeft.setAxisMinimum(0);

        YAxis axisRight = chart.getAxisRight();
        axisRight.setGranularity(0.5f);
        axisRight.setAxisMinimum(0);

        BarDataSet set1 = new BarDataSet(values, "Average time spent in hours");
        set1.setColor(Objects.requireNonNull(getActivity()).getResources().getColor(R.color.toms_400, null));

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

    private void setUpLongerPieChart(List<ActivityTimeFirebase> list) {
        PieChart chart = binding.gLongerPieChart;

        chart.getDescription().setEnabled(false);

        List<Integer> colors = new ArrayList<>();
        ArrayList<PieEntry> values = new ArrayList<>();
        for (int i = 2; i>0; i--) {
            for (int j = 0; j < 6; j++) {
                float result = containsGroup(list, i, j);
                if(result != 0f) {
                    int a;
                    if (i == 1) {
                        a = 6;
                    } else {
                        a = 0;
                    }
                    values.add(new PieEntry(DateConverter.durationConverterForPieChart(result), labelAll[a + j]));
                    colors.add(allColors[a+j]);
                }
            }
        }

        PieDataSet set1 = new PieDataSet(values, "All time spent in minutes");
        set1.setColors(colors);

        PieData pieData = new PieData(set1);
        pieData.setValueTextSize(12f);
        chart.setData(pieData);
        chart.invalidate();
    }

    private float containsType(List<ActivityTimeFirebase> list, int gender, int ageGroup) {
        ActivityTimeFirebase activityTime = list.stream()
                .filter(at -> at.getG() == gender && at.getA() == ageGroup)
                .findAny()
                .orElse(null);
        if(activityTime != null) {
            return DateConverter.durationConverterFromLongToBarChart(activityTime.getT() / activityTime.getC());
        } else {
            return 0f;
        }
    }

    private float containsDate(List<ActivityTimeFirebase> list, long date) {
        long sum = list.stream().filter(a -> a.getD() == date).mapToLong(ActivityTimeFirebase::getT).sum();
        long count = list.stream().filter(a -> a.getD() == date).mapToInt(ActivityTimeFirebase::getC).sum();
        if(sum != 0L && count != 0) {
            return DateConverter.durationConverterFromLongToBarChart(sum / count);
        } else {
            return 0f;
        }
    }

    private float containsGroup(List<ActivityTimeFirebase> list, int gender, int ageGroup) {
        long sum = list.stream().filter(a -> a.getG() == gender && a.getA() == ageGroup).mapToLong(ActivityTimeFirebase::getT).sum();
        int count = list.stream().filter(a -> a.getG() == gender && a.getA() == ageGroup).mapToInt(ActivityTimeFirebase::getC).sum();
        if(sum != 0L && count != 0) {
            return (float) (sum / count);
        } else {
            return 0f;
        }
    }

    private void initFilterButton(View fragView) {
        binding.gFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(fragView).navigate(R.id.action_nav_statistics_to_globalFilterFragment);
            }
        });
    }


}