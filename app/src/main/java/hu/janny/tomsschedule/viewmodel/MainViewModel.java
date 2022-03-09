package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.ActivityWithTimes;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.model.repository.Repository;
import hu.janny.tomsschedule.model.repository.UserRepository;

/**
 * This view model is for doing the repository changes connected with activities
 * and providing data about activities and times.
 */
public class MainViewModel extends AndroidViewModel {

    private final Repository repository;
    private final UserRepository userRepository;
    private final LiveData<Map<CustomActivity, List<ActivityTime>>> allActivitiesWithTimes;
    private final LiveData<List<CustomActivity>> allActivitiesList;
    private final MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activityWithTimes;
    private final MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activityByIdWithTimes;
    private final MutableLiveData<ActivityWithTimes> activityByIdWithTimesEntity;
    private final MutableLiveData<CustomActivity> singleActivity;
    private final LiveData<User> user;
    private final LiveData<List<CustomActivity>> activitiesList;
    private final LiveData<List<CustomActivity>> activitiesListEntities;
    private final LiveData<List<ActivityWithTimes>> activitiesWithTimesList;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        userRepository = new UserRepository(application);
        user = userRepository.getCurrentUser();
        allActivitiesWithTimes = repository.getAllActivitiesWithTimes();
        activityWithTimes = repository.getActivitiesWithTimesData();
        activitiesList = repository.getActivities();
        allActivitiesList = Transformations.map(repository.getAllActivitiesWithTimes(), new Deserializer());
        activityByIdWithTimes = repository.getActivityByIdWithTimesData();
        activityByIdWithTimesEntity = repository.getActivityWithTimesEntity();
        singleActivity = repository.getActivitiesData();
        activitiesListEntities = Transformations.map(repository.getActivitiesWithTimesEntities(), new DeserializerSecond());
        activitiesWithTimesList = Transformations.map(repository.getActivitiesWithTimesEntities(), new DeserializerThird());
    }

    private class Deserializer implements Function<Map<CustomActivity, List<ActivityTime>>, List<CustomActivity>> {
        @Override
        public List<CustomActivity> apply(Map<CustomActivity, List<ActivityTime>> liveData) {
            List<CustomActivity> list = new ArrayList<>(liveData.keySet());
            List<CustomActivity> filter = list.stream().filter(ca -> ca.getUserId().equals(FirebaseManager.user.getUid())).collect(Collectors.toList());
            //List<CustomActivity> filter = list.stream().filter(ca -> ca.getUserId().equals(user.getValue().getUid())).collect(Collectors.toList());
            return filter;
        }
    }

    private class DeserializerSecond implements Function<List<ActivityWithTimes>, List<CustomActivity>> {
        @Override
        public List<CustomActivity> apply(List<ActivityWithTimes> liveData) {
            //System.out.println(liveData);
            List<CustomActivity> list = new ArrayList<>();
            for(ActivityWithTimes at : liveData) {
                list.add(at.customActivity);
            }
            List<CustomActivity> listFiltered = list.stream().filter(ca -> ca.getUserId().equals(FirebaseManager.user.getUid())).collect(Collectors.toList());
            return listFiltered;
        }
    }

    private class DeserializerThird implements Function<List<ActivityWithTimes>, List<ActivityWithTimes>> {
        @Override
        public List<ActivityWithTimes> apply(List<ActivityWithTimes> liveData) {
            List<ActivityWithTimes> listFiltered = liveData.stream().filter(ca -> ca.customActivity.getUserId().equals(FirebaseManager.user.getUid())).collect(Collectors.toList());
            return listFiltered;
        }
    }

    //************************//
    // Insert, update, delete //
    //************************//

    /**
     * Inserts the given activity into the local database.
     * @param customActivity the activity to be inserted
     */
    public void insertActivity(CustomActivity customActivity) {
        repository.insertActivity(customActivity);
    }

    /**
     * Updates the given activity in the local database.
     * @param customActivity the activity to be updated
     */
    public void updateActivity(CustomActivity customActivity) {
        repository.updateActivity(customActivity);
    }

    //********//
    // Search //
    //********//

    public void logoutUserInDb(User user) {
        userRepository.updateUser(user);
    }

    public LiveData<Map<CustomActivity, List<ActivityTime>>> getAllActivitiesWithTimes() {
        return allActivitiesWithTimes;
    }

    public LiveData<List<CustomActivity>> getAllActivitiesInList() {
        return allActivitiesList;
    }

    public LiveData<List<CustomActivity>> getActivitiesListEntities() {
        return activitiesListEntities;
    }

    public LiveData<List<ActivityWithTimes>> getActivitiesWithTimesList() {
        return activitiesWithTimesList;
    }

    public MutableLiveData<Map<CustomActivity, List<ActivityTime>>> getActivityWithTimes() {
        return activityWithTimes;
    }



    public void insertActivityTime(ActivityTime activityTime) {
        repository.insertTime(activityTime);
    }


    public void deleteActivityById(long id) {
        repository.deleteActivityById(id);
    }

    public void deleteActivityTimesByActivityId(long id) {
        repository.deleteTimesByActivityId(id);
    }

    public int insertOrUpdateTime(ActivityTime activityTime) {
        boolean result = false;
        try {
            result = repository.updateOrInsertTime(activityTime);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
        if(result) {
            return 1;
        } else {
            return 2;
        }
    }

    public void insertOrUpdateTimeSingle(ActivityTime activityTime) {
        repository.updateOrInsertTimeSingle(activityTime);
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

    public void findActivityByIdWithTimes(long id) {
        repository.getSingleActivityByIdWithTimes(id);
    }

    public void findActivityByIdWithTimesEntity(long id) {
        repository.getSingleActivityByIdWithTimesEntity(id);
    }

    public void findActivityById(long id) {
        repository.getActivityById(id);
    }

    public MutableLiveData<Map<CustomActivity, List<ActivityTime>>> getActivityByIdWithTimes() {
        return activityByIdWithTimes;
    }

    public MutableLiveData<ActivityWithTimes> getActivityByIdWithTimesEntity() {
        return activityByIdWithTimesEntity;
    }

    public MutableLiveData<CustomActivity> getSingleActivity() {
        return singleActivity;
    }
}
