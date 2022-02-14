package hu.janny.tomsschedule.ui.main.timeadding;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.DetailFragmentBinding;
import hu.janny.tomsschedule.databinding.FragmentAddTimeBinding;
import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.DateConverter;
import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.ui.main.MainViewModel;

public class AddTimeFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String OPERATION_TYPE = "plus";
    public static final String ITEM_ID = "item_id";
    public static final String ACTIVITY_NAME = "name";

    private MainViewModel mainViewModel;
    private FragmentAddTimeBinding binding;
    private long activityId;
    private boolean isAdd;
    private String activityName;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private Calendar calendar = Calendar.getInstance();
    private long todayMillis = 0L;
    private int hour = 0, minute = 0;
    private User currentUser;

    public static AddTimeFragment newInstance() {
        return new AddTimeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        binding = FragmentAddTimeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments().containsKey(ITEM_ID)) {
            activityId = getArguments().getLong(ITEM_ID);
        }
        if (getArguments().containsKey(ACTIVITY_NAME)) {
            activityName = getArguments().getString(ACTIVITY_NAME);
        }
        if (getArguments().containsKey(OPERATION_TYPE)) {
            isAdd = getArguments().getBoolean(OPERATION_TYPE);
        }

        initDatePicker();
        initTimePicker();

        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        binding.time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerDialog.show();
            }
        });

        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savingTime(root);
            }
        });

        mainViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUser = user;
            }
        });

        return root;
    }

    private void savingTime(View fragView) {
        String d = binding.date.getText().toString().trim();
        String t = binding.time.getText().toString().trim();

        if(d.isEmpty() || todayMillis == 0L) {
            binding.date.setError("Date is required!");
            binding.date.requestFocus();
            return;
        }

        if(t.isEmpty() || (hour == 0 && minute == 0)) {
            binding.time.setError("Time amount is required!");
            binding.time.requestFocus();
            return;
        }
        long time = DateConverter.durationTimeConverterFromIntToLongForDays(hour, minute);
        ActivityTime activityTime = new ActivityTime(activityId, todayMillis, time);
        if(!isAdd) {
            activityTime.setT(-activityTime.getT());
        }

        if(CustomActivityHelper.isFixActivity(activityName)) {
            int isInsert = mainViewModel.insertOrUpdateTime(activityTime);
            while(isInsert == 0) {
                isInsert = mainViewModel.insertOrUpdateTime(activityTime);
            }
            if(isInsert == 1) {
                // add to Firebase
                FirebaseManager.saveInsertedActivityTimeToFirebase(activityTime, activityName, currentUser);
            } else if(isInsert == 2){
                // update in Firebase
                FirebaseManager.saveUpdateActivityTimeToFirebase(activityTime, activityName, currentUser);
            }
        } else {
            mainViewModel.insertOrUpdateTimeSingle(activityTime);
        }
        if(isAdd) {
            Toast.makeText(getContext(), String.format(Locale.getDefault(),"+%02d:%02d", hour, minute), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), String.format(Locale.getDefault(),"-%02d:%02d", hour, minute), Toast.LENGTH_LONG).show();
        }

        Navigation.findNavController(fragView).popBackStack();
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.clear();
                calendar.set(year, month, day);
                todayMillis = calendar.getTimeInMillis();
                month = month + 1;
                String date = DateConverter.makeDateStringForSimpleDateDialog(day, month, year);
                binding.date.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(getActivity(), style, dateSetListener, year, month, day);
        datePickerDialog.setTitle(R.string.select_date);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    private void initTimePicker() {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                hour = hours;
                minute = minutes;
                binding.time.setText(String.format(Locale.getDefault(),"%02d:%02d", hour, minute));
            }
        };
        int style = AlertDialog.THEME_HOLO_LIGHT;
        timePickerDialog = new TimePickerDialog(getContext(), style, onTimeSetListener, hour, minute, true);
        timePickerDialog.setTitle(R.string.select_time);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}