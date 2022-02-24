package hu.janny.tomsschedule.ui.main.statistics;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;

import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentGlobalFilterBinding;
import hu.janny.tomsschedule.databinding.FragmentPersonalFilterBinding;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.DateConverter;
import hu.janny.tomsschedule.viewmodel.GlobalStatisticsViewModel;
import hu.janny.tomsschedule.viewmodel.StatisticsViewModel;

public class GlobalFilterFragment extends Fragment {

    private FragmentGlobalFilterBinding binding;
    private GlobalStatisticsViewModel viewModel;

    final Calendar calDay = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(GlobalStatisticsViewModel.class);
        binding = FragmentGlobalFilterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initCalendars();
        initPeriodGroup();
        initFilterButton(root);

        return root;
    }

    private void initPeriodGroup() {
        binding.gPeriodChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if(binding.gCustomDayChip.isChecked()) {
                    binding.gCustomDay.setVisibility(View.VISIBLE);
                } else {
                    binding.gCustomDay.setVisibility(View.GONE);
                }
                if(binding.gCustomDayChip.isChecked() || binding.gYesterdayChip.isChecked()) {
                    hideChips();
                } else {
                    showChips();
                }
            }
        });
    }

    private void fixActivitySpinnerListener() {
        binding.gSelectActivitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initFilterButton(View fragView) {
        binding.gFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGender();
                getAgeGroup();
                getData();
                Navigation.findNavController(fragView).popBackStack();
            }
        });
    }

    private void getData() {
        String activity = CustomActivityHelper.getSelectedFixActivityName(binding.gSelectActivitySpinner.getSelectedItem().toString().trim());
        viewModel.setName(activity);
        if(binding.gYesterdayChip.isChecked()) {
            viewModel.setFrom(0L);
            viewModel.setTo(CustomActivityHelper.minusDaysMillis(1));
            viewModel.findYesterdayData(activity);
        } else if(binding.gCustomDayChip.isChecked()) {
            viewModel.setFrom(0L);
            viewModel.setTo(calDay.getTimeInMillis());
            viewModel.findExactDayData(activity, calDay.getTimeInMillis());
        } else if(binding.gWeekChip.isChecked()) {
            viewModel.setFrom(CustomActivityHelper.minusWeekMillis(1));
            viewModel.setTo(CustomActivityHelper.todayMillis());
            viewModel.findActivity(activity);
        } else {
            viewModel.setFrom(CustomActivityHelper.minusMonthMillis(1));
            viewModel.setTo(CustomActivityHelper.todayMillis());
            viewModel.findActivity(activity);
        }
    }

    private void getGender() {
        if(binding.gBothGenderChip.isChecked()) {
            viewModel.setGender(0);
        } else if(binding.gFemaleChip.isChecked()) {
            viewModel.setGender(2);
        } else {
            viewModel.setGender(1);
        }
    }

    private void getAgeGroup() {
        if(binding.gAgeGroupAllChip.isChecked()) {
            viewModel.setAgeGroup(-1);
        } else if(binding.gAgeGroup1.isChecked()) {
            viewModel.setAgeGroup(0);
        } else if(binding.gAgeGroup2.isChecked()) {
            viewModel.setAgeGroup(1);
        } else if(binding.gAgeGroup3.isChecked()) {
            viewModel.setAgeGroup(2);
        } else if(binding.gAgeGroup4.isChecked()) {
            viewModel.setAgeGroup(3);
        } else if(binding.gAgeGroup5.isChecked()) {
            viewModel.setAgeGroup(4);
        } else if(binding.gAgeGroup6.isChecked()) {
            viewModel.setAgeGroup(5);
        }
    }

    private void hideChips() {
        binding.gFemaleChip.setVisibility(View.GONE);
        binding.gMaleChip.setVisibility(View.GONE);
        binding.gBothGenderChip.setChecked(true);
        binding.gAgeGroup1.setVisibility(View.GONE);
        binding.gAgeGroup2.setVisibility(View.GONE);
        binding.gAgeGroup3.setVisibility(View.GONE);
        binding.gAgeGroup4.setVisibility(View.GONE);
        binding.gAgeGroup5.setVisibility(View.GONE);
        binding.gAgeGroup6.setVisibility(View.GONE);
        binding.gAgeGroupAllChip.setChecked(true);
    }

    private void showChips() {
        binding.gFemaleChip.setVisibility(View.VISIBLE);
        binding.gMaleChip.setVisibility(View.VISIBLE);
        binding.gBothGenderChip.setChecked(true);
        binding.gAgeGroup1.setVisibility(View.VISIBLE);
        binding.gAgeGroup2.setVisibility(View.VISIBLE);
        binding.gAgeGroup3.setVisibility(View.VISIBLE);
        binding.gAgeGroup4.setVisibility(View.VISIBLE);
        binding.gAgeGroup5.setVisibility(View.VISIBLE);
        binding.gAgeGroup6.setVisibility(View.VISIBLE);
        binding.gAgeGroupAllChip.setChecked(true);
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

        binding.gCustomDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateDay,calDay.get(Calendar.YEAR),calDay.get(Calendar.MONTH),calDay.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    private void updateLabelStartDay(){
        binding.gCustomDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calDay.get(Calendar.DATE), calDay.get(Calendar.MONTH) + 1, calDay.get(Calendar.YEAR)));
    }
}