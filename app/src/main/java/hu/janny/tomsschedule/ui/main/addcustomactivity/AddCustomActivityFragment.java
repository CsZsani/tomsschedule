package hu.janny.tomsschedule.ui.main.addcustomactivity;

import androidx.lifecycle.ViewModelProvider;

import androidx.appcompat.app.AlertDialog;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentAddCustomActivityBinding;
import hu.janny.tomsschedule.databinding.FragmentHomeBinding;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.DateConverter;
import hu.janny.tomsschedule.ui.main.MainViewModel;

public class AddCustomActivityFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private AddCustomActivityViewModel mViewModel;
    private MainViewModel mainViewModel;
    private FragmentAddCustomActivityBinding binding;
    private AlertDialog colorPickerDialog;
    final Calendar calDeadline= Calendar.getInstance();
    final Calendar calStartDay= Calendar.getInstance();
    final Calendar calEndDay= Calendar.getInstance();
    final Calendar calEndDate= Calendar.getInstance();
    private CustomActivity customActivity;

    public static AddCustomActivityFragment newInstance() {
        return new AddCustomActivityFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddCustomActivityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //return inflater.inflate(R.layout.fragment_add_custom_activity, container, false);
        intiColorPicker();
        binding.activityPriority.setOnItemSelectedListener(this);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding.activityColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPickerDialog.show();
            }
        });


        customActivity = new CustomActivity();

        initCalendars();
        initOnClickListenersOnRadioGroups();
        return root;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AddCustomActivityViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void intiColorPicker() {
        colorPickerDialog = new ColorPickerDialog.Builder(getActivity())
                .setTitle(R.string.nav_header_title)
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.confirm),
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                binding.activityColor.setBackgroundColor(envelope.getColor());
                            }
                        })
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .attachAlphaSlideBar(false)
                .attachBrightnessSlideBar(true)
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .create();
    }


    private void initOnClickListenersOnRadioGroups() {
        binding.selectActivityNameRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.selectFixActivityOption:
                        binding.selectFixActivitySpinner.setVisibility(View.VISIBLE);
                        binding.activityName.setVisibility(View.GONE);
                        break;
                    case R.id.selectCustomActivityOption:
                        binding.selectFixActivitySpinner.setVisibility(View.GONE);
                        binding.activityName.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        binding.selectNotifTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.activityHasDeadline:
                        setDeadlineVisible();
                        setIntervalGone();
                        setRegularityGone();
                        setEndDateGone();
                        break;
                    case R.id.activityRegularity:
                        setIntervalGone();
                        setDeadlineGone();
                        setEndDateGone();
                        binding.activityRegularityTypeText.setVisibility(View.VISIBLE);
                        binding.selectExactRegularity.setVisibility(View.VISIBLE);
                        break;
                    case R.id.activityIsInterval:
                        setIntervalVisible();
                        setDeadlineGone();
                        setEndDateGone();
                        setRegularityGone();
                        break;
                    case R.id.activityCustom:
                        setIntervalGone();
                        setDeadlineGone();
                        setEndDateGone();
                        setRegularityGone();
                        break;
                }
            }
        });

        binding.selectExactRegularity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.activityDaily:
                        setWeeklyStuffGone();
                        setEndDateGone();
                        binding.activityIsTimeMeasured.setChecked(false);
                        break;
                    case R.id.activityWeekly:
                        binding.activityHasFixedWeeks.setVisibility(View.VISIBLE);
                        binding.activityHasAnEndDate.setVisibility(View.VISIBLE);
                        binding.activityIsTimeMeasured.setChecked(false);
                        break;
                    case R.id.activityMonthly:
                        setWeeklyStuffGone();
                        binding.activityHasAnEndDate.setVisibility(View.VISIBLE);
                        binding.activityIsTimeMeasured.setChecked(false);
                        break;
                }
            }
        });

        binding.activityHasFixedWeeks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CompoundButton) view).isChecked()) {
                    binding.activityWeeklyDays.setVisibility(View.VISIBLE);
                    binding.allDaysOfWeek.setVisibility(View.VISIBLE);
                    binding.activityHasAnEndDate.setChecked(false);
                } else {
                    binding.activityWeeklyDays.setVisibility(View.GONE);
                    binding.allDaysOfWeek.setVisibility(View.GONE);
                    binding.activityHasAnEndDate.setChecked(false);
                }
            }
        });

        binding.activityHasAnEndDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    binding.activityEndDate.setVisibility(View.VISIBLE);
                } else {
                    binding.activityEndDate.setVisibility(View.GONE);
                }
            }
        });

        binding.activityIsTimeMeasured.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if(binding.activityHasDeadline.isChecked()) {
                        binding.durationText.setText(R.string.choose_sum_time_for_deadline);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
                    } else if(binding.activityCustom.isChecked()) {
                        binding.durationText.setText(R.string.choose_sum_time_for_neither);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
                    } else if(binding.activityIsInterval.isChecked()) {
                        binding.selectDurationType.setVisibility(View.VISIBLE);
                        binding.activityIsSumTime.setVisibility(View.VISIBLE);
                        binding.activityIsTime.setVisibility(View.VISIBLE);
                        binding.activityCustomTime.setVisibility(View.GONE);
                        binding.activityIsWeeklyTime.setVisibility(View.GONE);
                    } else if(binding.activityDaily.isChecked()) {
                        binding.durationText.setText(R.string.choose_monthly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
                        binding.activitySumTimePicker.days.setClickable(false);
                        binding.activitySumTimePicker.days.setBackgroundColor(Color.LTGRAY);
                    } else if(binding.activityWeekly.isChecked()) {
                        if (binding.activityHasFixedWeeks.isChecked()) {
                            // sok minden
                        } else {
                            binding.durationText.setText(R.string.choose_one_weekly_time);
                            binding.durationText.setVisibility(View.VISIBLE);
                            setSumTimePickerDefault();
                        }
                    } else if(binding.activityMonthly.isChecked()) {
                        binding.durationText.setText(R.string.choose_monthly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
                    }
                } else {
                    binding.durationText.setVisibility(View.GONE);
                    binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
                    binding.selectDurationType.setVisibility(View.GONE);
                }
            }
        });
    }

    private  void setSumTimePickerDefault() {
        binding.activitySumTimePicker.getRoot().setVisibility(View.VISIBLE);
        binding.activitySumTimePicker.days.setText("0");
        binding.activitySumTimePicker.hours.setText("0");
        binding.activitySumTimePicker.minutes.setText("0");
        binding.activitySumTimePicker.days.setClickable(true);
        binding.activitySumTimePicker.days.setBackgroundColor(Color.WHITE);
    }

    private void initCalendars() {
        DatePickerDialog.OnDateSetListener dateDeadline = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calDeadline.set(Calendar.YEAR, year);
                calDeadline.set(Calendar.MONTH,month);
                calDeadline.set(Calendar.DAY_OF_MONTH,day);
                updateLabelDeadline();
            }
        };

        DatePickerDialog.OnDateSetListener dateStartday = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calStartDay.set(Calendar.YEAR, year);
                calStartDay.set(Calendar.MONTH,month);
                calStartDay.set(Calendar.DAY_OF_MONTH,day);
                updateLabelStartDay();
            }
        };

        DatePickerDialog.OnDateSetListener dateEndDay = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calEndDay.set(Calendar.YEAR, year);
                calEndDay.set(Calendar.MONTH,month);
                calEndDay.set(Calendar.DAY_OF_MONTH,day);
                updateLabelEndDay();
            }
        };

        DatePickerDialog.OnDateSetListener dateEndDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calEndDate.set(Calendar.YEAR, year);
                calEndDate.set(Calendar.MONTH,month);
                calEndDate.set(Calendar.DAY_OF_MONTH,day);
                updateLabelEndDate();
            }
        };

        binding.activityDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateDeadline,calDeadline.get(Calendar.YEAR),calDeadline.get(Calendar.MONTH),calDeadline.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        binding.activityStartDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateStartday,calStartDay.get(Calendar.YEAR),calStartDay.get(Calendar.MONTH),calStartDay.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        binding.activityEndDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateEndDay,calEndDay.get(Calendar.YEAR),calEndDay.get(Calendar.MONTH),calEndDay.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        binding.activityEndDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateEndDate,calEndDate.get(Calendar.YEAR),calEndDate.get(Calendar.MONTH),calEndDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabelDeadline(){
        binding.activityDeadline.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calDeadline.get(Calendar.DATE), calDeadline.get(Calendar.MONTH) + 1, calDeadline.get(Calendar.YEAR)));
    }

    private void updateLabelStartDay(){
        binding.activityStartDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calStartDay.get(Calendar.DATE), calStartDay.get(Calendar.MONTH) + 1, calStartDay.get(Calendar.YEAR)));
    }

    private void updateLabelEndDay(){
        binding.activityEndDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calEndDay.get(Calendar.DATE), calEndDay.get(Calendar.MONTH) + 1, calEndDay.get(Calendar.YEAR)));
    }

    private void updateLabelEndDate(){
        binding.activityEndDate.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calEndDate.get(Calendar.DATE), calEndDate.get(Calendar.MONTH) + 1, calEndDate.get(Calendar.YEAR)));
    }

    private void setDeadlineGone() {
        binding.activityDeadlineText.setVisibility(View.GONE);
        binding.activityDeadline.setVisibility(View.GONE);
    }

    private void setDeadlineVisible() {
        binding.activityDeadlineText.setVisibility(View.VISIBLE);
        binding.activityDeadline.setVisibility(View.VISIBLE);
    }

    private void setIntervalGone() {
        binding.startDayText.setVisibility(View.GONE);
        binding.activityStartDay.setVisibility(View.GONE);
        binding.endDayText.setVisibility(View.GONE);
        binding.activityEndDay.setVisibility(View.GONE);
    }

    private void setIntervalVisible() {
        binding.startDayText.setVisibility(View.VISIBLE);
        binding.activityStartDay.setVisibility(View.VISIBLE);
        binding.endDayText.setVisibility(View.VISIBLE);
        binding.activityEndDay.setVisibility(View.VISIBLE);
    }

    private void setRegularityGone() {
        binding.activityRegularityTypeText.setVisibility(View.GONE);
        binding.selectExactRegularity.setVisibility(View.GONE);
        binding.activityHasFixedWeeks.setVisibility(View.GONE);
        binding.activityWeeklyDays.setVisibility(View.GONE);
        binding.allDaysOfWeek.setVisibility(View.GONE);
    }

    private void setEndDateGone() {
        binding.activityHasAnEndDate.setChecked(false);
        binding.activityHasAnEndDate.setVisibility(View.GONE);
        binding.activityEndDate.setVisibility(View.GONE);
    }

    private void setWeeklyStuffGone() {
        binding.activityHasFixedWeeks.setVisibility(View.GONE);
        binding.activityWeeklyDays.setVisibility(View.GONE);
        binding.allDaysOfWeek.setVisibility(View.GONE);
    }

}