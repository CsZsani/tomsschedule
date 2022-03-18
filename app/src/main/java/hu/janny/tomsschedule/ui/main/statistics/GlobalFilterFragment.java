package hu.janny.tomsschedule.ui.main.statistics;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;

import com.google.android.material.chip.ChipGroup;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import hu.janny.tomsschedule.databinding.FragmentGlobalFilterBinding;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.viewmodel.GlobalStatisticsViewModel;

public class GlobalFilterFragment extends Fragment {

    private FragmentGlobalFilterBinding binding;
    private GlobalStatisticsViewModel viewModel;

    private LocalDate ldDay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Binds the layout
        binding = FragmentGlobalFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets a GlobalStatisticsViewModel instance
        viewModel = new ViewModelProvider(requireActivity()).get(GlobalStatisticsViewModel.class);

        fixActivitySpinnerListener();
        initCalendars();
        initPeriodGroup();
        initFilterButton(view);
    }

    /**
     * Initializes the time period chip group, sets its click listener.
     */
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

    /**
     * Initializes fix activity choosing spinner. Sets its onItemSelectedListener.
     */
    private void fixActivitySpinnerListener() {
        binding.gSelectActivitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {}

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    /**
     * Initializes the filter button. It gets the selected data and navigates back to global statistics fragment.
     * @param fragView the root view of fragment
     */
    private void initFilterButton(View fragView) {
        binding.gFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGender();
                getAgeGroup();
                getData();
                viewModel.setLoading(true);
                Navigation.findNavController(fragView).popBackStack();
            }
        });
    }

    /**
     * Searches for the data based on the UI selction.
     */
    private void getData() {
        String activity = CustomActivityHelper.getSelectedFixActivityName(binding.gSelectActivitySpinner.getSelectedItem().toString().trim());
        viewModel.setName(activity);
        if(binding.gYesterdayChip.isChecked()) {
            viewModel.setFrom(0L);
            viewModel.setTo(CustomActivityHelper.minusDaysMillis(1));
            viewModel.findYesterdayData(activity);
        } else if(binding.gCustomDayChip.isChecked()) {
            viewModel.setFrom(0L);
            Instant custom = ldDay.atStartOfDay(ZoneId.systemDefault()).toInstant();
            viewModel.setTo(custom.toEpochMilli());
            viewModel.findExactDayData(activity, custom.toEpochMilli());
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

    /**
     * Sets the gender in the view model based on the UI selection.
     */
    private void getGender() {
        if(binding.gBothGenderChip.isChecked()) {
            viewModel.setGender(0);
        } else if(binding.gFemaleChip.isChecked()) {
            viewModel.setGender(2);
        } else {
            viewModel.setGender(1);
        }
    }

    /**
     * Sets the age group in the view model based on the UI selection.
     */
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

    /**
     * Hides gender and age group chips. Sets their visibility to GONE.
     */
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

    /**
     * Shows gender and age group chips. Sets their visibility to VISIBLE.
     */
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

    /**
     * Initializes date picker dialog and set on click listener to this date picker dialog.
     */
    private void initCalendars() {

        DatePickerDialog.OnDateSetListener dateDay = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month++;
                ldDay = LocalDate.of(year, month, day);
                updateLabelCustomDay();
            }
        };

        binding.gCustomDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateDay,ldDay.getYear(), ldDay.getMonthValue() - 1, ldDay.getDayOfMonth()).show();
            }
        });

        ldDay = LocalDate.now();
        updateLabelCustomDay();
    }

    /**
     * Displays the date of custom day.
     */
    private void updateLabelCustomDay(){
        binding.gCustomDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                ldDay.getYear(), ldDay.getMonthValue(), ldDay.getDayOfMonth()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}