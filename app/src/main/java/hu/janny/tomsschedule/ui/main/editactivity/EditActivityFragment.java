package hu.janny.tomsschedule.ui.main.editactivity;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.CustomTimePickerForOneDayBinding;
import hu.janny.tomsschedule.databinding.FragmentEditActivityBinding;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.ActivityWithTimes;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.viewmodel.MainViewModel;

public class EditActivityFragment extends Fragment{

    public static final String ARG_ITEM_ID = "item_id";

    private MainViewModel mainViewModel;
    private FragmentEditActivityBinding binding;

    private AlertDialog colorPickerDialog;
    final Calendar calStartDay= Calendar.getInstance();
    final Calendar calEndDay= Calendar.getInstance();
    final Calendar calEndDate= Calendar.getInstance();

    // Activity to be edited
    private CustomActivity customActivity;
    // The times of activity - we need it to recalculate soFar and remaining fields
    private List<ActivityTime> times;
    int color;
    // Indicates that the activity's data from the database are loaded to the UI or not
    private boolean setupFinished = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Binds layout
        binding = FragmentEditActivityBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Gets a MainViewModel instance
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Initializes color picker and the priority spinner
        intiColorPicker();
        prioritySpinnerListener();

        // Gets activity id argument from the calling view
        // If there is no argument, pop back stack.
        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            long id = getArguments().getLong(ARG_ITEM_ID);
            //mainViewModel.findActivityById(id);
            mainViewModel.findActivityByIdWithTimesEntity(id);
        } else {
            Navigation.findNavController(root).popBackStack();
        }

        // mainViewModel.getSingleActivity()
        // Observer of the activity to get the data from the database
        mainViewModel.getActivityByIdWithTimesEntity().observe(getViewLifecycleOwner(), new Observer<ActivityWithTimes>() {
            @Override
            public void onChanged(ActivityWithTimes activityWithTimes) {
                CustomActivity activity = null;
                if(activityWithTimes != null) {
                    activity = activityWithTimes.customActivity;
                    if(activityWithTimes.activityTimes != null && !activityWithTimes.activityTimes.isEmpty()) {
                        times = new ArrayList<>(activityWithTimes.activityTimes);
                    } else {
                        times = new ArrayList<>();
                    }
                }
                if(activity != null) {
                    customActivity = activity;
                    color = customActivity.getCol();
                    binding.activityColor.setBackgroundColor(color);
                    // Initializes edit text of name
                    initNameChooser();
                    // Initializes note and priority spinner
                    initNoteAndPriority();
                    // Initializes the radio groups and switches
                    initSelection();
                } else {
                    Navigation.findNavController(root).popBackStack();
                    Toast.makeText(getActivity(), getString(R.string.edit_act_no_act), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Initializes the UI, the on click listeners, and defines changes which occur when an item is selected
        initCalendars();
        initNotifyTypeRadioButtonGroup();
        initRegularityTypeRadioButtonGroup();
        initHasFixedDaysSwitch();
        initHasEndDateSwitch();
        initIsTimeMeasuredSwitch();
        initSelectDurationTypeRadioGroups();
        initFixedDaysTimePickerListeners();

        // Sets on click listener of saving activity
        saveOnClickListener(root);

        return root;
    }

    /**
     * Initializes priority choosing spinner. Sets its onItemSelectedListener.
     */
    private void prioritySpinnerListener() {
        binding.activityPriority.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {}

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    /**
     * Initializes the save button's onClickListener.
     * @param fragView view of fragment
     */
    private void saveOnClickListener(View fragView) {
        binding.saveActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveActivity(fragView);
            }
        });
    }

    /**
     * Updates the activity if there is no error or inconsistency in data.
     * @param fragView view of fragment
     */
    private void saveActivity(View fragView) {
        // Name of activity
        String name = binding.activityName.getText().toString().trim();
        if(name.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.edit_act_name_required), Toast.LENGTH_LONG).show();
            return;
        }
        if(!CustomActivityHelper.isFixActivity(name)) {
            customActivity.setName(name);
        }
        // Sets colour, note, priority of activity
        customActivity.setCol(color);
        customActivity.setNote(binding.activityNote.getText().toString().trim());
        customActivity.setPr(Integer.parseInt(binding.activityPriority.getSelectedItem().toString()));
        // Sets every other field to default except for soFar, remaining, allTime, lastDayAdded
        customActivity.setEverythingToDefault();
        // Sets other fields of the activity
        decideWhichMainType(fragView);
    }

    /**
     * Sets the main type of the activity. Interval, regular activity or neither of them.
     * @param fragView view of fragment
     */
    private void decideWhichMainType(View fragView) {
        if(binding.activityIsInterval.isChecked()) {
            // Interval
            if(!setInterval()) {return;}
        } else if(binding.activityCustom.isChecked()) {
            // Neither
            if(!setNeither()) {return;}
        } else if(binding.activityRegularity.isChecked()) {
            // Regular
            if(!setRegularity()) {return;}
        } else {
            Toast.makeText(getActivity(), getString(R.string.new_act_must_choose_type), Toast.LENGTH_LONG).show();
            return;
        }
        CustomActivityHelper.recalculateAfterEditActivity(customActivity, times);
        addActivityToDb(fragView);
    }

    /**
     * Sets the interval start day, end day, and duration if it is set.
     * @return true if there was not error, false otherwise
     */
    private boolean setInterval() {
        // Checks if start day and end day is given
        String from = binding.activityStartDay.getText().toString().trim();
        String to = binding.activityEndDay.getText().toString().trim();
        if(from.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.edit_act_start_day_required), Toast.LENGTH_LONG).show();
            return false;
        }
        if(to.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.edit_act_end_day_required), Toast.LENGTH_LONG).show();
            return false;
        }
        customActivity.setsD(calStartDay.getTimeInMillis());
        customActivity.seteD(calEndDay.getTimeInMillis());
        customActivity.settN(6);
        if(binding.activityIsTimeMeasured.isChecked()) {
            return setIntervalMeasuredTime();
        }
        return true;
    }

    /**
     * Sets duration of interval.
     * @return true if there was not error, false otherwise
     */
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
            Toast.makeText(getActivity(), getString(R.string.edit_act_must_add_time), Toast.LENGTH_LONG).show();
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
            Toast.makeText(getActivity(), getString(R.string.edit_act_format_add_time), Toast.LENGTH_LONG).show();
            return true;
        }
        customActivity.setDur(DateConverter.durationTimeConverterFromIntToLong(day, hour, minute));
        return false;
    }

    /**
     * Sets "neither" and duration of it is set.
     * @return true if there was not error, false otherwise
     */
    private boolean setNeither() {
        customActivity.settN(1);
        if(binding.activityIsTimeMeasured.isChecked()) {
            customActivity.settT(1);
            customActivity.settN(5);
            return !setTimeFromSumTime();
        }
        return true;
    }

    /**
     * Sets the regularity of the activity and duration if it is set.
     * @return true if there was not error, false otherwise
     */
    private boolean setRegularity() {
        if(binding.activityDaily.isChecked()) {
            return setDaily();
        } else if(binding.activityWeekly.isChecked()) {
            return setWeekly();
        } else if(binding.activityMonthly.isChecked()) {
            return setMonthly();
        } else {
            Toast.makeText(getActivity(), getString(R.string.edit_act_must_choose_reg_type), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /**
     * Sets daily regularity and duration if it is set.
     * @return true if there was not error, false otherwise
     */
    private boolean setDaily() {
        customActivity.setReg(1);
        customActivity.settN(1);
        if(binding.activityIsTimeMeasured.isChecked()) {
            customActivity.settT(1);
            customActivity.settN(2);
            return !setTimeFromSumTime();
        }
        return true;
    }

    /**
     * Sets weekly regularity and duration if it is set.
     * @return true if there was not error, false otherwise
     */
    private boolean setWeekly() {
        customActivity.setReg(2);
        if(binding.activityHasFixedWeeks.isChecked()) {
            return setFixedWeeks();
        } else {
            return setWeeklyWithoutFixedDays();
        }
    }

    /**
     * Sets weekly parameters if fix days are not selected.
     * @return true if there was not error, false otherwise
     */
    private boolean setWeeklyWithoutFixedDays() {
        if(!setAnEndDate()) {return false;}
        customActivity.settT(3);
        customActivity.settN(4);
        return !setTimeFromSumTime();
    }

    /**
     * Sets weekly parameters if fix days are selected, including end date, exact days and duration.
     * @return true if there was not error, false otherwise
     */
    private boolean setFixedWeeks() {
        if(!setAnEndDate()) {return false;}
        customActivity.settN(1);
        if(!setFixedDays()) {return false;}
        if(binding.activityIsTimeMeasured.isChecked()) {
            if(binding.activityCustomTime.isChecked()) {
                customActivity.settT(5);
                customActivity.settN(8);
                return setTimeForFixedDays();
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

    /**
     * Sets the exact fix days if they were selected.
     * @return true if there was not error, false otherwise
     */
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
            Toast.makeText(getActivity(), getString(R.string.edit_act_must_set_one_day), Toast.LENGTH_LONG).show();
            return false;
        }
        customActivity.sethFD(true);
        return true;
    }

    /**
     * Sets the time for fix days if they were selected.
     * @return true if there was not error, false otherwise
     */
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

    /**
     * Sets time for the given day from a custom time picker for one day.
     * @param tp custom time picker for one day binding (to reach this layout's items)
     * @return the time given for the day
     */
    private long setTimeForDay(CustomTimePickerForOneDayBinding tp) {
        String hours = tp.oneDayHours.getText().toString().trim();
        String minutes = tp.oneDayMinutes.getText().toString().trim();
        if(hours.isEmpty() || minutes.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.edit_act_must_add_time_for_a_day), Toast.LENGTH_LONG).show();
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
            Toast.makeText(getActivity(), getString(R.string.edit_act_format_add_time_for_a_day), Toast.LENGTH_LONG).show();
            return -1L;
        }
        return DateConverter.durationTimeConverterFromIntToLongForDays(hour, minute);
    }

    /**
     * Sets monthly regularity and duration if it is set.
     * @return true if there was not error, false otherwise
     */
    private boolean setMonthly() {
        customActivity.setReg(3);
        customActivity.settN(3);
        if(!setAnEndDate()) {return false;}
        customActivity.settT(4);
        return !setTimeFromSumTime();
    }

    /**
     * Sets the end date for the activity.
     * @return true if there was not error, false otherwise
     */
    private boolean setAnEndDate() {
        if(binding.activityHasAnEndDate.isChecked()) {
            String ed = binding.activityEndDate.getText().toString().trim();
            if(ed.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.edit_act_format_add_time), Toast.LENGTH_LONG).show();
                return false;
            }
            customActivity.seteD(calEndDate.getTimeInMillis());
        }
        return true;
    }

    /**
     * Updated the activity in the local database.
     * @param fragView root view of fragment
     */
    private void addActivityToDb(View fragView) {
        mainViewModel.updateActivity(customActivity);
        Navigation.findNavController(fragView).popBackStack();
    }

    /**
     * Initializes the color picker.
     */
    private void intiColorPicker() {
        colorPickerDialog = new ColorPickerDialog.Builder(getActivity())
                .setTitle(R.string.select_color_for_activity)
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
        // Sets onClickListener for opening dialog
        binding.activityColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPickerDialog.show();
            }
        });
    }

    /**
     * Initializes edit text of name. If it is a fix activity, we cannot change name just otherwise.
     */
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

    /**
     * Initializes the radio group where we can select if we want regular activity, an interval or neither of them.
     * Displays the corresponding UI items based on our choice.
     */
    private void initNotifyTypeRadioButtonGroup() {
        binding.selectNotifTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(binding.activityRegularity.isChecked()) {
                    // Regular
                    setIntervalGone();
                    setEndDateGone();
                    binding.activityRegularityTypeText.setVisibility(View.VISIBLE);
                    binding.selectExactRegularity.setVisibility(View.VISIBLE);
                    setDurationGone();
                    binding.activityHasFixedWeeks.setVisibility(View.GONE);
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                    binding.activityHasAnEndDate.setVisibility(View.GONE);
                } else if(binding.activityIsInterval.isChecked()) {
                    // Interval
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
                } else if(binding.activityCustom.isChecked()) {
                    // Neither
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
                }
            }
        });
    }

    /**
     * Clears regularity radio group checks.
     */
    private void setRegularityRadiosFalse() {
        binding.selectExactRegularity.clearCheck();
    }

    /**
     * Initializes the radio button group where we can choose the regularity of the activity,
     * daily, weekly or monthly. Displays the corresponding UI items based on our choice.
     */
    private void initRegularityTypeRadioButtonGroup() {
        binding.selectExactRegularity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(binding.activityDaily.isChecked()) {
                    // Daily
                    setWeeklyStuffGone();
                    setEndDateGone();
                    setDurationGone();
                    binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                } else if(binding.activityWeekly.isChecked()) {
                    // Weekly
                    binding.activityHasFixedWeeks.setChecked(false);
                    binding.activityHasFixedWeeks.setVisibility(View.VISIBLE);
                    binding.activityHasAnEndDate.setChecked(false);
                    binding.activityHasAnEndDate.setVisibility(View.VISIBLE);
                    setDurationGone();
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                    binding.durationText.setText(R.string.choose_one_weekly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePicker();
                } else if(binding.activityMonthly.isChecked()) {
                    // Monthly
                    setWeeklyStuffGone();
                    binding.activityHasAnEndDate.setVisibility(View.VISIBLE);
                    setDurationGone();
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                    binding.durationText.setText(R.string.choose_monthly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePicker();
                }
            }
        });
    }

    /**
     * Initializes the "has fix days" switch.
     */
    private void initHasFixedDaysSwitch() {
        binding.activityHasFixedWeeks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    binding.activityWeeklyDays.setVisibility(View.VISIBLE);
                    binding.allDaysOfWeek.setVisibility(View.VISIBLE);
                    // First we restore the time from the database
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
                    binding.durationText.setText(R.string.choose_one_weekly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePicker();
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Initializes the "has end date" switch.
     */
    private void initHasEndDateSwitch() {
        binding.activityHasAnEndDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    binding.activityEndDate.setVisibility(View.VISIBLE);
                } else {
                    binding.activityEndDate.setVisibility(View.GONE);
                }
                setDurationGone();
                binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                // If it is regular weekly and no fix days
                if(binding.activityWeekly.isChecked() && !binding.activityHasFixedWeeks.isChecked()) {
                    binding.durationText.setText(R.string.choose_one_weekly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePicker();
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                    // If it is regular monthly
                } else if(binding.activityMonthly.isChecked()) {
                    binding.durationText.setText(R.string.choose_monthly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePicker();
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Initializes the "is time measured" switch.
     */
    private void initIsTimeMeasuredSwitch() {
        binding.activityIsTimeMeasured.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) { // Checked
                    if(binding.activityCustom.isChecked()) { // Neither
                        binding.durationText.setText(R.string.choose_sum_time_for_neither);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePicker();
                    } else if(binding.activityIsInterval.isChecked()) { // Interval
                        binding.selectDurationType.setVisibility(View.VISIBLE);
                        binding.activityIsSumTime.setVisibility(View.VISIBLE);
                        binding.activityIsTime.setVisibility(View.VISIBLE);
                        binding.activityCustomTime.setVisibility(View.GONE);
                        binding.activityIsWeeklyTime.setVisibility(View.GONE);
                    } else if(binding.activityDaily.isChecked()) { // Regular - daily
                        binding.durationText.setText(R.string.choose_daily_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePicker();
                        binding.activitySumTimePicker.days.setEnabled(false);
                        binding.activitySumTimePicker.days.setBackgroundColor(Color.LTGRAY);
                        binding.activitySumTimePicker.days.setTypeface(null, Typeface.ITALIC);
                    } else if(binding.activityWeekly.isChecked()) { // Regular - weekly
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
                            // First we restore the time from the database
                            setSumTimePicker();
                        }
                    } else if(binding.activityMonthly.isChecked()) { // Regular - monthly
                        binding.durationText.setText(R.string.choose_monthly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePicker();
                    }
                } else { // Unchecked
                    setDurationTypeRadiosToFalse();
                    binding.durationText.setVisibility(View.GONE);
                    binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
                    binding.selectDurationType.setVisibility(View.GONE);
                    binding.fixedDaysTimes.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Clears check in duration type radio button group.
     */
    private void setDurationTypeRadiosToFalse() {
        binding.selectDurationType.clearCheck();
    }

    /**
     * Initializes the duration type radio group where we can choose from sum time, daily, weekly or custom time.
     * Displays the corresponding UI items based on our choice.
     */
    private void initSelectDurationTypeRadioGroups() {
        binding.selectDurationType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(binding.activityIsSumTime.isChecked()) {
                    binding.fixedDaysTimes.setVisibility(View.GONE);
                    if(binding.activityIsInterval.isChecked()) {
                        binding.durationText.setText(R.string.choose_sum_time_for_interval);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePicker();
                    } else {
                        binding.durationText.setText(R.string.choose_sum_time_for_fixed_week_days);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePicker();
                    }
                } else if(binding.activityIsTime.isChecked()) {
                    binding.fixedDaysTimes.setVisibility(View.GONE);
                    if(binding.activityIsInterval.isChecked()) {
                        binding.durationText.setText(R.string.choose_daily_time_for_interval);
                    } else {
                        binding.durationText.setText(R.string.choose_daily_time);
                    }
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePicker();
                    binding.activitySumTimePicker.days.setEnabled(false);
                    binding.activitySumTimePicker.days.setBackgroundColor(Color.LTGRAY);
                    binding.activitySumTimePicker.days.setTypeface(null, Typeface.ITALIC);
                } else if(binding.activityCustomTime.isChecked()) {
                    binding.durationText.setVisibility(View.GONE);
                    binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
                    setFixDaysPickers();
                    binding.fixedDaysTimes.setVisibility(View.VISIBLE);
                } else if(binding.activityIsWeeklyTime.isChecked()) {
                    binding.fixedDaysTimes.setVisibility(View.GONE);
                    binding.durationText.setText(R.string.choose_one_weekly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePicker();
                }
            }
        });
    }

    /**
     * Sets fix days time pickers to default or to the time that comes from the database.
     */
    private void setFixDaysPickers() {
        if(!setupFinished && customActivity.ishFD()) {
            resetFixedDaysTimePickersFromActivity();
        } else {
            resetFixedDaysTimePickers();
        }
    }

    /**
     * Sets time picker to default or to the time that comes from the database.
     */
    private void setSumTimePicker() {
        if(!setupFinished && customActivity.getDur() != 0L) {
            setSumTimePickerFromActivity();
        }else{
            setSumTimePickerDefault();
        }
    }

    /**
     * Sets time picker to default data.
     */
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

    /**
     * Initializes date picker dialogs and set on click listeners to these date picker dialogs.
     */
    private void initCalendars() {
        // Initializes date picker dialogs' onDateSetListener and after that it sets the UI according to
        // the new date
        DatePickerDialog.OnDateSetListener dateStartday = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calStartDay.clear();
                calStartDay.set(year, month, day);
                updateLabelStartDay();
            }
        };

        DatePickerDialog.OnDateSetListener dateEndDay = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calEndDay.clear();
                calEndDay.set(year, month, day);
                updateLabelEndDay();
            }
        };

        DatePickerDialog.OnDateSetListener dateEndDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calEndDate.clear();
                calEndDate.set(year, month, day);
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
        initTodayDate();
    }

    /**
     * Initializes start day picker dialog for today date and shows on the UI as well.
     */
    private void initTodayDate() {
        Calendar helper = Calendar.getInstance();
        int year = helper.get(Calendar.YEAR);
        int month = helper.get(Calendar.MONTH);
        int day = helper.get(Calendar.DATE);
        calStartDay.clear();
        calStartDay.set(year, month, day);
        updateLabelStartDay();
    }

    /**
     * Displays start day's date.
     */
    private void updateLabelStartDay(){
        binding.activityStartDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calStartDay.get(Calendar.DATE), calStartDay.get(Calendar.MONTH) + 1, calStartDay.get(Calendar.YEAR)));
    }

    /**
     * Displays end day's date.
     */
    private void updateLabelEndDay(){
        binding.activityEndDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calEndDay.get(Calendar.DATE), calEndDay.get(Calendar.MONTH) + 1, calEndDay.get(Calendar.YEAR)));
    }

    /**
     * Displays end date's date.
     */
    private void updateLabelEndDate(){
        binding.activityEndDate.setText(DateConverter.makeDateStringForSimpleDateDialog(
                calEndDate.get(Calendar.DATE), calEndDate.get(Calendar.MONTH) + 1, calEndDate.get(Calendar.YEAR)));
    }

    /**
     * Sets UI items belonging to interval to gone.
     */
    private void setIntervalGone() {
        binding.startDayText.setVisibility(View.GONE);
        binding.activityStartDay.setVisibility(View.GONE);
        binding.endDayText.setVisibility(View.GONE);
        binding.activityEndDay.setVisibility(View.GONE);
    }

    /**
     * Sets UI items belonging to interval to visible.
     */
    private void setIntervalVisible() {
        binding.startDayText.setVisibility(View.VISIBLE);
        binding.activityStartDay.setVisibility(View.VISIBLE);
        binding.endDayText.setVisibility(View.VISIBLE);
        binding.activityEndDay.setVisibility(View.VISIBLE);
    }

    /**
     * Set UI items belonging to regularity to gone.
     */
    private void setRegularityGone() {
        binding.activityRegularityTypeText.setVisibility(View.GONE);
        binding.selectExactRegularity.setVisibility(View.GONE);
        binding.activityHasFixedWeeks.setChecked(false);
        binding.activityHasFixedWeeks.setVisibility(View.GONE);
        binding.activityWeeklyDays.setVisibility(View.GONE);
        binding.allDaysOfWeek.setVisibility(View.GONE);
    }

    /**
     * Set UI items belonging to end date to gone.
     */
    private void setEndDateGone() {
        binding.activityHasAnEndDate.setChecked(false);
        binding.activityHasAnEndDate.setVisibility(View.GONE);
        binding.activityEndDate.setVisibility(View.GONE);
    }

    /**
     * Set UI items belonging to weekly regularity to gone.
     */
    private void setWeeklyStuffGone() {
        binding.activityHasFixedWeeks.setVisibility(View.GONE);
        binding.activityWeeklyDays.setVisibility(View.GONE);
        binding.allDaysOfWeek.setVisibility(View.GONE);
    }

    /**
     * Set UI items belonging to duration to gone.
     */
    private void setDurationGone() {
        binding.activityIsTimeMeasured.setChecked(false);
        binding.selectDurationType.setVisibility(View.GONE);
        binding.durationText.setVisibility(View.GONE);
        binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
        binding.fixedDaysTimes.setVisibility(View.GONE);
    }

    /**
     * Resets fix day time pickers.
     */
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

    /**
     * Resets fix day time picker field for picking.
     * @param et the field EditText
     */
    private void resetTimePickerFieldForPicking(EditText et) {
        et.setText("0");
        et.setBackgroundColor(Color.WHITE);
        et.setEnabled(true);
        et.setTypeface(null, Typeface.NORMAL);
    }

    /**
     * Resets fix day time picker field for not picking.
     * @param et the field EditText
     */
    private void resetTimePickerFieldForNotPicking(EditText et) {
        et.setText("0");
        et.setBackgroundColor(Color.LTGRAY);
        et.setEnabled(false);
        et.setTypeface(null, Typeface.ITALIC);
    }

    /**
     * Resets a given fix day time picker based on if its checkbox is checked or not.
     * @param rb checkbox of the day
     * @param b custom time picker binding
     */
    private void setFixedDayTimePicker(CheckBox rb, CustomTimePickerForOneDayBinding b) {
        if(rb.isChecked()) {
            resetTimePickerFieldForPicking(b.oneDayHours);
            resetTimePickerFieldForPicking(b.oneDayMinutes);
        } else {
            resetTimePickerFieldForNotPicking(b.oneDayHours);
            resetTimePickerFieldForNotPicking(b.oneDayMinutes);
        }
    }

    /**
     * Sets on change listener on a given fix day time picker.
     * @param rb checkbox of the day
     * @param tp custom time picker binding
     */
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

    /**
     * Initializes fix days time pickers.
     * It uses setOnChangeListenersOnFixedDayTimePicker(CheckBox rb, CustomTimePickerForOneDayBinding tp) method.
     */
    private void initFixedDaysTimePickerListeners() {
        setOnChangeListenersOnFixedDayTimePicker(binding.monday, binding.activityMondayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.tuesday, binding.activityTuesdayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.wednesday, binding.activityWednesdayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.thursday, binding.activityThursdayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.friday, binding.activityFridayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.saturday, binding.activitySaturdayPicker);
        setOnChangeListenersOnFixedDayTimePicker(binding.sunday, binding.activitySundayPicker);
    }

    /**
     * Initializes note and priority spinner from activity.
     */
    private void initNoteAndPriority() {
        binding.activityNote.setText(customActivity.getNote());
        binding.activityPriority.setSelection(10 - customActivity.getPr());
    }

    /**
     * Initializes the UI (radio button groups and switches) based on the activity's
     * current fields what come from the database.
     */
    private void initSelection() {
        // Selects the main type then initializes them
        if(customActivity.getsD() > 0L && customActivity.geteD() > 0L) {
            // Interval
            initInterval();
        } else if(customActivity.getReg() > 0) {
            // Regular
            initRegular();
        } else {
            // Neither
            initNeither();
        }
        setupFinished = true;
    }

    /**
     * Initializes the UI to interval based on the activity's current fields what come from the database.
     */
    private void initInterval() {
        binding.activityIsInterval.toggle();
        // Updates start and end day calendars
        setStartDayCalendar();
        setEndDayCalendar();
        // Time is measured and it is all time together or daily
        if(customActivity.gettT() == 1) {
            binding.activityIsTimeMeasured.toggle();
            binding.activityIsSumTime.toggle();
        } else if(customActivity.gettT() == 2) {
            binding.activityIsTimeMeasured.toggle();
            binding.activityIsTime.toggle();
        }
    }

    /**
     * Initializes the start day calendar and UI based on the activity's current fields what come from the database.
     */
    private void setStartDayCalendar() {
        calStartDay.setTimeInMillis(customActivity.getsD());
        updateLabelStartDay();
    }

    /**
     * Initializes the end day calendar and UI based on the activity's current fields what come from the database.
     */
    private void setEndDayCalendar() {
        calEndDay.setTimeInMillis(customActivity.geteD());
        updateLabelEndDay();
    }

    /**
     * Initializes the UI to regular based on the activity's current fields what come from the database.
     */
    private void initRegular() {
        binding.activityRegularity.toggle();
        switch (customActivity.getReg()) {
            case 1:
                // Daily
                binding.activityDaily.toggle();
                // Time is measured and daily
                if(customActivity.gettT() == 2) {
                    binding.activityIsTimeMeasured.toggle();
                }
                break;
            case 2:
                // Weekly
                binding.activityWeekly.toggle();
                // Fixed days are set
                if(customActivity.ishFD()) {
                    binding.activityHasFixedWeeks.toggle();
                }
                // With end date
                if(customActivity.getsD() == 0L && customActivity.geteD() > 0L) {
                    binding.activityHasAnEndDate.toggle();
                    setEndDayCalendar();
                }
                // Time is measured and all time together, daily, weekly, or custom
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
                // Monthly
                binding.activityMonthly.toggle();
                // With end date
                if(customActivity.getsD() == 0L && customActivity.geteD() > 0L) {
                    binding.activityHasAnEndDate.toggle();
                    setEndDayCalendar();
                }
                // Time is measured and monthly
                if(customActivity.gettT() == 4) {
                    binding.activityIsTimeMeasured.toggle();
                }
                break;
        }
    }

    /**
     * Sets checkboxes of days of week based on the activity's current fields what come from the database.
     * If a day is >=0L, we toggle it.
     */
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

    /**
     * Initializes the UI to "neither" based on the activity's current fields what come from the database.
     */
    private void initNeither() {
        binding.activityCustom.toggle();
        // Time is measured and all time together
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