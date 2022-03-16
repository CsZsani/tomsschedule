package hu.janny.tomsschedule.ui.main.details;

import androidx.annotation.ColorInt;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.DetailFragmentBinding;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.ActivityWithTimes;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.entities.CustomWeekTime;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.viewmodel.MainViewModel;
import hu.janny.tomsschedule.ui.main.editactivity.EditActivityFragment;
import hu.janny.tomsschedule.ui.main.timeadding.AddTimeFragment;
import hu.janny.tomsschedule.ui.timeractivity.TimerActivity;

/**
 * This fragment shows the details of the chosen activity. It presents the name, note, priority,
 * regularity type, duration, deadline and a bar chart that shows the time spent on this activity
 * in the last 7 days.
 */
public class DetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    public static final String TODAY_SO_FAR = "today_so_far";

    private DetailFragmentBinding binding;
    private MainViewModel mainViewModel;

    private AlertDialog deleteDialog;
    //ActionBar actionBar;

    public static DetailFragment newInstance() {
        return new DetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Binds layout
        binding = DetailFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets a MainViewModel instance
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        //actionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();

        // Gets activity id argument from the calling view
        // If there is no argument, pop back stack.
        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            long id = getArguments().getLong(ARG_ITEM_ID);
            mainViewModel.findActivityByIdWithTimesEntity(id);
        } else {
            Navigation.findNavController(view).popBackStack();
        }

        // Observer of the activity to get the data from the database
        mainViewModel.getActivityByIdWithTimesEntity().observe(getViewLifecycleOwner(), new Observer<ActivityWithTimes>() {
            @Override
            public void onChanged(ActivityWithTimes activityWithTimes) {
                CustomActivity activity = activityWithTimes.customActivity;
                if (activity != null) {
                    // Gets how many time were spent today on the activity
                    List<ActivityTime> times = activityWithTimes.activityTimes;

                    // Sets up the UI based on the activity's parameters that comes from the database

                    // Sets up name
                    if (CustomActivityHelper.isFixActivity(activity.getName())) {
                        binding.activityDetailName.setText(CustomActivityHelper.getStringResourceOfFixActivity(activity.getName()));
                    } else {
                        binding.activityDetailName.setText(activity.getName());
                    }
                    // Sets up UI items with the activity color
                    /*requireActivity().getWindow().setStatusBarColor(darkenColor(darkenColor(activity.getCol())));
                    if(actionBar != null ){
                        actionBar.setBackgroundDrawable(new ColorDrawable(darkenColor(activity.getCol())));
                    }*/
                    binding.toolbarLayout.setBackgroundColor(activity.getCol());
                    binding.detailToolbar.setBackgroundColor(activity.getCol());
                    binding.minusTimeFab.setBackgroundTintList(ColorStateList.valueOf(darkenColor(activity.getCol())));
                    binding.minusTimeFab.setRippleColor(darkenColor(darkenColor(activity.getCol())));
                    binding.plusTimeFab.setBackgroundTintList(ColorStateList.valueOf(darkenColor(activity.getCol())));
                    binding.plusTimeFab.setRippleColor(darkenColor(darkenColor(activity.getCol())));
                    binding.startTimerFab.setBackgroundTintList(ColorStateList.valueOf(darkenColor(activity.getCol())));
                    binding.startTimerFab.setRippleColor(darkenColor(darkenColor(activity.getCol())));
                    binding.toolbarLayout.setContentScrimColor(darkenColor(activity.getCol()));
                    // Sets up note and priority
                    if (activity.getNote().equals("")) {
                        binding.detailNote.setText("-");
                    } else {
                        binding.detailNote.setText(activity.getNote());
                    }
                    binding.detailPriority.setText(String.valueOf(activity.getPr()));

                    // Sets up view that changes according to data
                    setUpTheViewRegularity(activity);
                    setUpTheViewDeadline(activity);
                    setUpTheViewDuration(activity);
                    setUpTable(activity);

                    // Sets up action buttons
                    setUpDeleteDialog(activity.getId(), view);
                    setUpEditButton(activity.getId(), view);
                    setUpStartActivityButton(activity.getId(), activity.getName());
                    setUpAddTimeButton(activity.getId(), activity.getName(), view);
                    setUpSubtractionButton(activity.getId(), activity.getName(), view);

                    // Sets up the bar chart for the last seven days
                    setUpBarChart(activityWithTimes.activityTimes, activity.getCol());
                } else {
                    Navigation.findNavController(view).popBackStack();
                    Toast.makeText(getActivity(), getString(R.string.detail_act_no_act), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Sets up delete button
        setUpDeleteActivity();

    }

    /**
     * Sets up the bar chart that shows the time was spent on activity in the last 7 days.
     *
     * @param list  list of times
     * @param color the color of the activity
     */
    private void setUpBarChart(List<ActivityTime> list, int color) {
        BarChart chart = binding.chart;

        int MAX_X_VALUE = 7;
        String SET_LABEL = getString(R.string.detail_bar_chart_label);
        String[] DAYS = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(true);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        ArrayList<BarEntry> values = new ArrayList<>();

        LocalDate localDate = LocalDate.now();
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        long today = instant.toEpochMilli();

        for (int i = MAX_X_VALUE - 1; i >= 0; i--) {
            float x = i;
            values.add(new BarEntry(x, containsDate(list, today)));
            DAYS[i] = String.format(Locale.getDefault(), "%02d.%02d.", localDate.getMonthValue(), localDate.getDayOfMonth());
            localDate = localDate.minusDays(1);
            today = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(DAYS));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(1.0f);
        axisLeft.setAxisMinimum(0);
        axisLeft.setValueFormatter(new HourValueFormatter());
        axisLeft.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        YAxis axisRight = chart.getAxisRight();
        axisRight.setEnabled(false);
        axisRight.setGranularity(0.5f);
        axisRight.setAxisMinimum(0);

        //BarDataSet set1 = new BarDataSet(values, SET_LABEL);
        BarDataSet set1 = new BarDataSet(values, SET_LABEL);
        set1.setColor(color);
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

        chart.setDrawBorders(true);
        chart.setBorderColor(darkenColor(darkenColor(color)));
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
            return DateConverter.chartTimeConverter(barEntry.getY());
        }
    }

    /**
     * Returns the time value of the activity time if its date equals to the given date.
     * Otherwise it returns 0f.
     *
     * @param list activity time list
     * @param date the date we are searching for in the list
     * @return time value of the activity time for the given day
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
     * Sets up the click listener of the add time button. When clicked, it navigates to add time fragment.
     * It sends a bundle with the id, and a boolean true, which means that the time is need to be added.
     *
     * @param activityId   the activity id, we send it in a bundle to add time fragment
     * @param activityName name of the activity
     * @param fragView     root view of the fragment
     */
    private void setUpAddTimeButton(long activityId, String activityName, View fragView) {
        binding.plusTimeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = new Bundle();
                arguments.putLong(AddTimeFragment.ITEM_ID, activityId);
                arguments.putBoolean(AddTimeFragment.OPERATION_TYPE, true);
                Navigation.findNavController(fragView).navigate(R.id.action_detailFragment_to_addTimeFragment, arguments);
            }
        });
    }

    /**
     * Sets up the click listener of the subtraction time button. When clicked, it navigates to add time fragment.
     * It sends a bundle with the id, and a boolean false, which means that the time is need to be subtracted.
     *
     * @param activityId   the activity id, we send it in a bundle to add time fragment
     * @param activityName name of the activity
     * @param fragView     root view of the fragment
     */
    private void setUpSubtractionButton(long activityId, String activityName, View fragView) {
        binding.minusTimeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = new Bundle();
                arguments.putLong(AddTimeFragment.ITEM_ID, activityId);
                arguments.putBoolean(AddTimeFragment.OPERATION_TYPE, false);
                Navigation.findNavController(fragView).navigate(R.id.action_detailFragment_to_addTimeFragment, arguments);
            }
        });
    }

    /**
     * Sets up the click listener of the edit activity button. When clicked, it navigates to edit fragment.
     *
     * @param activityId the activity id, we send it in a bundle to edit fragment
     * @param fragView   root view of the fragment
     */
    private void setUpEditButton(long activityId, View fragView) {
        binding.editActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = new Bundle();
                arguments.putLong(EditActivityFragment.ARG_ITEM_ID, activityId);
                Navigation.findNavController(fragView).navigate(R.id.action_detailFragment_to_editActivityFragment, arguments);
            }
        });
    }

    /**
     * Sets up the dialog for deleting an activity. It asks if we are sure that we want to delete the activity.
     *
     * @param activityId the id of the activity we want to delete
     * @param fragView   root view of the fragment
     */
    private void setUpDeleteDialog(long activityId, View fragView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.sure_delete_acitvity);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mainViewModel.deleteActivityById(activityId);
                // mainViewModel.deleteActivityTimesByActivityId(activityId);
                Navigation.findNavController(fragView).popBackStack();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog - nothing happens
            }
        });
        deleteDialog = builder.create();
    }

    /**
     * Sets up the click listener of the start timer button. When clicked, it starts the timer activity.
     * It sends an intent with arguments: the id, the name, and soFar.
     *
     * @param activityId   id of the activity to send to timer activity
     * @param activityName name of the activity to send to timer activity
     */
    private void setUpStartActivityButton(long activityId, String activityName) {
        binding.startTimerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), TimerActivity.class);
                i.putExtra(TimerActivity.ACTIVITY_ID, activityId);
                i.putExtra(TimerActivity.ACTIVITY_NAME, activityName);
                startActivity(i);
                Objects.requireNonNull(getActivity()).finish();
            }
        });

    }

    /**
     * Sets up delete activity button. It shows a confirmation dialog before we delete it.
     */
    private void setUpDeleteActivity() {
        binding.deleteActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.show();
            }
        });
    }

    /**
     * Sets up the UI item that show the given duration of the activity.
     *
     * @param activity the activity we want to display
     */
    private void setUpTheViewDuration(CustomActivity activity) {
        if (activity.gettT() > 0) {
            switch (activity.gettT()) {
                case 1:
                    binding.detailDurationText.setText(R.string.details_sum_time);
                    binding.detailDuration.setText(DateConverter.durationConverterFromLongToString(activity.getDur()));
                    break;
                case 2:
                    binding.detailDurationText.setText(R.string.details_daily_time);
                    binding.detailDuration.setText(DateConverter.durationConverterFromLongToString(activity.getDur()));
                    break;
                case 3:
                    binding.detailDurationText.setText(R.string.details_weekly_time);
                    binding.detailDuration.setText(DateConverter.durationConverterFromLongToString(activity.getDur()));
                    break;
                case 4:
                    binding.detailDurationText.setText(R.string.details_monthly_time);
                    binding.detailDuration.setText(DateConverter.durationConverterFromLongToString(activity.getDur()));
                    break;
                case 5:
                    binding.detailDurationText.setText(R.string.details_custom_time);
                    binding.detailDuration.setText(selectedWeeklyDaysTimeToString(activity.getCustomWeekTime()));
                    break;
            }
            binding.detailDurationText.setVisibility(View.VISIBLE);
            binding.detailDuration.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets up the UI item that show the deadlines of the activity.
     *
     * @param activity the activity we want to display
     */
    private void setUpTheViewDeadline(CustomActivity activity) {
        binding.detailDeadlineText.setVisibility(View.VISIBLE);
        binding.detailDeadline.setVisibility(View.VISIBLE);
        if (activity.getsD() != 0L && activity.geteD() != 0L) {
            binding.detailDeadlineText.setText(R.string.details_interval);
            String text = DateConverter.longMillisToStringForSimpleDateDialog(activity.getsD())
                    + " - " + DateConverter.longMillisToStringForSimpleDateDialog(activity.geteD());
            binding.detailDeadline.setText(text);
        } else if (activity.getsD() == 0L && activity.geteD() != 0L) {
            binding.detailDeadlineText.setText(R.string.details_end_date);
            binding.detailDeadline.setText(DateConverter.longMillisToStringForSimpleDateDialog(activity.geteD()));
        } else {
            binding.detailDeadlineText.setVisibility(View.GONE);
            binding.detailDeadline.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up the UI item that show the regularity of the activity.
     *
     * @param activity the activity we want to display
     */
    private void setUpTheViewRegularity(CustomActivity activity) {
        if (activity.getReg() > 0) {
            switch (activity.getReg()) {
                case 1:
                    binding.detailRegularity.setText(R.string.details_daily);
                    break;
                case 2:
                    if (activity.ishFD()) {
                        String text = getString(R.string.details_weekly) + " - " + selectedWeeklyDaysToString(activity);
                        binding.detailRegularity.setText(text);
                    } else {
                        binding.detailRegularity.setText(R.string.details_weekly);
                    }
                    break;
                case 3:
                    binding.detailRegularity.setText(R.string.details_monthly);
                    break;
            }
            binding.detailRegularityText.setVisibility((View.VISIBLE));
            binding.detailRegularity.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Sets up the UI item that show the soFar, remaining and allTime fields of the activity.
     *
     * @param activity the activity we want to display
     */
    private void setUpTable(CustomActivity activity) {
        binding.detailAllTime.setText(DateConverter.durationConverterFromLongToString(activity.getaT()));
        binding.detailSoFar.setText(CustomActivityHelper.getSoFar(activity));
        binding.detailRemaining.setText(CustomActivityHelper.getRemaining(activity));
    }

    /**
     * Creates a string to display the custom week days for the activity. It shows only the selected days.
     *
     * @param activity the object that holds the custom week time data
     * @return a string to display in a readable format, every day is in a different row
     */
    private String selectedWeeklyDaysToString(CustomActivity activity) {
        StringBuilder s = new StringBuilder("");
        boolean notFirst = false;
        if (activity.getCustomWeekTime().getMon() != -1L) {
            s.append(getString(R.string.monday));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getTue() != -1L) {
            if (notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.tuesday));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getWed() != -1L) {
            if (notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.wednesday));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getThu() != -1L) {
            if (notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.thursday));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getFri() != -1L) {
            if (notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.friday));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getSat() != -1L) {
            if (notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.saturday));
            notFirst = true;
        }
        if (activity.getCustomWeekTime().getSun() != -1L) {
            if (notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.sunday));
        }
        return s.toString();
    }

    /**
     * Creates a string to display the custom week time for the activity. It shows the selected days and the
     * duration that was set to them.
     *
     * @param customWeekTime the object that holds the custom week time data
     * @return a string to display in a readable format, every day is in a different row
     */
    private String selectedWeeklyDaysTimeToString(CustomWeekTime customWeekTime) {
        StringBuilder s = new StringBuilder("");
        boolean notFirst = false;
        if (customWeekTime.getMon() != -1L) {
            s.append(getString(R.string.monday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getMon()));
            notFirst = true;
        }
        if (customWeekTime.getTue() != -1L) {
            if (notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.tuesday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getTue()));
            notFirst = true;
        }
        if (customWeekTime.getWed() != -1L) {
            if (notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.wednesday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getWed()));
            notFirst = true;
        }
        if (customWeekTime.getThu() != -1L) {
            if (notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.thursday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getThu()));
            notFirst = true;
        }
        if (customWeekTime.getFri() != -1L) {
            if (notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.friday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getFri()));
            notFirst = true;
        }
        if (customWeekTime.getSat() != -1L) {
            if (notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.saturday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getSat()));
            notFirst = true;
        }
        if (customWeekTime.getSun() != -1L) {
            if (notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.sunday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getSun()));
        }
        return s.toString();
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