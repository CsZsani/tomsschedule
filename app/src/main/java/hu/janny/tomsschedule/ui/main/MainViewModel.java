package hu.janny.tomsschedule.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.repository.Repository;

public class MainViewModel extends AndroidViewModel {

    private Repository repository;
    private LiveData<Map<CustomActivity, List<ActivityTime>>> allActivitiesWithTimes;
    private LiveData<List<CustomActivity>> allActivitiesList;
    private MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activityWithTimes;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        allActivitiesWithTimes = repository.getAllActivitiesWithTimes();
        activityWithTimes = repository.getActivitiesWithTimesData();
        allActivitiesList = Transformations.map(repository.getAllActivitiesWithTimes(), new Deserializer());

    }

    private GenericTypeIndicator<List<CustomActivity>> typeIndicator = new GenericTypeIndicator<List<CustomActivity>>() {};

    private class Deserializer implements Function<Map<CustomActivity, List<ActivityTime>>, List<CustomActivity>> {
        @Override
        public List<CustomActivity> apply(Map<CustomActivity, List<ActivityTime>> liveData) {
            List<CustomActivity> list = new ArrayList<>(liveData.keySet());
            return list;
        }
    }

    public LiveData<Map<CustomActivity, List<ActivityTime>>> getAllActivitiesWithTimes() {
        return allActivitiesWithTimes;
    }

    public LiveData<List<CustomActivity>> getAllActivitiesWithTimesInList() {
        return allActivitiesList;
    }

    public MutableLiveData<Map<CustomActivity, List<ActivityTime>>> getActivityWithTimes() {
        return activityWithTimes;
    }

    public void insertActivity(CustomActivity customActivity) {
        repository.insertActivity(customActivity);
    }
}
