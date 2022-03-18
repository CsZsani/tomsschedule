package hu.janny.tomsschedule.ui.main.statistics;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentPersonalFilterBinding;
import hu.janny.tomsschedule.model.helper.ActivityFilter;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.viewmodel.StatisticsViewModel;

/**
 * This fragment is for filtering the personal statistics before displaying the data.
 */
public class PersonalFilterFragment extends Fragment {

    private FragmentPersonalFilterBinding binding;
    private StatisticsViewModel statisticsViewModel;

    private List<ActivityFilter> activityFilters;

    // LocalDates for the date pickers
    private LocalDate ldCustom;
    private LocalDate ldFrom;
    private LocalDate ldTo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Binds layout
        binding = FragmentPersonalFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets a StatisticsViewModel instance
        statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        initPeriodChipGroup();
        initCalendars();

        // Observer of activities of the user to choose from these activities
        statisticsViewModel.getFilterActivities().observe(getViewLifecycleOwner(), new Observer<List<ActivityFilter>>() {
            @Override
            public void onChanged(List<ActivityFilter> activityFilters) {
                PersonalFilterFragment.this.activityFilters = activityFilters;
                initActivitiesChips(activityFilters);
            }
        });

        initActivityChipGroup();
        initFilterButton(view);

    }

    /**
     * Initializes activity chip group. Adds on checked change listener to all activity chip. If all activity chip is
     * selected, then we uncheck all the other chips.
     */
    private void initActivityChipGroup() {
        binding.pAllActivityChip.setChecked(true);
        binding.pAllActivityChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (binding.pAllActivityChip.isChecked()) {
                    int chipsCount = binding.pActivityChipGroup.getChildCount();
                    for (int i = 1; i < chipsCount; i++) {
                        Chip chip = (Chip) binding.pActivityChipGroup.getChildAt(i);
                        chip.setChecked(false);
                    }
                }
            }
        });
    }

    /**
     * It programmatically adds the activities to activity chip group.
     *
     * @param activityFilters the list of activities to filter
     */
    private void initActivitiesChips(List<ActivityFilter> activityFilters) {
        for (ActivityFilter af : activityFilters) {
            addChipToActivityGroup(af);
        }
    }

    /**
     * It adds an activity to activity chip group to filter.
     *
     * @param af the activity to filter
     */
    private void addChipToActivityGroup(ActivityFilter af) {
        if (getContext() != null) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_layout, binding.pActivityChipGroup, false);
            chip.setId(ViewCompat.generateViewId());
            chip.setText(af.name);
            chip.setChipBackgroundColor(ColorStateList.valueOf(af.color));
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        // If we check a chip which is not all, then we set all chip to unchecked
                        if (binding.pAllActivityChip.isChecked()) {
                            binding.pAllActivityChip.setChecked(false);
                        }
                    }
                }
            });
            binding.pActivityChipGroup.addView(chip);
        }
        binding.pActivityChipGroup.getChildAt(0);
    }

    /**
     * Initializes the time period chip group.
     */
    private void initPeriodChipGroup() {
        binding.pPeriodChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                // Changes the UI if we want add custom day or interval to display the data
                if (binding.pCustomDayChip.isChecked()) {
                    binding.pCustomDay.setVisibility(View.VISIBLE);
                    binding.pFromText.setVisibility(View.GONE);
                    binding.pFromDay.setVisibility(View.GONE);
                    binding.pToText.setVisibility(View.GONE);
                    binding.pToDay.setVisibility(View.GONE);
                } else if (binding.pFromToChip.isChecked()) {
                    binding.pCustomDay.setVisibility(View.GONE);
                    binding.pFromText.setVisibility(View.VISIBLE);
                    binding.pFromDay.setVisibility(View.VISIBLE);
                    binding.pToText.setVisibility(View.VISIBLE);
                    binding.pToDay.setVisibility(View.VISIBLE);
                } else {
                    binding.pCustomDay.setVisibility(View.GONE);
                    binding.pFromText.setVisibility(View.GONE);
                    binding.pFromDay.setVisibility(View.GONE);
                    binding.pToText.setVisibility(View.GONE);
                    binding.pToDay.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Initializes the click listener of the filter button.
     *
     * @param fragView the root view of fragment
     */
    private void initFilterButton(View fragView) {
        binding.pFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.pAllActivityChip.isChecked()) {
                    filterAll(fragView);
                } else {
                    filterActivities(fragView);
                }
            }
        });
    }

    /**
     * Sends filtering to view model. This method used when want the data of all activities.
     *
     * @param fragView the root view of fragment
     */
    private void filterAll(View fragView) {
        // We make lists of the parameters of all activities. It used by the fragment which shows charts based on the data
        List<Long> list = new ArrayList<>();
        List<Integer> colList = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < activityFilters.size(); i++) {
            list.add(activityFilters.get(i).activityId);
            colList.add(activityFilters.get(i).color);
            names.add(activityFilters.get(i).name);
        }
        // 0 means that we will display the data of all activities
        statisticsViewModel.setpActivityNum(0);
        statisticsViewModel.setActsList(list);
        statisticsViewModel.setColors(colList);
        statisticsViewModel.setNames(names);

        if (!sendRequestToDb(list)) {
            return;
        }
        Navigation.findNavController(fragView).popBackStack();

    }

    /**
     * Sends filtering to view model. This method used when want the data of one or more activities.
     *
     * @param fragView the root view of fragment
     */
    private void filterActivities(View fragView) {
        // We make lists of the parameters of the selected activities. It used by the fragment which shows charts based on the da
        List<Long> list = new ArrayList<>();
        List<Integer> colList = new ArrayList<>();
        List<String> names = new ArrayList<>();
        int chipsCount = binding.pActivityChipGroup.getChildCount();
        for (int i = 1; i < chipsCount; i++) {
            Chip chip = (Chip) binding.pActivityChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                list.add(activityFilters.get(i - 1).activityId);
                colList.add(activityFilters.get(i - 1).color);
                names.add(activityFilters.get(i - 1).name);
            }
        }
        // Looking for errors
        if (list.size() == 0) {
            Toast.makeText(getActivity(), getString(R.string.must_choose_one_error), Toast.LENGTH_LONG).show();
            return;
        }
        if (list.size() == 1 && (binding.pTodayChip.isChecked() || binding.pYesterdayChip.isChecked() || binding.pWeekChip.isChecked())) {
            Toast.makeText(getActivity(), getString(R.string.one_act_longer_period_error), Toast.LENGTH_LONG).show();
            return;
        }
        statisticsViewModel.setpActivityNum(list.size());
        statisticsViewModel.setActsList(list);
        statisticsViewModel.setColors(colList);
        statisticsViewModel.setNames(names);

        if (!sendRequestToDb(list)) {
            return;
        }
        Navigation.findNavController(fragView).popBackStack();
    }

    private boolean sendRequestToDb(List<Long> list) {
        if (binding.pYesterdayChip.isChecked()) {
            // Yesterday
            statisticsViewModel.setpPeriodType(1);
            statisticsViewModel.setFromTime(0L);
            statisticsViewModel.setToTime(CustomActivityHelper.minusDaysMillis(1));
            statisticsViewModel.filterExactDay(CustomActivityHelper.minusDaysMillis(1), list);
        } else if (binding.pWeekChip.isChecked()) {
            // One week
            statisticsViewModel.setpPeriodType(2);
            statisticsViewModel.setFromTime(CustomActivityHelper.minusWeekMillis(1));
            statisticsViewModel.setToTime(CustomActivityHelper.todayMillis());
            statisticsViewModel.filterFrom(CustomActivityHelper.minusWeekMillis(1), list);
        } else if (binding.pTwoWeeksChip.isChecked()) {
            // Two weeks
            statisticsViewModel.setpPeriodType(3);
            statisticsViewModel.setFromTime(CustomActivityHelper.minusWeekMillis(2));
            statisticsViewModel.setToTime(CustomActivityHelper.todayMillis());
            statisticsViewModel.filterFrom(CustomActivityHelper.minusWeekMillis(2), list);
        } else if (binding.pMonthChip.isChecked()) {
            // One month
            statisticsViewModel.setpPeriodType(4);
            statisticsViewModel.setFromTime(CustomActivityHelper.minusMonthMillis(1));
            statisticsViewModel.setToTime(CustomActivityHelper.todayMillis());
            statisticsViewModel.filterFrom(CustomActivityHelper.minusMonthMillis(1), list);
        } else if (binding.pThreeMonthsChip.isChecked()) {
            // Three months
            statisticsViewModel.setpPeriodType(5);
            statisticsViewModel.setFromTime(CustomActivityHelper.minusMonthMillis(3));
            statisticsViewModel.setToTime(CustomActivityHelper.todayMillis());
            statisticsViewModel.filterFrom(CustomActivityHelper.minusMonthMillis(3), list);
        } else if (binding.pCustomDayChip.isChecked()) {
            // A custom day
            statisticsViewModel.setpPeriodType(6);
            statisticsViewModel.setFromTime(0L);
            Instant custom = ldCustom.atStartOfDay(ZoneId.systemDefault()).toInstant();
            statisticsViewModel.setToTime(custom.toEpochMilli());
            statisticsViewModel.filterExactDay(custom.toEpochMilli(), list);
        } else if (binding.pFromToChip.isChecked()) {
            // An interval
            statisticsViewModel.setpPeriodType(7);
            Instant from = ldFrom.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant to = ldTo.atStartOfDay(ZoneId.systemDefault()).toInstant();
            // Checks if the interval is less than one day
            if (from.toEpochMilli() > to.toEpochMilli()) {
                Toast.makeText(getContext(), getString(R.string.filter_from_to_error), Toast.LENGTH_LONG).show();
                return false;
            }
            statisticsViewModel.setFromTime(from.toEpochMilli());
            statisticsViewModel.setToTime(to.toEpochMilli());
            statisticsViewModel.filterFromTo(from.toEpochMilli(), to.toEpochMilli(), list);
        } else {
            // Today - which is the default as well
            statisticsViewModel.setpPeriodType(0);
            statisticsViewModel.setFromTime(0L);
            statisticsViewModel.setToTime(CustomActivityHelper.todayMillis());
            statisticsViewModel.filterExactDay(CustomActivityHelper.todayMillis(), list);
        }
        return true;
    }

    /**
     * Initializes date picker dialogs and set on click listeners to these date picker dialogs.
     */
    private void initCalendars() {
        // Initializes date picker dialogs' onDateSetListener and after that it sets the UI according to
        // the new date
        DatePickerDialog.OnDateSetListener dateCustomDay = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month++;
                ldCustom = LocalDate.of(year, month, day);
                updateLabelCustomDay();
            }
        };

        DatePickerDialog.OnDateSetListener dateFromDay = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month++;
                ldFrom = LocalDate.of(year, month, day);
                updateLabelStartDay();
            }
        };

        DatePickerDialog.OnDateSetListener dateToDay = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month++;
                ldTo = LocalDate.of(year, month, day);
                updateLabelEndDay();
            }
        };

        // Sets onClickListeners for date pickers
        binding.pCustomDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), dateCustomDay, ldCustom.getYear(), ldCustom.getMonthValue() - 1, ldCustom.getDayOfMonth()).show();
            }
        });
        binding.pFromDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), dateFromDay, ldFrom.getYear(), ldFrom.getMonthValue() - 1, ldFrom.getDayOfMonth()).show();
            }
        });
        binding.pToDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), dateToDay, ldTo.getYear(), ldTo.getMonthValue() - 1, ldTo.getDayOfMonth()).show();
            }
        });
        initTodayDate();
    }

    /**
     * Initializes start day picker dialog for today date and shows on the UI as well.
     */
    private void initTodayDate() {
        ldCustom = LocalDate.now();
        updateLabelCustomDay();
        ldFrom = LocalDate.now();
        updateLabelStartDay();
        ldTo = LocalDate.now();
        updateLabelEndDay();
    }

    /**
     * Displays the date of custom day.
     */
    private void updateLabelCustomDay() {
        binding.pCustomDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                ldCustom.getYear(), ldCustom.getMonthValue(), ldCustom.getDayOfMonth()));
    }

    /**
     * Displays the date of start day.
     */
    private void updateLabelStartDay() {
        binding.pFromDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                ldFrom.getYear(), ldFrom.getMonthValue(), ldFrom.getDayOfMonth()));
    }

    /**
     * Displays the date of end day.
     */
    private void updateLabelEndDay() {
        binding.pToDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                ldTo.getYear(), ldTo.getMonthValue(), ldTo.getDayOfMonth()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}