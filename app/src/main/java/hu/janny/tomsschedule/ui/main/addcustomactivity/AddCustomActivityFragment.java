package hu.janny.tomsschedule.ui.main.addcustomactivity;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.CustomTimePickerForOneDayBinding;
import hu.janny.tomsschedule.databinding.FragmentAddCustomActivityBinding;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.viewmodel.MainViewModel;

public class AddCustomActivityFragment extends Fragment {

    private MainViewModel mainViewModel;
    private FragmentAddCustomActivityBinding binding;

    private AlertDialog colorPickerDialog;
    private LocalDate ldStartDay;
    private LocalDate ldEndDay;
    private LocalDate ldEndDate;
    // Beginning colour
    int color; //Color.rgb(255, 164, 119);

    // The new activity
    private CustomActivity customActivity;

    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Binds layout
        binding = FragmentAddCustomActivityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        color = ContextCompat.getColor(view.getContext(), R.color.base_activity);

        // Gets a MainViewModel instance
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Initializes color picker and the priority and fix activity spinners
        intiColorPicker();
        prioritySpinnerListener();
        fixActivitySpinnerListener();

        // Gets user data due to saving the activity with the user id
        mainViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUser = user;
            }
        });

        // Initializes the UI, the on click listeners, and defines changes which occur when an item is selected
        initCalendars();
        initNameChooserRadioButtonGroup();
        initNotifyTypeRadioButtonGroup();
        initRegularityTypeRadioButtonGroup();
        initHasFixedDaysSwitch();
        initHasEndDateSwitch();
        initIsTimeMeasuredSwitch();
        initSelectDurationTypeRadioGroups();
        initFixedDaysTimePickerListeners();

        // Sets on click listener of saving activity
        saveOnClickListener(view);
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
     * Initializes fix activity choosing spinner. Sets its onItemSelectedListener.
     */
    private void fixActivitySpinnerListener() {
        binding.selectFixActivitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Saves the activity if there is no error or inconsistency in data.
     * @param fragView view of fragment
     */
    private void saveActivity(View fragView) {
        // Name of activity
        String name;
        if(binding.selectFixActivityOption.isChecked()) {
            name = CustomActivityHelper.getSelectedFixActivityName(binding.selectFixActivitySpinner.getSelectedItem().toString().trim());
        } else {
            name = binding.activityName.getText().toString().trim();
        }
        if(name.isEmpty()) {
            binding.activityName.setError(getString(R.string.new_activity_name_is_required));
            binding.activityName.requestFocus();
            return;
        }
        // Colour, note, priority of activity
        int col = color;
        String note = binding.activityNote.getText().toString().trim();
        int priority = Integer.parseInt(binding.activityPriority.getSelectedItem().toString());

        // Creates the activity
        if(currentUser != null) {
            long activityId = System.currentTimeMillis();
            customActivity = new CustomActivity(activityId, currentUser.uid, name, col, note, priority);
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_saving_no_user), Toast.LENGTH_LONG).show();
            return;
        }
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
            Toast.makeText(getActivity(), getString(R.string.new_act_start_day_required), Toast.LENGTH_LONG).show();
            return false;
        }
        if(to.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.new_act_end_day_required), Toast.LENGTH_LONG).show();
            return false;
        }
        Instant sd = ldStartDay.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant ed = ldEndDay.atStartOfDay(ZoneId.systemDefault()).toInstant();
        if(sd.toEpochMilli() > ed.toEpochMilli()) {
            Toast.makeText(getActivity(), getString(R.string.new_act_interval_date_error), Toast.LENGTH_LONG).show();
            return false;
        }
        //        customActivity.setsD(calStartDay.getTimeInMillis());
        customActivity.setsD(sd.toEpochMilli());
//        customActivity.seteD(calEndDay.getTimeInMillis());
        customActivity.seteD(ed.toEpochMilli());
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
            Toast.makeText(getActivity(), getString(R.string.new_act_must_add_time), Toast.LENGTH_LONG).show();
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
            Toast.makeText(getActivity(), getString(R.string.new_act_format_add_time), Toast.LENGTH_LONG).show();
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
            Toast.makeText(getActivity(), getString(R.string.new_act_must_choose_reg_type), Toast.LENGTH_LONG).show();
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
            customActivity.settT(2);
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
            Toast.makeText(getActivity(), getString(R.string.new_act_must_set_one_day), Toast.LENGTH_LONG).show();
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
            Toast.makeText(getActivity(), getString(R.string.new_act_must_add_time_for_a_day), Toast.LENGTH_LONG).show();
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
            Toast.makeText(getActivity(), getString(R.string.new_act_format_add_time_for_a_day), Toast.LENGTH_LONG).show();
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
            // Checks if end date is set
            String ed = binding.activityEndDate.getText().toString().trim();
            if(ed.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.new_act_format_add_time), Toast.LENGTH_LONG).show();
                return false;
            }
            Instant d = ldEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//            customActivity.seteD(calEndDate.getTimeInMillis());
            if(d.toEpochMilli() < CustomActivityHelper.todayMillis()) {
                Toast.makeText(getActivity(), getString(R.string.new_act_end_date_error), Toast.LENGTH_LONG).show();
                return false;
            }
            customActivity.seteD(d.toEpochMilli());
        }
        return true;
    }

    /**
     * Saves the new activity to local database.
     * @param fragView root view of fragment
     */
    private void addActivityToDb(View fragView) {
        mainViewModel.insertActivity(customActivity);
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
     * Initializes name chooser radio button group. If fix activity is selected, we show
     * activity spinner, if custom activity name is selected, an edit text becomes visible
     * and we are able to type the name we want.
     */
    private void initNameChooserRadioButtonGroup() {
        binding.selectActivityNameRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(binding.selectFixActivityOption.isChecked()) {
                    binding.selectFixActivitySpinner.setVisibility(View.VISIBLE);
                    binding.activityName.setVisibility(View.GONE);
                } else if(binding.selectCustomActivityOption.isChecked()) {
                    binding.selectFixActivitySpinner.setVisibility(View.GONE);
                    binding.activityName.setVisibility(View.VISIBLE);
                }
            }
        });
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
                    setSumTimePickerDefault();
                } else if(binding.activityMonthly.isChecked()) {
                    // Monthly
                    setWeeklyStuffGone();
                    binding.activityHasAnEndDate.setVisibility(View.VISIBLE);
                    setDurationGone();
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                    binding.durationText.setText(R.string.choose_monthly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePickerDefault();
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
                    setDurationGone();
                    binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                } else {
                    binding.activityWeeklyDays.setVisibility(View.GONE);
                    binding.allDaysOfWeek.setVisibility(View.GONE);
                    setDurationGone();
                    binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                    binding.durationText.setText(R.string.choose_one_weekly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePickerDefault();
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
                    setSumTimePickerDefault();
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                    // If it is regular monthly
                } else if(binding.activityMonthly.isChecked()) {
                    binding.durationText.setText(R.string.choose_monthly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePickerDefault();
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
                        setSumTimePickerDefault();
                    } else if(binding.activityIsInterval.isChecked()) { // Interval
                        binding.selectDurationType.setVisibility(View.VISIBLE);
                        binding.activityIsSumTime.setVisibility(View.VISIBLE);
                        binding.activityIsTime.setVisibility(View.VISIBLE);
                        binding.activityCustomTime.setVisibility(View.GONE);
                        binding.activityIsWeeklyTime.setVisibility(View.GONE);
                    } else if(binding.activityDaily.isChecked()) { // Regular - daily
                        binding.durationText.setText(R.string.choose_daily_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
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
                            setSumTimePickerDefault();
                        }
                    } else if(binding.activityMonthly.isChecked()) { // Regular - monthly
                        binding.durationText.setText(R.string.choose_monthly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
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
                        setSumTimePickerDefault();
                    } else {
                        binding.durationText.setText(R.string.choose_sum_time_for_fixed_week_days);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
                    }
                } else if(binding.activityIsTime.isChecked()) {
                    binding.fixedDaysTimes.setVisibility(View.GONE);
                    if(binding.activityIsInterval.isChecked()) {
                        binding.durationText.setText(R.string.choose_daily_time_for_interval);
                    } else {
                        binding.durationText.setText(R.string.choose_daily_time);
                    }
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePickerDefault();
                    binding.activitySumTimePicker.days.setEnabled(false);
                    binding.activitySumTimePicker.days.setBackgroundColor(Color.LTGRAY);
                    binding.activitySumTimePicker.days.setTypeface(null, Typeface.ITALIC);
                } else if(binding.activityCustomTime.isChecked()) {
                    binding.durationText.setVisibility(View.GONE);
                    binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
                    resetFixedDaysTimePickers();
                    binding.fixedDaysTimes.setVisibility(View.VISIBLE);
                } else if(binding.activityIsWeeklyTime.isChecked()) {
                    binding.fixedDaysTimes.setVisibility(View.GONE);
                    binding.durationText.setText(R.string.choose_one_weekly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePickerDefault();
                }
            }
        });
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

    /**
     * Initializes date picker dialogs and set on click listeners to these date picker dialogs.
     */
    private void initCalendars() {
        // Initializes date picker dialogs' onDateSetListener and after that it sets the UI according to
        // the new date
        DatePickerDialog.OnDateSetListener dateStartDay = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month++;
                ldStartDay = LocalDate.of(year, month, day);
                updateLabelStartDay();
            }
        };

        DatePickerDialog.OnDateSetListener dateEndDay = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month++;
                ldEndDay = LocalDate.of(year, month, day);
                updateLabelEndDay();
            }
        };

        DatePickerDialog.OnDateSetListener dateEndDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month++;
                ldEndDate = LocalDate.of(year, month, day);
                updateLabelEndDate();
            }
        };

        // Sets onClickListeners for date pickers
        binding.activityStartDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateStartDay,ldStartDay.getYear(),ldStartDay.getMonthValue()-1,ldStartDay.getDayOfMonth()).show();
            }
        });
        binding.activityEndDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateEndDay,ldEndDay.getYear(),ldEndDay.getMonthValue()-1,ldEndDay.getDayOfMonth()).show();
            }
        });
        binding.activityEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(),dateEndDate,ldEndDate.getYear(),ldEndDate.getMonthValue()-1,ldEndDate.getDayOfMonth()).show();
            }
        });
        initTodayDate();
    }

    /**
     * Initializes start day picker dialog for today date and shows on the UI as well.
     */
    private void initTodayDate() {
        ldStartDay = LocalDate.now();
        updateLabelStartDay();
        ldEndDay = LocalDate.now();
        updateLabelEndDay();
        ldEndDate = LocalDate.now();
        updateLabelEndDate();
    }

    /**
     * Displays start day's date.
     */
    private void updateLabelStartDay(){
        binding.activityStartDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                ldStartDay.getYear(),ldStartDay.getMonthValue(),ldStartDay.getDayOfMonth()));
    }

    /**
     * Displays end day's date.
     */
    private void updateLabelEndDay(){
        binding.activityEndDay.setText(DateConverter.makeDateStringForSimpleDateDialog(
                ldEndDay.getYear(),ldEndDay.getMonthValue(),ldEndDay.getDayOfMonth()));
    }

    /**
     * Displays end date's date.
     */
    private void updateLabelEndDate(){
        binding.activityEndDate.setText(DateConverter.makeDateStringForSimpleDateDialog(
                ldEndDate.getYear(),ldEndDate.getMonthValue(),ldEndDate.getDayOfMonth()));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}