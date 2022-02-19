package hu.janny.tomsschedule.ui.main.details;

import androidx.annotation.ColorInt;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.DetailFragmentBinding;
import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.ActivityWithTimes;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.CustomWeekTime;
import hu.janny.tomsschedule.model.DateConverter;
import hu.janny.tomsschedule.ui.main.MainViewModel;
import hu.janny.tomsschedule.ui.main.editactivity.EditActivityFragment;
import hu.janny.tomsschedule.ui.main.timeadding.AddTimeFragment;
import hu.janny.tomsschedule.ui.timeractivity.TimerActivity;

public class DetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    public static final String TODAY_SO_FAR = "today_so_far";

    private DetailFragmentBinding binding;
    private MainViewModel mainViewModel;
    private AlertDialog deleteDialog;
    private long today;

    public static DetailFragment newInstance() {
        return new DetailFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        //return inflater.inflate(R.layout.detail_fragment, container, false);
        binding = DetailFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            long id = getArguments().getLong(ARG_ITEM_ID);
            //mainViewModel.findActivityByIdWithTimes(id);
            mainViewModel.findActivityByIdWithTimesEntity(id);
        }

        if (getArguments().containsKey(TODAY_SO_FAR)) {
            today = getArguments().getLong(TODAY_SO_FAR);
        }
        //System.out.println(today);

        //mainViewModel.getActivityByIdWithTimes().observe(getViewLifecycleOwner(), new Observer<Map<CustomActivity, List<ActivityTime>>>() {
        mainViewModel.getActivityByIdWithTimesEntity().observe(getViewLifecycleOwner(), new Observer<ActivityWithTimes>() {
            @Override
            public void onChanged(ActivityWithTimes activityWithTimes) {
                //System.out.println(activityWithTimes + " in detail frafgment");
                CustomActivity activity = activityWithTimes.customActivity;
                if(activity != null) {
                    if(CustomActivityHelper.isFixActivity(activity.getName())) {
                        binding.activityDetailName.setText(CustomActivityHelper.getStringResourceOfFixActivity(activity.getName()));
                    } else {
                        binding.activityDetailName.setText(activity.getName());
                    }
                    binding.toolbarLayout.setBackgroundColor(activity.getCol());
                    binding.detailToolbar.setBackgroundColor(activity.getCol());
                    binding.minusTimeFab.setBackgroundTintList(ColorStateList.valueOf(darkenColor(activity.getCol())));
                    binding.minusTimeFab.setRippleColor(darkenColor(darkenColor(activity.getCol())));
                    binding.plusTimeFab.setBackgroundTintList(ColorStateList.valueOf(darkenColor(activity.getCol())));
                    binding.plusTimeFab.setRippleColor(darkenColor(darkenColor(activity.getCol())));
                    binding.startTimerFab.setBackgroundTintList(ColorStateList.valueOf(darkenColor(activity.getCol())));
                    binding.startTimerFab.setRippleColor(darkenColor(darkenColor(activity.getCol())));
                    binding.toolbarLayout.setContentScrimColor(darkenColor(activity.getCol()));
                    if(activity.getNote().equals("")) {
                        binding.detailNote.setText("-");
                    } else {
                        binding.detailNote.setText(activity.getNote());
                    }
                    binding.detailPriority.setText(String.valueOf(activity.getPr()));

                    setUpTheViewRegularity(activity);
                    setUpTheViewDeadline(activity);
                    setUpTheViewDuration(activity);
                    setUpTable(activity);
                    setUpDeleteDialog(activity.getId(), root);
                    setUpEditButton(activity.getId(), root);
                    setUpStartActivityButton(activity.getId(), activity.getName());
                    setUpAddTimeButton(activity.getId(), activity.getName(), root);
                    setUpSubtractionButton(activity.getId(), activity.getName(), root);
                    setUpBarChart(activityWithTimes.activityTimes, activity.getCol());
                } else {
                    navigateBackHome(root);
                    Toast.makeText(getActivity(), "I can't find this activity!", Toast.LENGTH_LONG).show();
                }
            }
        });
        setUpDeleteActivity();

        return root;
    }




    private void setUpBarChart(List<ActivityTime> list, int color) {
        Collections.sort(list);

        BarChart chart = binding.chart;

        int MAX_X_VALUE = 7;
        float MAX_Y_VALUE = DateConverter.durationConverterFromLongToBarChart(list.get(0).t);
        float MIN_Y_VALUE = 0f;
        String SET_LABEL = "Time spent on this activity in the last week in hours";
        String[] DAYS = new String[MAX_X_VALUE];

        chart.getDescription().setEnabled(false);

        ArrayList<BarEntry> values = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date  = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year, month, date);
        long todayMillis = cal.getTimeInMillis();
        Random r = new Random();
        for (int i = MAX_X_VALUE - 1; i >= 0; i--) {
            float x = i;
            values.add(new BarEntry(x, containsName(list, todayMillis)));
            DAYS[i] = String.format(Locale.getDefault(), "%02d.%02d.", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
            cal.add(Calendar.DATE, -1);
            todayMillis = cal.getTimeInMillis();
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

        BarDataSet set1 = new BarDataSet(values, SET_LABEL);
        set1.setColor(color);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);

        data.setValueTextSize(12f);
        chart.setData(data);
        chart.invalidate();
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

    private void setUpAddTimeButton(long activityId, String activityName, View fragView) {
        binding.plusTimeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = new Bundle();
                arguments.putLong(AddTimeFragment.ITEM_ID, activityId);
                //arguments.putString(AddTimeFragment.ACTIVITY_NAME, activityName);
                arguments.putBoolean(AddTimeFragment.OPERATION_TYPE, true);
                Navigation.findNavController(fragView).navigate(R.id.action_detailFragment_to_addTimeFragment, arguments);
            }
        });
    }

    private void setUpSubtractionButton(long activityId, String activityName, View fragView) {
        binding.minusTimeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = new Bundle();
                arguments.putLong(AddTimeFragment.ITEM_ID, activityId);
                //arguments.putString(AddTimeFragment.ACTIVITY_NAME, activityName);
                arguments.putBoolean(AddTimeFragment.OPERATION_TYPE, false);
                Navigation.findNavController(fragView).navigate(R.id.action_detailFragment_to_addTimeFragment, arguments);
            }
        });
    }

    private void setUpEditButton(long id, View fragView) {
        binding.editActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = new Bundle();
                arguments.putLong(EditActivityFragment.ARG_ITEM_ID, id);
                Navigation.findNavController(fragView).navigate(R.id.action_detailFragment_to_editActivityFragment, arguments);
            }
        });
    }

    private void setUpDeleteDialog(long id, View fragView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.sure_delete_acitvity);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mainViewModel.deleteActivityById(id);
                mainViewModel.deleteActivityTimesByActivityId(id);
                navigateBackHome(fragView);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        deleteDialog = builder.create();
    }

    private void setUpStartActivityButton(long activityId, String activityName) {
        binding.startTimerFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), TimerActivity.class);
                i.putExtra(TimerActivity.ACTIVITY_ID, activityId);
                i.putExtra(TimerActivity.ACTIVITY_NAME, activityName);
                i.putExtra(TimerActivity.TODAY_SO_FAR, today);
                startActivity(i);
                Objects.requireNonNull(getActivity()).finish();
            }
        });

    }

    private void navigateBackHome(View fragView) {
        //Navigation.findNavController(fragView).navigate(R.id.action_detailFragment_to_nav_home);
        Navigation.findNavController(fragView).popBackStack();
    }

    private void setUpDeleteActivity() {
        binding.deleteActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.show();
            }
        });
    }

    private void setUpTheViewDuration(CustomActivity activity) {
        if(activity.gettT() > 0) {
            binding.detailDurationText.setVisibility(View.VISIBLE);
            binding.detailDuration.setVisibility(View.VISIBLE);
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
        }
    }

    private void setUpTheViewDeadline(CustomActivity activity) {
        binding.detailDeadlineText.setVisibility(View.VISIBLE);
        binding.detailDeadline.setVisibility(View.VISIBLE);
        if(activity.getsD() != 0L && activity.geteD() != 0L) {
            binding.detailDeadlineText.setText(R.string.details_interval);
            String text = DateConverter.longMillisToStringForSimpleDateDialog(activity.getsD())
                    + " - " + DateConverter.longMillisToStringForSimpleDateDialog(activity.geteD());
            binding.detailDeadline.setText(text);
        } else if(activity.getsD() == 0L && activity.geteD() != 0L) {
            binding.detailDeadlineText.setText(R.string.details_end_date);
            binding.detailDeadline.setText(DateConverter.longMillisToStringForSimpleDateDialog(activity.geteD()));
        } else {
            binding.detailDeadlineText.setVisibility(View.GONE);
            binding.detailDeadline.setVisibility(View.GONE);
        }
    }

    private void setUpTheViewRegularity(CustomActivity activity) {
        if(activity.getReg() > 0) {
            binding.detailRegularityText.setVisibility((View.VISIBLE));
            binding.detailRegularity.setVisibility(View.VISIBLE);
            switch (activity.getReg()) {
                case 1:
                    binding.detailRegularity.setText(R.string.details_daily);
                    break;
                case 2:
                    if(activity.ishFD()) {
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
        }

    }

    private void setUpTable(CustomActivity activity) {
        binding.detailAllTime.setText(DateConverter.durationConverterFromLongToString(activity.getaT()));
        binding.detailSoFar.setText(CustomActivityHelper.getSoFar(activity));
        binding.detailRemaining.setText(CustomActivityHelper.getRemaining(activity));
    }

    private String selectedWeeklyDaysToString(CustomActivity activity) {
        StringBuilder s = new StringBuilder("");
        boolean notFirst = false;
        if(activity.getCustomWeekTime().getMon() != -1L) {
            s.append(getString(R.string.monday));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getTue() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.tuesday));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getWed() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.wednesday));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getThu() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.thursday));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getFri() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.friday));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getSat() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.saturday));
            notFirst = true;
        }
        if(activity.getCustomWeekTime().getSun() != -1L) {
            if(notFirst) {
                s.append(", ");
            }
            s.append(getString(R.string.sunday));
            notFirst = true;
        }
        return s.toString();
    }

    private String selectedWeeklyDaysTimeToString(CustomWeekTime customWeekTime) {
        StringBuilder s = new StringBuilder("");
        boolean notFirst = false;
        if(customWeekTime.getMon() != -1L) {
            s.append(getString(R.string.monday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getMon()));
            notFirst = true;
        }
        if(customWeekTime.getTue() != -1L) {
            if(notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.tuesday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getTue()));
            notFirst = true;
        }
        if(customWeekTime.getWed() != -1L) {
            if(notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.wednesday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getWed()));
            notFirst = true;
        }
        if(customWeekTime.getThu() != -1L) {
            if(notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.thursday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getThu()));
            notFirst = true;
        }
        if(customWeekTime.getFri() != -1L) {
            if(notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.friday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getFri()));
            notFirst = true;
        }
        if(customWeekTime.getSat() != -1L) {
            if(notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.saturday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getSat()));
            notFirst = true;
        }
        if(customWeekTime.getSun() != -1L) {
            if(notFirst) {
                s.append(System.getProperty("line.separator"));
            }
            s.append(getString(R.string.sunday));
            s.append(": ");
            s.append(DateConverter.durationConverterFromLongToString(customWeekTime.getSun()));
        }
        return s.toString();
    }

    @ColorInt
    int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}