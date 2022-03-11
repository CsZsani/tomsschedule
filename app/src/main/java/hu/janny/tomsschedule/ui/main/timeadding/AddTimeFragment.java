package hu.janny.tomsschedule.ui.main.timeadding;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.FragmentAddTimeBinding;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.ActivityWithTimes;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.viewmodel.MainViewModel;

public class AddTimeFragment extends Fragment {

    public static final String OPERATION_TYPE = "plus";
    public static final String ITEM_ID = "item_id";
    public static final String ACTIVITY_NAME = "name";

    private MainViewModel mainViewModel;
    private FragmentAddTimeBinding binding;

    private long activityId;
    private boolean isAdd;

    private User currentUser;
    private CustomActivity customActivity;
    // ActivityTime list - needed to examine the validation of time during subtraction
    private List<ActivityTime> times;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private final Calendar calendar = Calendar.getInstance();
    private long dateMillis = 0L;
    private int hour = 0, minute = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Binds layout
        binding = FragmentAddTimeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Gets a MainViewModel instance
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        if (getArguments() != null && getArguments().containsKey(ITEM_ID)) {
            activityId = getArguments().getLong(ITEM_ID);
            mainViewModel.findActivityById(activityId);
        } else {
            Navigation.findNavController(root).popBackStack();
        }

        if (getArguments().containsKey(OPERATION_TYPE)) {
            isAdd = getArguments().getBoolean(OPERATION_TYPE);
        }

        // Observer of the activity to get the data from the database
        mainViewModel.getActivityByIdWithTimesEntity().observe(getViewLifecycleOwner(), new Observer<ActivityWithTimes>() {
            @Override
            public void onChanged(ActivityWithTimes activityWithTimes) {
                CustomActivity activity = null;
                if (activityWithTimes != null) {
                    activity = activityWithTimes.customActivity;
                    if (activityWithTimes.activityTimes != null && !activityWithTimes.activityTimes.isEmpty()) {
                        times = new ArrayList<>(activityWithTimes.activityTimes);
                    } else {
                        times = new ArrayList<>();
                    }
                }
                if (activity != null) {
                    customActivity = activity;
                } else {
                    Navigation.findNavController(root).popBackStack();
                    Toast.makeText(getActivity(), getString(R.string.add_time_no_activity), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Observer of the current user
        mainViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUser = user;
            }
        });

        // Initializes date and time picker dialogs
        initDatePicker();
        initTimePicker();

        setUpDate();
        setUpTime();
        setUpSave(root);

        return root;
    }

    /**
     * Sets up save button which cals savingTime method.
     *
     * @param fragView root view of the fragment
     */
    private void setUpSave(View fragView) {
        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savingTime(fragView);
            }
        });
    }

    /**
     * Sets up time picker field which opens time picker dialog.
     */
    private void setUpTime() {
        binding.time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerDialog.show();
            }
        });
    }

    /**
     * Sets up date picker field which opens date picker dialog.
     */
    private void setUpDate() {
        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
    }

    /**
     * Saves the given time to the given date for the activity. First it checks whether every data is
     * provided or not.
     *
     * @param fragView root view of the fragment
     */
    private void savingTime(View fragView) {
        String d = binding.date.getText().toString().trim();
        String t = binding.time.getText().toString().trim();

        if (d.isEmpty() || dateMillis == 0L) {
            Toast.makeText(getContext(), getString(R.string.add_time_date_required), Toast.LENGTH_LONG).show();
            return;
        }

        if (t.isEmpty() || (hour == 0 && minute == 0)) {
            Toast.makeText(getContext(), getString(R.string.add_time_time_required), Toast.LENGTH_LONG).show();
            return;
        }
        long time = DateConverter.durationTimeConverterFromIntToLongForDays(hour, minute);
        ActivityTime activityTime = new ActivityTime(activityId, dateMillis, time);
        // * -1 if we want to subtract the time amount
        if (!isAdd) {
            activityTime.setT(-activityTime.getT());
        }
        checkingSubtraction(activityTime);

        saveIntoDatabase(activityTime);

        Navigation.findNavController(fragView).popBackStack();
    }

    /**
     * Checks whether the given amount of time will not decrease the time amount in database under 0.
     *
     * @param activityTime the time to be updated
     */
    private void checkingSubtraction(ActivityTime activityTime) {

    }

    /**
     * Saves the time into database, local and Firebase. Then updates the activity in fields of soFar,
     * remaining and allTime. If the activity is fix, then we update in the Firebase.
     *
     * @param activityTime the time with which we want to update
     */
    private void saveIntoDatabase(ActivityTime activityTime) {
        // Fix activity - we have to wait that it was an insert or an update.
        // Insert - we add the time and increase the user count with one
        // Update - we add just the time
        if (CustomActivityHelper.isFixActivity(customActivity.getName())) {
            int isInsert = mainViewModel.insertOrUpdateTime(activityTime);
            // If the insertOrUpdate was not successful (0), we try again
            while (isInsert == 0) {
                isInsert = mainViewModel.insertOrUpdateTime(activityTime);
            }
            // 1 means - it was insert, 2 means - it wan update
            if (isInsert == 1) {
                // add to Firebase
                FirebaseManager.saveInsertedActivityTimeToFirebase(activityTime, customActivity.getName(), currentUser);
            } else if (isInsert == 2) {
                // update in Firebase
                FirebaseManager.saveUpdateActivityTimeToFirebase(activityTime, customActivity.getName(), currentUser);
            }
        } else {
            // No fix activity - we just simply update in local database
            mainViewModel.insertOrUpdateTimeSingle(activityTime);
        }
        // Informs the user about what amount of time was added
        if (isAdd) {
            Toast.makeText(getContext(), String.format(Locale.getDefault(), "+%02d:%02d", hour, minute), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), String.format(Locale.getDefault(), "-%02d:%02d", hour, minute), Toast.LENGTH_LONG).show();
        }
        // Updates the activity by soFar, remaining and allTime
        CustomActivityHelper.updateActivity(customActivity, activityTime);
        mainViewModel.updateActivity(customActivity);
    }

    /**
     * Initializes date picker dialog for choosing date.
     */
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.clear();
                calendar.set(year, month, day);
                dateMillis = calendar.getTimeInMillis();
                // Months counting begins with 0
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

    /**
     * Initializes time picker dialog.
     */
    private void initTimePicker() {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                hour = hours;
                minute = minutes;
                binding.time.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
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

}