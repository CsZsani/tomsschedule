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

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.CustomTimePickerForOneDayBinding;
import hu.janny.tomsschedule.databinding.FragmentAddCustomActivityBinding;
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
    int color = Color.rgb(255, 164, 119);

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
        prioritySpinnerListener();
        fixActivitySpinnerListener();

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding.activityColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPickerDialog.show();
            }
        });

        customActivity = new CustomActivity();

        initCalendars();
        initNameChooserRadioButtonGroup();
        initNotifyTypeRadioButtonGroup();
        initRegularityTypeRadioButtonGroup();
        initHasFixedDaysSwitch();
        initHasEndDateSwitch();
        initIsTimeMeasuredSwitch();
        initSelectDurationTypeRadioGroups();
        initFixedDaysTimePickerListeners();

        saveOnClickListener();

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

    private void fixActivitySpinnerListener() {
        binding.selectFixActivitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void saveOnClickListener() {
        binding.saveActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveActivity();
            }
        });
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

    private void saveActivity() {
        String name;
        if(binding.selectFixActivityOption.isChecked()) {
            name = getSelectedFixActivityName();
        } else {
            name = binding.activityName.getText().toString().trim();
        }
        if(name.isEmpty()) {
            binding.activityName.setError("Name is required");
            binding.activityName.requestFocus();
            return;
        }
        int col = color;
        String note = binding.activityNote.getText().toString().trim();
        int priority = Integer.parseInt(binding.activityPriority.getSelectedItem().toString());

        if(mainViewModel.getUser().getValue() != null) {
            customActivity = new CustomActivity(mainViewModel.getUser().getValue().uid, name, col, note, priority);
        } else {
            Toast.makeText(getActivity(), "Error is saving activity, no user detected!", Toast.LENGTH_LONG).show();
            return;
        }

        decideWhichMainType();
    }

    private void decideWhichMainType() {
        if(binding.activityHasDeadline.isChecked()) {
            String dl = binding.activityDeadline.getText().toString().trim();
            if(dl.isEmpty()) {
                binding.activityDeadline.setError("Date is required for deadline option!");
                binding.activityDeadline.requestFocus();
                return;
            }
            setDeadline(dl);
        } else if(binding.activityIsInterval.isChecked()) {
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
            setInterval(from, to);
        } else if(binding.activityCustom.isChecked()) {
            setNeither();
        } else {
            setRegularity();
        }
        addActivityToDb();
    }

    private void setDeadline(String date) {
        customActivity.setDl(DateConverter.stringFromSimpleDateDialogToLongMillis(date));
    }

    private void setInterval(String from, String to) {
        customActivity.setsD(DateConverter.stringFromSimpleDateDialogToLongMillis(from));
        customActivity.seteD(DateConverter.stringFromSimpleDateDialogToLongMillis(to));
    }

    private void setNeither() {

    }

    private void setRegularity() {

    }

    private void addActivityToDb() {

    }

    private String getSelectedFixActivityName() {
        String d = binding.selectFixActivitySpinner.getSelectedItem().toString().trim();
        switch (d) {
            case "Sleeping":
            case "Alvás":
                return "SLEEPING";
            case "Cooking":
            case "Főzés":
                return "COOKING";
            case "Workout":
            case "Edzés":
                return "WORKOUT";
            case "Housework":
            case "Házimunka":
                return "HOUSEWORK";
            case "Shopping":
            case "Bevásárlás":
                return "SHOPPING";
            case "Work":
            case "Munka":
                return "WORK";
            case "School":
            case "Iskola":
                return "SCHOOL";
            case "Learning":
            case "Tanulás":
                return "LEARNING";
            case "Travelling":
            case "Utazás":
                return "TRAVELLING";
            case "Hobby":
            case "Hobbi":
                return "HOBBY";
            case "Relaxation":
            case "Kikapcsolódás":
                return "RELAXATION";
            case "Reading":
            case "Olvasás":
                return "READING";
        }
        return "ERROR";
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

    private void initNameChooserRadioButtonGroup() {
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
    }

    private void initNotifyTypeRadioButtonGroup() {
        binding.selectNotifTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.activityHasDeadline:
                        setDeadlineVisible();
                        setIntervalGone();
                        setRegularityGone();
                        setEndDateGone();
                        setDurationGone();
                        setRegularityRadiosFalse();
                        binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                        break;
                    case R.id.activityRegularity:
                        setIntervalGone();
                        setDeadlineGone();
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
                        setDeadlineGone();
                        setEndDateGone();
                        setRegularityGone();
                        setDurationGone();
                        setRegularityRadiosFalse();
                        binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
                        break;
                    case R.id.activityCustom:
                        setIntervalGone();
                        setDeadlineGone();
                        setEndDateGone();
                        setRegularityGone();
                        setDurationGone();
                        setRegularityRadiosFalse();
                        binding.activityIsTimeMeasured.setVisibility(View.VISIBLE);
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
                        setSumTimePickerDefault();
                        break;
                    case R.id.activityMonthly:
                        setWeeklyStuffGone();
                        binding.activityHasAnEndDate.setVisibility(View.VISIBLE);
                        setDurationGone();
                        binding.activityIsTimeMeasured.setVisibility(View.GONE);
                        binding.durationText.setText(R.string.choose_monthly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
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
                    setSumTimePickerDefault();
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
                    setSumTimePickerDefault();
                    binding.activityIsTimeMeasured.setVisibility(View.GONE);
                } else if(binding.activityMonthly.isChecked()) {
                    binding.durationText.setText(R.string.choose_monthly_time);
                    binding.durationText.setVisibility(View.VISIBLE);
                    setSumTimePickerDefault();
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
                        binding.durationText.setText(R.string.choose_daily_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
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
                            setSumTimePickerDefault();
                        }
                    } else if(binding.activityMonthly.isChecked()) {
                        binding.durationText.setText(R.string.choose_monthly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
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
                            setSumTimePickerDefault();
                        } else {
                            binding.durationText.setText(R.string.choose_sum_time_for_fixed_week_days);
                            binding.durationText.setVisibility(View.VISIBLE);
                            setSumTimePickerDefault();
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
                        setSumTimePickerDefault();
                        binding.activitySumTimePicker.days.setEnabled(false);
                        binding.activitySumTimePicker.days.setBackgroundColor(Color.LTGRAY);
                        binding.activitySumTimePicker.days.setTypeface(null, Typeface.ITALIC);
                        break;
                    case R.id.activityCustomTime:
                        binding.durationText.setVisibility(View.GONE);
                        binding.activitySumTimePicker.getRoot().setVisibility(View.GONE);
                        resetFixedDaysTimePickers();
                        binding.fixedDaysTimes.setVisibility(View.VISIBLE);
                        break;
                    case R.id.activityIsWeeklyTime:
                        binding.fixedDaysTimes.setVisibility(View.GONE);
                        binding.durationText.setText(R.string.choose_one_weekly_time);
                        binding.durationText.setVisibility(View.VISIBLE);
                        setSumTimePickerDefault();
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
        binding.activityEndDate.setOnClickListener(new View.OnClickListener() {
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

}