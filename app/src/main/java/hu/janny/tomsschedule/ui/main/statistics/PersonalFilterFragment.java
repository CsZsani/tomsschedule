package hu.janny.tomsschedule.ui.main.statistics;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentPersonalFilterBinding;
import hu.janny.tomsschedule.model.helper.ActivityFilter;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.viewmodel.StatisticsViewModel;

public class PersonalFilterFragment extends Fragment {

    private FragmentPersonalFilterBinding binding;
    private StatisticsViewModel statisticsViewModel;
    private List<ActivityFilter> activityFilters;

    final Calendar calDay = Calendar.getInstance();
    final Calendar calFrom = Calendar.getInstance();
    final Calendar calTo = Calendar.getInstance();

    private int periodType;
    private int activityNum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        statisticsViewModel =
                new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);
        binding = FragmentPersonalFilterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initPeriodChipGroup();
        initCalendars();

        statisticsViewModel.getFilterActivities().observe(getViewLifecycleOwner(), new Observer<List<ActivityFilter>>() {
            @Override
            public void onChanged(List<ActivityFilter> activityFilters) {
                PersonalFilterFragment.this.activityFilters = activityFilters;
                initActivitiesChips(activityFilters);
            }
        });

        initActivityChipGroup();
        initFilterButton(root);

        return root;
    }

    private void initActivityChipGroup() {
        binding.pAllActivityChip.setChecked(true);
        binding.pAllActivityChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(binding.pAllActivityChip.isChecked()) {
                    int chipsCount = binding.pActivityChipGroup.getChildCount();
                    for(int i = 1; i<chipsCount; i++) {
                        Chip chip = (Chip) binding.pActivityChipGroup.getChildAt(i);
                        chip.setChecked(false);
                    }
                }
            }
        });
    }

    private void initActivitiesChips(List<ActivityFilter> activityFilters) {
        for(ActivityFilter af : activityFilters) {
            addChipToActivityGroup(af);
        }
    }

    private void addChipToActivityGroup(ActivityFilter af) {
        if(getContext() != null) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_layout, binding.pActivityChipGroup, false);
            chip.setId(ViewCompat.generateViewId());
            chip.setText(af.name);
            chip.setChipBackgroundColor(ColorStateList.valueOf(af.color));
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        if(binding.pAllActivityChip.isChecked()) {
                            binding.pAllActivityChip.setChecked(false);
                        }
                    }
                }
            });
            //chip.setTextColor(getResources().getColor(R.color.black));
            binding.pActivityChipGroup.addView(chip);
        }
        binding.pActivityChipGroup.getChildAt(0);
    }

    private void initPeriodChipGroup() {
        binding.pPeriodChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {

                if(binding.pCustomDayChip.isChecked()) {
                    binding.pCustomDay.setVisibility(View.VISIBLE);
                    binding.pFromText.setVisibility(View.GONE);
                    binding.pFromDay.setVisibility(View.GONE);
                    binding.pToText.setVisibility(View.GONE);
                    binding.pToDay.setVisibility(View.GONE);
                } else if(binding.pFromToChip.isChecked()) {
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

    private void initFilterButton(View fragView) {
        binding.pFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.pAllActivityChip.isChecked()) {
                    filterAll(fragView);
                } else {
                    filterActivities(fragView);
                }
            }
        });
    }

    private void filterAll(View fragView) {
        activityNum = 0;
        statisticsViewModel.setpActivityNum(0);
        statisticsViewModel.setActsList(new ArrayList<>());
        List<Long> list = new ArrayList<>();
        List<Integer> colList = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for(int i = 0; i<activityFilters.size(); i++) {
            list.add(activityFilters.get(i).activityId);
            colList.add(activityFilters.get(i).color);
            names.add(activityFilters.get(i).name);
        }
        statisticsViewModel.setActsList(list);
        statisticsViewModel.setColors(colList);
        statisticsViewModel.setNames(names);
        sendRequestToDb(new ArrayList<>());
        //sendData(new ArrayList<>());
        Navigation.findNavController(fragView).popBackStack();

    }

    private void filterActivities(View fragView) {
        List<Long> list = new ArrayList<>();
        List<Integer> colList = new ArrayList<>();
        List<String> names = new ArrayList<>();
        int chipsCount = binding.pActivityChipGroup.getChildCount();
        for(int i = 1; i<chipsCount; i++) {
            Chip chip = (Chip) binding.pActivityChipGroup.getChildAt(i);
            if(chip.isChecked()) {
                list.add(activityFilters.get(i-1).activityId);
                colList.add(activityFilters.get(i-1).color);
                names.add(activityFilters.get(i-1).name);
            }
        }
        if(list.size() == 0) {
            Toast.makeText(getActivity(), "You must choose All or at least one activity!", Toast.LENGTH_LONG).show();
            return;
        }
        if(list.size() == 1 && (binding.pTodayChip.isChecked() || binding.pYesterdayChip.isChecked() || binding.pWeekChip.isChecked())) {
            Toast.makeText(getActivity(), "For only one activity, you must choose longer period than one week or custom dates!", Toast.LENGTH_LONG).show();
            return;
        }
        activityNum = list.size();
        statisticsViewModel.setpActivityNum(list.size());
        statisticsViewModel.setActsList(list);
        statisticsViewModel.setColors(colList);
        statisticsViewModel.setNames(names);
        sendRequestToDb(list);
        //sendData(list);
        Navigation.findNavController(fragView).popBackStack();
    }

    private void sendRequestToDb(List<Long> list) {
        if(binding.pYesterdayChip.isChecked()) {
            statisticsViewModel.setpPeriodType(1);
            statisticsViewModel.setFromTime(0L);
            statisticsViewModel.setToTime(CustomActivityHelper.minusDaysMillis(1));
            statisticsViewModel.filterExactDay(CustomActivityHelper.minusDaysMillis(1), list);
            periodType = 1;
        } else if(binding.pWeekChip.isChecked()) {
            statisticsViewModel.setpPeriodType(2);
            statisticsViewModel.setFromTime(CustomActivityHelper.minusWeekMillis(1));
            statisticsViewModel.setToTime(CustomActivityHelper.todayMillis());
            statisticsViewModel.filterFrom(CustomActivityHelper.minusWeekMillis(1), list);
            periodType = 2;
        } else if(binding.pTwoWeeksChip.isChecked()) {
            statisticsViewModel.setpPeriodType(3);
            statisticsViewModel.setFromTime(CustomActivityHelper.minusWeekMillis(2));
            statisticsViewModel.setToTime(CustomActivityHelper.todayMillis());
            statisticsViewModel.filterFrom(CustomActivityHelper.minusWeekMillis(2), list);
            periodType = 3;
        } else if(binding.pMonthChip.isChecked()) {
            statisticsViewModel.setpPeriodType(4);
            statisticsViewModel.setFromTime(CustomActivityHelper.minusMonthMillis(1));
            statisticsViewModel.setToTime(CustomActivityHelper.todayMillis());
            statisticsViewModel.filterFrom(CustomActivityHelper.minusMonthMillis(1), list);
            periodType = 4;
        } else if(binding.pThreeMonthsChip.isChecked()) {
            statisticsViewModel.setpPeriodType(5);
            statisticsViewModel.setFromTime(CustomActivityHelper.minusMonthMillis(3));
            statisticsViewModel.setToTime(CustomActivityHelper.todayMillis());
            statisticsViewModel.filterFrom(CustomActivityHelper.minusMonthMillis(3), list);
            periodType = 5;
        } else if(binding.pCustomDayChip.isChecked()) {
            statisticsViewModel.setpPeriodType(6);
            statisticsViewModel.setFromTime(0L);
            statisticsViewModel.setToTime(calDay.getTimeInMillis());
            statisticsViewModel.filterExactDay(calDay.getTimeInMillis(), list);
            periodType = 6;
        } else if(binding.pFromToChip.isChecked()) {
            statisticsViewModel.setpPeriodType(7);
            statisticsViewModel.setFromTime(calFrom.getTimeInMillis());
            statisticsViewModel.setToTime(calTo.getTimeInMillis());
            statisticsViewModel.filterFromTo(calFrom.getTimeInMillis(), calTo.getTimeInMillis(), list);
            periodType = 7;
        } else {
            statisticsViewModel.setpPeriodType(0);
            statisticsViewModel.setFromTime(0L);
            statisticsViewModel.setToTime(CustomActivityHelper.todayMillis());
            statisticsViewModel.filterExactDay(CustomActivityHelper.todayMillis(), list);
            periodType = 0;
        }
    }

    private void sendData(List<Long> list) {
        ArrayList<String> stringList = new ArrayList<>();
        for(int i = 0; i<list.size(); i++) {
            stringList.add(String.valueOf(list.get(i)));
        }
        Bundle result = new Bundle();
        result.putInt(PersonalStatisticsFragment.PERIOD_TYPE, periodType);
        result.putInt(PersonalStatisticsFragment.ACTIVITY_NUM, list.size());
        result.putStringArrayList(PersonalStatisticsFragment.ACTIVITIES, stringList);
        System.out.println(periodType + " " + list.size() + " " + stringList + " filterrfrag");
        getParentFragmentManager().setFragmentResult(PersonalStatisticsFragment.REQUEST_KEY, result);
    }

    private void initCalendars() {

        DatePickerDialog.OnDateSetListener dateDay = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calDay.clear();
                calDay.set(year, month, day);
                updateLabelStartDay();
            }
        };

        DatePickerDialog.OnDateSetListener dateFrom = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calFrom.clear();
                calFrom.set(year, month, day);
                updateLabelEndDay();
            }
        };

        DatePickerDialog.OnDateSetListener dateTo = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calTo.clear();
                calTo.set(year, month, day);
                updateLabelEndDate();
            }
        };

        binding.pCustomDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateDay,calDay.get(Calendar.YEAR),calDay.get(Calendar.MONTH),calDay.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        binding.pFromDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateFrom,calFrom.get(Calendar.YEAR),calFrom.get(Calendar.MONTH),calFrom.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        binding.pToDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateTo,calTo.get(Calendar.YEAR),calTo.get(Calendar.MONTH),calTo.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    private void updateLabelStartDay(){
        binding.pCustomDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calDay.get(Calendar.DATE), calDay.get(Calendar.MONTH) + 1, calDay.get(Calendar.YEAR)));
    }

    private void updateLabelEndDay(){
        binding.pFromDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calFrom.get(Calendar.DATE), calFrom.get(Calendar.MONTH) + 1, calFrom.get(Calendar.YEAR)));
    }

    private void updateLabelEndDate(){
        binding.pToDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calTo.get(Calendar.DATE), calTo.get(Calendar.MONTH) + 1, calTo.get(Calendar.YEAR)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}