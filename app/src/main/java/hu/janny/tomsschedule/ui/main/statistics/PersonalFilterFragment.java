package hu.janny.tomsschedule.ui.main.statistics;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;

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
import hu.janny.tomsschedule.model.ActivityFilter;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.DateConverter;
import hu.janny.tomsschedule.viewmodel.StatisticsViewModel;

public class PersonalFilterFragment extends Fragment {

    private FragmentPersonalFilterBinding binding;
    private StatisticsViewModel statisticsViewModel;
    private List<ActivityFilter> activityFilters;

    final Calendar calDay = Calendar.getInstance();
    final Calendar calFrom = Calendar.getInstance();
    final Calendar calTo = Calendar.getInstance();

    private int periodType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);
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
        initFilterButton();

        /*Bundle result = new Bundle();
        result.putString("bundleKey", "result");
        getParentFragmentManager().setFragmentResult("requestKey", result);*/

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
            //chip.setId(ViewCompat.generateViewId());
            chip.setText(af.name);
            chip.setChipBackgroundColor(ColorStateList.valueOf(af.color));
            chip.setCheckable(true);
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

    private void initFilterButton() {
        binding.pFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.pAllActivityChip.isChecked()) {
                    filterAll();
                } else {
                    filterActivities();
                }
            }
        });
    }

    private void filterAll() {
        if(binding.pTodayChip.isChecked()) {
            statisticsViewModel.filterExactDay(CustomActivityHelper.todayMillis(), new ArrayList<>());
            periodType = 0;
        } else if(binding.pYesterdayChip.isChecked()) {
            statisticsViewModel.filterExactDay(CustomActivityHelper.yesterdayMillis(), new ArrayList<>());
            periodType = 1;
        }
    }

    private void filterActivities() {

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
}