package hu.janny.tomsschedule.ui.main.editactivity;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.CustomTimePickerForOneDayBinding;
import hu.janny.tomsschedule.databinding.DetailFragmentBinding;
import hu.janny.tomsschedule.databinding.FragmentEditActivityBinding;
import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.DateConverter;
import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.ui.main.MainViewModel;
import hu.janny.tomsschedule.ui.main.details.DetailFragment;

public class EditActivityFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    public static final String ARG_ITEM_ID = "item_id";

    private MainViewModel mainViewModel;
    private FragmentEditActivityBinding binding;
    private CustomActivity customActivity;
    private AlertDialog colorPickerDialog;
    final Calendar calDeadline= Calendar.getInstance();
    final Calendar calStartDay= Calendar.getInstance();
    final Calendar calEndDay= Calendar.getInstance();
    final Calendar calEndDate= Calendar.getInstance();
    private long activityId;
    int color;//Color.rgb(255, 164, 119);
    private boolean setupFinished = false;

    public static EditActivityFragment newInstance() {
        return new EditActivityFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding = FragmentEditActivityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        intiColorPicker();
        prioritySpinnerListener();

        binding.activityColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPickerDialog.show();
            }
        });

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            long id = getArguments().getLong(ARG_ITEM_ID);
            mainViewModel.findActivityById(id);
        }

        mainViewModel.getSingleActivity().observe(getViewLifecycleOwner(), new Observer<CustomActivity>() {
            @Override
            public void onChanged(CustomActivity activity) {
                if(activity != null) {
                    customActivity = activity;
                    color = customActivity.getCol();
                    binding.activityColor.setBackgroundColor(color);
                    initNameChooser();

                    initNoteAndPriority();
                    initSelection();
                } else {
                    navigateBackHome(root);
                    Toast.makeText(getActivity(), "I can't find this activity!", Toast.LENGTH_LONG).show();
                }
            }
        });
        initCalendars();
        initNotifyTypeRadioButtonGroup();
        initRegularityTypeRadioButtonGroup();
        initHasFixedDaysSwitch();
        initHasEndDateSwitch();
        initIsTimeMeasuredSwitch();
        initSelectDurationTypeRadioGroups();
        initFixedDaysTimePickerListeners();
        saveOnClickListener(root);

        return root;
    }

    private void prioritySpinnerListener() {
        binding.activityPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void saveOnClickListener(View fragView) {
        binding.saveActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveActivity(fragView);
            }
        });
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

    private void saveActivity(View fragView) {
        String name = binding.activityName.getText().toString().trim();
        if(name.isEmpty()) {
            binding.activityName.setError("Name is required");
            binding.activityName.requestFocus();
            return;
        }
        if(!CustomActivityHelper.isFixActivity(name)) {
            customActivity.setName(name);
        }

        customActivity.setCol(color);
        customActivity.setNote(binding.activityNote.getText().toString().trim());
        customActivity.setPr(Integer.parseInt(binding.activityPriority.getSelectedItem().toString()));
        customActivity.setEverythingToDefault();
        decideWhichMainType(fragView);
    }

    private void decideWhichMainType(View fragView) {
        if(binding.activityIsInterval.isChecked()) {
            String from = binding.activityStartDay.getText().toString().trim();
            String to = binding.activityEndDay.getText().toString().trim();
            if(from.isEmpty()) {
                binding.activityStartDay.setError("Start day is required for interval option!");
                binding.activityStartDay.requestFocus();
                return;
            }
            if(to.isEmpty()) {
                binding.activityEndDay.setError("Start day is required for interval option!");
                binding.activityEndDay.requestFocus();
                return;
            }
            if(!setInterval(from, to)) {return;}
        } else if(binding.activityCustom.isChecked()) {
            if(!setNeither()) {return;}
        } else {
            if(!setRegularity()) {return;}
        }
        addActivityToDb(fragView);
    }

    private boolean setInterval(String from, String to) {
        customActivity.setsD(DateConverter.stringFromSimpleDateDialogToLongMillis(from));
        customActivity.seteD(DateConverter.stringFromSimpleDateDialogToLongMillis(to));
        customActivity.settN(6);
        if(binding.activityIsTimeMeasured.isChecked()) {
            if(!setIntervalMeasuredTime()) {return false;}
        }
        return true;
    }

    private boolean setIntervalMeasuredTime() {
        if(binding.activityIsSumTime.isChecked()) {
            customActivity.settT(1);
            customActivity.settN(7);
        } else {
            customActivity.settT(2);
            customActivity.settN(2);
        }
        return !setTimeFromSumTime();
    }

    /**
     * Sets time duration for activity.
     * @return true if something went wrong, there are empty fields, false if set time happened
     */
    private boolean setTimeFromSumTime() {
        String days = binding.activitySumTimePicker.days.getText().toString().trim();
        String hours = binding.activitySumTimePicker.hours.getText().toString().trim();
        String minutes = binding.activitySumTimePicker.minutes.getText().toString().trim();
        if(days.isEmpty() || hours.isEmpty() || minutes.isEmpty()) {
            binding.activitySumTimePicker.minutes.setError("You must add days, hours and minutes (however it can be 0)!");
            binding.activitySumTimePicker.minutes.requestFocus();
            return true;
        }
        int day = 0;
        int hour = 0;
        int minute = 0;
        try {
            day = Integer.parseInt(days);
            hour = Integer.parseInt(hours);
            minute = Integer.parseInt(minutes);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return true;
        }
        if(day < 0 || hour > 23 || hour < 0 || minute > 59 || minute < 0) {
            binding.activitySumTimePicker.minutes.setError("Hours must be whole number between 0-23, minutes 0-59 and days >=0!");
            binding.activitySumTimePicker.minutes.requestFocus();
            return true;
        }
        customActivity.setDur(DateConverter.durationTimeConverterFromStringToLong(days, hours, minutes));
        return false;
    }

    private boolean setNeither() {
        customActivity.settN(1);
        if(binding.activityIsTimeMeasured.isChecked()) {
            customActivity.settT(1);
            customActivity.settN(5);
            if(setTimeFromSumTime()) {return false;}
        }
        return true;
    }

    private boolean setRegularity() {
        if(binding.activityDaily.isChecked()) {
            if(!setDaily()) {return false;}
        } else if(binding.activityWeekly.isChecked()) {
            if(!setWeekly()) {return false;}
        } else if(binding.activityMonthly.isChecked()) {
            if(!setMonthly()) {return false;}
        } else {
            return false;
        }
        return true;
    }

    private boolean setDaily() {
        customActivity.setReg(1);
        customActivity.settN(1);
        if(binding.activityIsTimeMeasured.isChecked()) {
            customActivity.settT(1);
            customActivity.settN(2);
            if(setTimeFromSumTime()) {return false;}
        }
        return true;
    }

    private boolean setWeekly() {
        customActivity.setReg(2);
        if(binding.activityHasFixedWeeks.isChecked()) {
            if(!setFixedWeeks()) {return false;}
        } else {
            if(!setWeeklyWithoutFixedDays()) {return false;}
        }
        return true;
    }

    private boolean setWeeklyWithoutFixedDays() {
        if(!setAnEndDate()) {return false;}
        customActivity.settT(3);
        customActivity.settN(4);
        return !setTimeFromSumTime();
    }

    private boolean setFixedWeeks() {
        if(!setAnEndDate()) {return false;}
        customActivity.settN(1);
        if(!setFixedDays()) {return false;}
        if(binding.activityIsTimeMeasured.isChecked()) {
            if(binding.activityCustomTime.isChecked()) {
                customActivity.settT(5);
                customActivity.settN(8);
                if(!setTimeForFixedDays()) {return false;}
            } else {
                if(binding.activityIsSumTime.isChecked()) {
                    customActivity.settT(1);
                    customActivity.settN(5);
                } else if(binding.activityDaily.isChecked()) {
                    customActivity.settT(2);
                    customActivity.settN(2);
                } else if(binding.activityWeekly.isChecked()) {
                    customActivity.settT(3);
                    customActivity.settN(4);
                }
                if(!setTimeFromSumTime()) {return true;}
            }
        } else {
            if(binding.activityCustomTime.isChecked()) {
                customActivity.settN(8);
            } else if(binding.activityDaily.isChecked()) {
                customActivity.settN(2);
            } else if(binding.activityWeekly.isChecked()) {
                customActivity.settN(4);
            }
        }
        return true;
    }

    private boolean setFixedDays() {
        if(binding.monday.isChecked()) {
            customActivity.getCustomWeekTime().setMon(0);
        }
        if(binding.tuesday.isChecked()) {
            customActivity.getCustomWeekTime().setTue(0);
        }
        if(binding.wednesday.isChecked()) {
            customActivity.getCustomWeekTime().setWed(0);
        }
        if(binding.thursday.isChecked()) {
            customActivity.getCustomWeekTime().setThu(0);
        }
        if(binding.friday.isChecked()) {
            customActivity.getCustomWeekTime().setFri(0);
        }
        if(binding.saturday.isChecked()) {
            customActivity.getCustomWeekTime().setSat(0);
        }
        if(binding.sunday.isChecked()) {
            customActivity.getCustomWeekTime().setSun(0);
        }
        if(customActivity.getCustomWeekTime().nothingSet()) {
            binding.monday.setError("You must set at least one day!");
            binding.monday.requestFocus();
            return false;
        }
        customActivity.sethFD(true);
        return true;
    }

    private boolean setTimeForFixedDays() {
        long i;
        if(binding.monday.isChecked()) {
            i = setTimeForDay(binding.activityMondayPicker);
            if(i != -1L) {
                customActivity.getCustomWeekTime().setMon(i);
            }
        }
        if(binding.tuesday.isChecked()) {
            i = setTimeForDay(binding.activityTuesdayPicker);
            if(i != -1L) {
                customActivity.getCustomWeekTime().setTue(i);
            }
        }
        if(binding.wednesday.isChecked()) {
            i = setTimeForDay(binding.activityWednesdayPicker);
            if(i != -1L) {
                customActivity.getCustomWeekTime().setWed(i);
            }
        }
        if(binding.thursday.isChecked()) {
            i = setTimeForDay(binding.activityThursdayPicker);
            if(i != -1L) {
                customActivity.getCustomWeekTime().setThu(i);
            }
        }
        if(binding.friday.isChecked()) {
            i = setTimeForDay(binding.activityFridayPicker);
            if(i != -1L) {
                customActivity.getCustomWeekTime().setFri(i);
            }
        }
        if(binding.saturday.isChecked()) {
            i = setTimeForDay(binding.activitySaturdayPicker);
            if(i != -1L) {
                customActivity.getCustomWeekTime().setSat(i);
            }
        }
        if(binding.sunday.isChecked()) {
            i = setTimeForDay(binding.activitySundayPicker);
            if(i != -1L) {
                customActivity.getCustomWeekTime().setSun(i);
            }
        }
        return true;
    }

    private long setTimeForDay(CustomTimePickerForOneDayBinding tp) {
        String hours = tp.oneDayHours.getText().toString().trim();
        String minutes = tp.oneDayMinutes.getText().toString().trim();
        if(hours.isEmpty() || minutes.isEmpty()) {
            tp.oneDayHours.setError("Setting hours and minutes is required (it can be 0)!");
            tp.oneDayHours.requestFocus();
            return -1L;
        }
        int hour = 0;
        int minute = 0;
        try {
            hour = Integer.parseInt(hours);
            minute = Integer.parseInt(minutes);
        }catch (NumberFormatException e) {
            e.printStackTrace();
            return -1L;
        }
        if(hour > 23 || hour < 0 || minute > 59 || minute < 0) {
            tp.oneDayHours.setError("Hours must be whole number between 0-23 and minutes 0-59!");
            tp.oneDayHours.requestFocus();
            return -1L;
        }
        return DateConverter.durationTimeConverterFromIntToLongForDays(hour, minute);
    }

    private boolean setMonthly() {
        customActivity.setReg(3);
        customActivity.settN(3);
        if(!setAnEndDate()) {return false;}
        customActivity.settT(4);
        return !setTimeFromSumTime();
    }

    private boolean setAnEndDate() {
        if(binding.activityHasAnEndDate.isChecked()) {
            String ed = binding.activityEndDate.getText().toString().trim();
            if(ed.isEmpty()) {
                binding.activityEndDate.setError("End date is required!");
                binding.activityEndDate.requestFocus();
                return false;
            }
            customActivity.seteD(DateConverter.stringFromSimpleDateDialogToLongMillis(ed));
        }
        return true;
    }

    private void addActivityToDb(View fragView) {
        mainViewModel.updateActivity(customActivity);
        System.out.println(customActivity);
        //Navigation.findNavController(this.getView()).navigate(R.id.action_editActivityFragment_to_detailFragment);
        navigateBackToDetails(fragView);
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
                                color = envelope.getColor();
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

    private void initNameChooser() {
        if(CustomActivityHelper.isFixActivity(customActivity.getName())) {
            binding.activityNameText.setText(R.string.global_activity_name_immutable);
            binding.activityName.setEnabled(false);
            binding.activityName.setText(CustomActivityHelper.getStringResourceOfFixActivity(customActivity.getName()));
            binding.activityName.setTypeface(null, Typeface.ITALIC);
        } else {
            binding.activityName.setText(customActivity.getName());
        }
    }

    private void initNotifyTypeRadioButtonGroup() {
        binding.selectNotifTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.activityRegularity:
                        setIntervalGone();
                        setEndDateGone();
                        binding.activityRegularityTypeText.setVisibility(View.VISIBLE);
                        binding.selectExactRegularity.setVisibility(View.VISIBLE);
                        setDurationGone();
                        binding.activityHasFixedWeeks.setVisibility(View.GONE);
                        binding.activityIsTimeMeasured.setVisibility(View.GONE);
                        binding.activityHasAnEndDate.setVisibility(View.GONE);
                        break;
                    case R.id.activityIsInterval:
                        setIntervalVisible();
                        setEndDateGone();
                        setRegularityGone();
                        setDurationGone();
                        setRegularityRadiosFalse();
                        binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                        binding.activityHasFixedWeeks.setVisibility(View.GONE);
                        binding.activityHasAnEndDate.setVisibility(View.GONE);
                        binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
                        binding.durationText.setVisibility(View.GONE);
                        break;
                    case R.id.activityCustom:
                        setIntervalGone();
                        setEndDateGone();
                        setRegularityGone();
                        setDurationGone();
                        setRegularityRadiosFalse();
                        binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                        binding.activityHasFixedWeeks.setVisibility(View.GONE);
                        binding.activityHasAnEndDate.setVisibility(View.GONE);
                        binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
                        binding.durationText.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    private void setRegularityRadiosFalse() {
        binding.selectExactRegularity.clearCheck();
    }

    private void initRegularityTypeRadioButtonGroup() {
        binding.selectExactRegularity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.activityDaily:
                        setWeeklyStuffGone();
                        setEndDateGone();
                        setDurationGone();
                        binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                        break;
                    case R.id.activityWeekly:
                        binding.activityHasFixedWeeks.setChecked(false);
                        binding.activityHasFixedWeeks.setVisibility(View.VISIBLE);
                        binding.activityHasAnEndDate.setChecked(false);
                        binding.activityHasAnEndDate.setVisibility(View.VISIBLE);
                        setDurationGone();
                        binding.activityIsTimeMeasured.setVisibility(View.GONE);
                        binding.durationText.setText(R.string.choose_one_weekly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        if(!setupFinished && customActivity.getDur() != 0L) {
                            setSumTimePickerFromActivity();
                        }else{
                            setSumTimePickerDefault();
                        }
                        break;
                    case R.id.activityMonthly:
                        setWeeklyStuffGone();
                        binding.activityHasAnEndDate.setVisibility(View.VISIBLE);
                        setDurationGone();
                        binding.activityIsTimeMeasured.setVisibility(View.GONE);
                        binding.durationText.setText(R.string.choose_monthly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        if(!setupFinished && customActivity.getDur() != 0L) {
                            setSumTimePickerFromActivity();
                        }else{
                            setSumTimePickerDefault();
                        }
                        break;
                }
            }
        });
    }

    private void initHasFixedDaysSwitch() {
        binding.activityHasFixedWeeks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    binding.activityWeeklyDays.setVisibility(View.VISIBLE);
                    binding.allDaysOfWeek.setVisibility(View.VISIBLE);
                    if(!setupFinished && !customActivity.getCustomWeekTime().nothingSet()) {
                        setFixedDaysFromActivity();
                    }
                    setDurationGone();
                    binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                } else {
                    binding.activityWeeklyDays.setVisibility(View.GONE);
                    binding.allDaysOfWeek.setVisibility(View.GONE);
                    setDurationGone();
                    binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                    //if(binding.activityWeekly.isChecked()) {
                    binding.durationText.setText(R.string.choose_one_weekly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    if(!setupFinished && customActivity.getDur() != 0L) {
                        setSumTimePickerFromActivity();
                    }else{
                        setSumTimePickerDefault();
                    }
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                    //}
                }
            }
        });
    }

    private void initHasEndDateSwitch() {
        binding.activityHasAnEndDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    binding.activityEndDate.setVisibility(View.VISIBLE);
                    setDurationGone();
                    binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                } else {
                    binding.activityEndDate.setVisibility(View.GONE);
                    setDurationGone();
                    binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                }
                if(binding.activityWeekly.isChecked() && !binding.activityHasFixedWeeks.isChecked()) {
                    binding.durationText.setText(R.string.choose_one_weekly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    if(!setupFinished && customActivity.getDur() != 0L) {
                        setSumTimePickerFromActivity();
                    }else{
                        setSumTimePickerDefault();
                    }
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                } else if(binding.activityMonthly.isChecked()) {
                    binding.durationText.setText(R.string.choose_monthly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    if(!setupFinished && customActivity.getDur() != 0L) {
                        setSumTimePickerFromActivity();
                    }else{
                        setSumTimePickerDefault();
                    }
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initIsTimeMeasuredSwitch() {
        binding.activityIsTimeMeasured.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if(binding.activityCustom.isChecked()) {
                        binding.durationText.setText(R.string.choose_sum_time_for_neither);
                        binding.durationText.setVisibility(View.VISIBLE);
                        if(!setupFinished && customActivity.getDur() != 0L) {
                            setSumTimePickerFromActivity();
                        }else{
                            setSumTimePickerDefault();
                        }
                    } else if(binding.activityIsInterval.isChecked()) {
                        binding.selectDurationType.setVisibility(View.VISIBLE);
                        binding.activityIsSumTime.setVisibility(View.VISIBLE);
                        binding.activityIsTime.setVisibility(View.VISIBLE);
                        binding.activityCustomTime.setVisibility(View.GONE);
                        binding.activityIsWeeklyTime.setVisibility(View.GONE);
                    } else if(binding.activityDaily.isChecked()) {
                        binding.durationText.setText(R.string.choose_daily_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        if(!setupFinished && customActivity.getDur() != 0L) {
                            setSumTimePickerFromActivity();
                        }else{
                            setSumTimePickerDefault();
                        }
                        binding.activitySumTimePicker.days.setEnabled(false);
                        binding.activitySumTimePicker.days.setBackgroundColor(Color.LTGRAY);
                        binding.activitySumTimePicker.days.setTypeface(null, Typeface.ITALIC);
                    } else if(binding.activityWeekly.isChecked()) {
                        if (binding.activityHasFixedWeeks.isChecked()) {
                            if(binding.activityHasAnEndDate.isChecked()) {
                                binding.selectDurationType.setVisibility(View.VISIBLE);
                                binding.activityIsSumTime.setVisibility(View.VISIBLE);
                                binding.activityIsTime.setVisibility(View.VISIBLE);
                                binding.activityCustomTime.setVisibility(View.VISIBLE);
                                binding.activityIsWeeklyTime.setVisibility(View.VISIBLE);
                            } else {
                                binding.selectDurationType.setVisibility(View.VISIBLE);
                                binding.activityIsSumTime.setVisibility(View.GONE);
                                binding.activityIsTime.setVisibility(View.VISIBLE);
                                binding.activityCustomTime.setVisibility(View.VISIBLE);
                                binding.activityIsWeeklyTime.setVisibility(View.VISIBLE);
                            }
                        } else {
                            binding.durationText.setText(R.string.choose_one_weekly_time);
                            binding.durationText.setVisibility(View.VISIBLE);
                            if(!setupFinished && customActivity.getDur() != 0L) {
                                setSumTimePickerFromActivity();
                            }else{
                                setSumTimePickerDefault();
                            }
                        }
                    } else if(binding.activityMonthly.isChecked()) {
                        binding.durationText.setText(R.string.choose_monthly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        if(!setupFinished && customActivity.getDur() != 0L) {
                            setSumTimePickerFromActivity();
                        }else{
                            setSumTimePickerDefault();
                        }
                    }
                } else {
                    setDurationTypeRadiosToFalse();
                    binding.durationText.setVisibility(View.GONE);
                    binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
                    binding.selectDurationType.setVisibility(View.GONE);
                    binding.fixedDaysTimes.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setDurationTypeRadiosToFalse() {
        binding.selectDurationType.clearCheck();
    }

    private void initSelectDurationTypeRadioGroups() {
        binding.selectDurationType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.activityIsSumTime:
                        binding.fixedDaysTimes.setVisibility(View.GONE);
                        if(binding.activityIsInterval.isChecked()) {
                            binding.durationText.setText(R.string.choose_sum_time_for_interval);
                            binding.durationText.setVisibility(View.VISIBLE);
                            if(!setupFinished && customActivity.getDur() != 0L) {
                                setSumTimePickerFromActivity();
                            }else{
                                setSumTimePickerDefault();
                            }
                        } else {
                            binding.durationText.setText(R.string.choose_sum_time_for_fixed_week_days);
                            binding.durationText.setVisibility(View.VISIBLE);
                            if(!setupFinished && customActivity.getDur() != 0L) {
                                setSumTimePickerFromActivity();
                            }else{
                                setSumTimePickerDefault();
                            }
                        }
                        break;
                    case R.id.activityIsTime:
                        binding.fixedDaysTimes.setVisibility(View.GONE);
                        if(binding.activityIsInterval.isChecked()) {
                            binding.durationText.setText(R.string.choose_daily_time_for_interval);
                        } else {
                            binding.durationText.setText(R.string.choose_daily_time);
                        }
                        binding.durationText.setVisibility(View.VISIBLE);
                        if(!setupFinished && customActivity.getDur() != 0L) {
                            setSumTimePickerFromActivity();
                        }else{
                            setSumTimePickerDefault();
                        }
                        binding.activitySumTimePicker.days.setEnabled(false);
                        binding.activitySumTimePicker.days.setBackgroundColor(Color.LTGRAY);
                        binding.activitySumTimePicker.days.setTypeface(null, Typeface.ITALIC);
                        break;
                    case R.id.activityCustomTime:
                        binding.durationText.setVisibility(View.GONE);
                        binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
                        if(!setupFinished && customActivity.ishFD()) {
                            resetFixedDaysTimePickersFromActivity();
                        } else {
                            resetFixedDaysTimePickers();
                        }
                        binding.fixedDaysTimes.setVisibility(View.VISIBLE);
                        break;
                    case R.id.activityIsWeeklyTime:
                        binding.fixedDaysTimes.setVisibility(View.GONE);
                        binding.durationText.setText(R.string.choose_one_weekly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        if(!setupFinished && customActivity.getDur() != 0L) {
                            setSumTimePickerFromActivity();
                        }else{
                            setSumTimePickerDefault();
                        }
                        break;
                }
            }
        });
    }

    private  void setSumTimePickerDefault() {
        binding.activitySumTimePicker.getRoot().setVisibility(View.VISIBLE);
        binding.activitySumTimePicker.days.setText("0");
        binding.activitySumTimePicker.hours.setText("0");
        binding.activitySumTimePicker.minutes.setText("0");
        binding.activitySumTimePicker.days.setEnabled(true);
        binding.activitySumTimePicker.days.setBackgroundColor(Color.WHITE);
        binding.activitySumTimePicker.days.setTypeface(null, Typeface.NORMAL);
    }

    private  void setSumTimePickerFromActivity() {
        binding.activitySumTimePicker.getRoot().setVisibility(View.VISIBLE);
        binding.activitySumTimePicker.days.setText(String.valueOf(DateConverter.durationConverterFromLongToDays(customActivity.getDur())));
        binding.activitySumTimePicker.hours.setText(String.valueOf(DateConverter.durationConverterFromLongToHours(customActivity.getDur())));
        binding.activitySumTimePicker.minutes.setText(String.valueOf(DateConverter.durationConverterFromLongToMinutes(customActivity.getDur())));
        binding.activitySumTimePicker.days.setEnabled(true);
        binding.activitySumTimePicker.days.setBackgroundColor(Color.WHITE);
        binding.activitySumTimePicker.days.setTypeface(null, Typeface.NORMAL);
    }

    private void initCalendars() {
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
        binding.activityEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateEndDate,calEndDate.get(Calendar.YEAR),calEndDate.get(Calendar.MONTH),calEndDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        binding.activityStartDay.performClick();
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

    private void setDurationGone() {
        binding.activityIsTimeMeasured.setChecked(false);
        binding.selectDurationType.setVisibility(View.GONE);
        binding.durationText.setVisibility(View.GONE);
        binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
        binding.fixedDaysTimes.setVisibility(View.GONE);
    }

    private void resetFixedDaysTimePickers() {
        setFixedDayTimePicker(binding.monday, binding.activityMondayPicker);
        setFixedDayTimePicker(binding.tuesday, binding.activityTuesdayPicker);
        setFixedDayTimePicker(binding.wednesday, binding.activityWednesdayPicker);
        setFixedDayTimePicker(binding.thursday, binding.activityThursdayPicker);
        setFixedDayTimePicker(binding.friday, binding.activityFridayPicker);
        setFixedDayTimePicker(binding.saturday, binding.activitySaturdayPicker);
        setFixedDayTimePicker(binding.sunday, binding.activitySundayPicker);
    }

    private void resetFixedDaysTimePickersFromActivity() {
        setFixedDayTimePickerFromActivity(binding.monday, binding.activityMondayPicker, customActivity.getCustomWeekTime().getMon());
        setFixedDayTimePickerFromActivity(binding.tuesday, binding.activityTuesdayPicker, customActivity.getCustomWeekTime().getTue());
        setFixedDayTimePickerFromActivity(binding.wednesday, binding.activityWednesdayPicker, customActivity.getCustomWeekTime().getWed());
        setFixedDayTimePickerFromActivity(binding.thursday, binding.activityThursdayPicker, customActivity.getCustomWeekTime().getThu());
        setFixedDayTimePickerFromActivity(binding.friday, binding.activityFridayPicker, customActivity.getCustomWeekTime().getFri());
        setFixedDayTimePickerFromActivity(binding.saturday, binding.activitySaturdayPicker, customActivity.getCustomWeekTime().getSat());
        setFixedDayTimePickerFromActivity(binding.sunday, binding.activitySundayPicker, customActivity.getCustomWeekTime().getSun());
    }

    private void setFixedDayTimePickerFromActivity(CheckBox rb, CustomTimePickerForOneDayBinding b, long time) {
        if(rb.isChecked()) {
            resetTimePickerFieldHourForPickingFromActivity(b.oneDayHours, time);
            resetTimePickerFieldMinutesForPickingFromActivity(b.oneDayMinutes, time);
        } else {
            resetTimePickerFieldForNotPicking(b.oneDayHours);
            resetTimePickerFieldForNotPicking(b.oneDayMinutes);
        }
    }

    private void resetTimePickerFieldHourForPickingFromActivity(EditText et, long time) {
        et.setText(String.valueOf(DateConverter.durationConverterFromLongToHours(time)));
        et.setBackgroundColor(Color.WHITE);
        et.setEnabled(true);
        et.setTypeface(null, Typeface.NORMAL);
    }

    private void resetTimePickerFieldMinutesForPickingFromActivity(EditText et, long time) {
        et.setText(String.valueOf(DateConverter.durationConverterFromLongToMinutes(time)));
        et.setBackgroundColor(Color.WHITE);
        et.setEnabled(true);
        et.setTypeface(null, Typeface.NORMAL);
    }

    private void resetTimePickerFieldForPicking(EditText et) {
        et.setText("0");
        et.setBackgroundColor(Color.WHITE);
        et.setEnabled(true);
        et.setTypeface(null, Typeface.NORMAL);
    }

    private void resetTimePickerFieldForNotPicking(EditText et) {
        et.setText("0");
        et.setBackgroundColor(Color.LTGRAY);
        et.setEnabled(false);
        et.setTypeface(null, Typeface.ITALIC);
    }

    private void setFixedDayTimePicker(CheckBox rb, CustomTimePickerForOneDayBinding b) {
        if(rb.isChecked()) {
            resetTimePickerFieldForPicking(b.oneDayHours);
            resetTimePickerFieldForPicking(b.oneDayMinutes);
        } else {
            resetTimePickerFieldForNotPicking(b.oneDayHours);
            resetTimePickerFieldForNotPicking(b.oneDayMinutes);
        }
    }

    private void setOnChangeListenersOnFixedDayTimePicker(CheckBox rb, CustomTimePickerForOneDayBinding tp) {
        rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    resetTimePickerFieldForPicking(tp.oneDayHours);
                    resetTimePickerFieldForPicking(tp.oneDayMinutes);
                } else {
                    resetTimePickerFieldForNotPicking(tp.oneDayHours);
                    resetTimePickerFieldForNotPicking(tp.oneDayMinutes);
                }
            }
        });
    }

    private void initFixedDaysTimePickerListeners() {
        setOnChangeListenersOnFixedDayTimePicker(binding.monday, binding.activityMondayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.tuesday, binding.activityTuesdayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.wednesday, binding.activityWednesdayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.thursday, binding.activityThursdayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.friday, binding.activityFridayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.saturday, binding.activitySaturdayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.sunday, binding.activitySundayPicker);
    }

    private void navigateBackHome(View fragView) {
        //Navigation.findNavController(this.getView()).navigate(R.id.action_editActivityFragment_to_nav_home);
        Navigation.findNavController(fragView).popBackStack();
    }

    private void navigateBackToDetails(View fragView) {
        //Bundle arguments = new Bundle();
        //arguments.putLong(DetailFragment.ARG_ITEM_ID, customActivity.getId());
        //Navigation.findNavController(this.getView()).navigate(R.id.action_editActivityFragment_to_detailFragment, arguments);
        Navigation.findNavController(fragView).popBackStack();
    }

    private void initNoteAndPriority() {
        binding.activityNote.setText(customActivity.getNote());
        binding.activityPriority.setSelection(((ArrayAdapter)binding.activityPriority.getAdapter()).getPosition(String.valueOf(customActivity.getPr())));
    }

    private void initSelection() {
        if(customActivity.getsD() > 0L && customActivity.geteD() > 0L) {
            initInterval();
        } else if(customActivity.getReg() > 0) {
            initRegular();
        } else {
            initNeither();
        }
        setupFinished = true;
    }

    private void initInterval() {
        binding.activityIsInterval.toggle();
        calStartDay.setTimeInMillis(customActivity.getsD());
        calEndDay.setTimeInMillis(customActivity.geteD());
        updateLabelStartDay();
        updateLabelEndDay();
        if(customActivity.gettT() == 1) {
            binding.activityIsTimeMeasured.toggle();
            binding.activityIsSumTime.toggle();
        } else if(customActivity.gettT() == 2) {
            binding.activityIsTimeMeasured.toggle();
            binding.activityIsTime.toggle();
        }
    }

    private void initRegular() {
        binding.activityRegularity.toggle();
        switch (customActivity.getReg()) {
            case 1:
                binding.activityDaily.toggle();
                if(customActivity.gettT() == 2) {
                    binding.activityIsTimeMeasured.toggle();
                }
                break;
            case 2:
                binding.activityWeekly.toggle();
                if(customActivity.ishFD()) {
                    binding.activityHasFixedWeeks.toggle();
                }
                if(customActivity.getsD() == 0L && customActivity.geteD() > 0L) {
                    binding.activityHasAnEndDate.toggle();
                    calEndDate.setTimeInMillis(customActivity.geteD());
                    updateLabelEndDate();
                }
                if(customActivity.gettT() == 1) {
                    binding.activityIsTimeMeasured.toggle();
                    binding.activityIsSumTime.toggle();
                } else if(customActivity.gettT() == 2) {
                    binding.activityIsTimeMeasured.toggle();
                    binding.activityIsTime.toggle();
                } else if(customActivity.gettT() == 3) {
                    binding.activityIsTimeMeasured.toggle();
                    binding.activityIsWeeklyTime.toggle();
                } else if(customActivity.gettT() == 5) {
                    binding.activityIsTimeMeasured.toggle();
                    binding.activityCustomTime.toggle();
                }
                break;
            case 3:
                binding.activityMonthly.toggle();
                if(customActivity.getsD() == 0L && customActivity.geteD() > 0L) {
                    binding.activityHasAnEndDate.toggle();
                    calEndDate.setTimeInMillis(customActivity.geteD());
                    updateLabelEndDate();
                }
                if(customActivity.gettT() == 4) {
                    binding.activityIsTimeMeasured.toggle();
                }
                break;
        }
    }

    private void setFixedDaysFromActivity() {
        if(customActivity.getCustomWeekTime().getMon() >= 0L) {
            binding.monday.toggle();
        }
        if(customActivity.getCustomWeekTime().getTue() >= 0L) {
            binding.tuesday.toggle();
        }
        if(customActivity.getCustomWeekTime().getWed() >= 0L) {
            binding.wednesday.toggle();
        }
        if(customActivity.getCustomWeekTime().getThu() >= 0L) {
            binding.thursday.toggle();
        }
        if(customActivity.getCustomWeekTime().getFri() >= 0L) {
            binding.friday.toggle();
        }
        if(customActivity.getCustomWeekTime().getSat() >= 0L) {
            binding.saturday.toggle();
        }
        if(customActivity.getCustomWeekTime().getSun() >= 0L) {
            binding.sunday.toggle();
        }
    }

    private void initNeither() {
        binding.activityCustom.toggle();
        if(customActivity.gettT() == 1) {
            binding.activityIsTimeMeasured.toggle();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}