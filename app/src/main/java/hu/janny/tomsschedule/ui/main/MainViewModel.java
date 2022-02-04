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
import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.model.repository.Repository;
import hu.janny.tomsschedule.model.repository.UserRepository;

public class MainViewModel extends AndroidViewModel {

    private final Repository repository;
    private final UserRepository userRepository;
    private final LiveData<Map<CustomActivity, List<ActivityTime>>> allActivitiesWithTimes;
    private final LiveData<List<CustomActivity>> allActivitiesList;
    private final MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activityWithTimes;
    private final LiveData<User> user;
    private final LiveData<List<CustomActivity>> activitiesList;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        userRepository = new UserRepository(application);
        allActivitiesWithTimes = repository.getAllActivitiesWithTimes();
        activityWithTimes = repository.getActivitiesWithTimesData();
        activitiesList = repository.getActivities();
        allActivitiesList = Transformations.map(repository.getAllActivitiesWithTimes(), new Deserializer());
        user = userRepository.getCurrentUser();
    }

    private class Deserializer implements Function<Map<CustomActivity, List<ActivityTime>>, List<CustomActivity>> {
        @Override
        public List<CustomActivity> apply(Map<CustomActivity, List<ActivityTime>> liveData) {
            System.out.println(liveData.entrySet().toString());
            List<CustomActivity> list = new ArrayList<>(liveData.keySet());
            return list;
        }
    }

    public void logoutUserInDb(User user) {
        userRepository.updateUser(user);
    }

    public LiveData<Map<CustomActivity, List<ActivityTime>>> getAllActivitiesWithTimes() {
        return allActivitiesWithTimes;
    }

    public LiveData<List<CustomActivity>> getAllActivitiesInList() {
        return allActivitiesList;
    }

    public MutableLiveData<Map<CustomActivity, List<ActivityTime>>> getActivityWithTimes() {
        return activityWithTimes;
    }

    public long insertActivity(CustomActivity customActivity) {
        return repository.insertActivity(customActivity);
    }

    public void insertActivityTime(ActivityTime activityTime) {
        repository.insertTime(activityTime);
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<List<CustomActivity>> getActivitiesList() {
        return activitiesList;
    }

    public int getIdByName(String name) {
        return repository.getActivityIdByName(name);
    }
}
