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

    // An activity with its times - edit, detail, add time
    private final MutableLiveData<ActivityWithTimes> activityByIdWithTimesEntity;
    // An activity (without times) - timer
    private final MutableLiveData<CustomActivity> singleActivity;
    // The user who is signed in
    private final LiveData<User> user;

    private final LiveData<Map<CustomActivity, List<ActivityTime>>> allActivitiesWithTimes;
    private final LiveData<List<CustomActivity>> allActivitiesList;
    private final MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activityWithTimes;
    private final MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activityByIdWithTimes;

    private final LiveData<List<CustomActivity>> activitiesList;
    private final LiveData<List<CustomActivity>> activitiesListEntities;
    private final LiveData<List<ActivityWithTimes>> activitiesWithTimesList;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        userRepository = new UserRepository(application);
        user = userRepository.getCurrentUser();
        activityByIdWithTimesEntity = repository.getActivityWithTimesEntity();
        singleActivity = repository.getActivitiesData();


        allActivitiesWithTimes = repository.getAllActivitiesWithTimes();
        activityWithTimes = repository.getActivitiesWithTimesData();
        activitiesList = repository.getActivities();
        allActivitiesList = Transformations.map(repository.getAllActivitiesWithTimes(), new Deserializer());
        activityByIdWithTimes = repository.getActivityByIdWithTimesData();
        activitiesListEntities = Transformations.map(repository.getActivitiesWithTimesEntities(), new DeserializerSecond());
        activitiesWithTimesList = Transformations.map(repository.getActivitiesWithTimesEntities(), new DeserializerThird());
    }

    private class Deserializer implements Function<Map<CustomActivity, List<ActivityTime>>, List<CustomActivity>> {
        @Override
        public List<CustomActivity> apply(Map<CustomActivity, List<ActivityTime>> liveData) {
            List<CustomActivity> list = new ArrayList<>(liveData.keySet());
            List<CustomActivity> filter = list.stream().filter(ca -> ca.getUserId().equals(FirebaseManager.user.getUid())).collect(Collectors.toList());
            return filter;
        }
    }

    private class DeserializerSecond implements Function<List<ActivityWithTimes>, List<CustomActivity>> {
        @Override
        public List<CustomActivity> apply(List<ActivityWithTimes> liveData) {
            List<CustomActivity> list = new ArrayList<>();
            for (ActivityWithTimes at : liveData) {
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
     *
     * @param customActivity the activity to be inserted
     */
    public void insertActivity(CustomActivity customActivity) {
        repository.insertActivity(customActivity);
    }

    /**
     * Updates the given activity in the local database.
     *
     * @param customActivity the activity to be updated
     */
    public void updateActivity(CustomActivity customActivity) {
        repository.updateActivity(customActivity);
    }

    /**
     * Updates an activity time if it belongs to a fix activity. It actually inserts if there is no time added for that activity and day,
     * and updates if we have added a time to that day before. If an exception occurs during database
     * transaction it returns 0, if we inserted then 1, and if we updated then 2.
     *
     * @param activityTime the time to be updated (or inserted)
     * @return 0 if exception occurred, 1 if we inserted and if we updated
     */
    public int insertOrUpdateTime(ActivityTime activityTime) {
        boolean result = false;
        try {
            result = repository.updateOrInsertTime(activityTime);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
        if (result) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Updates an activity time if it does not belong to a fix activity. It actually inserts if there is no time added for that activity and day,
     *      * and updates if we have added a time to that day before.
     * @param activityTime the time to be updated (or inserted)
     */
    public void insertOrUpdateTimeSingle(ActivityTime activityTime) {
        repository.updateOrInsertTimeSingle(activityTime);
    }

    /**
     * Deletes the activity by the given id.
     *
     * @param id id of the activity to be deleted
     */
    public void deleteActivityById(long id) {
        repository.deleteActivityById(id);
    }

    //********//
    // Search //
    //********//

    // edit, details, add time

    /**
     * Searches an activity with its times by its id.
     *
     * @param id id of the activity we search
     */
    public void findActivityByIdWithTimesEntity(long id) {
        repository.getSingleActivityByIdWithTimesEntity(id);
    }

    // timer

    /**
     * Searches an activity by its id.
     *
     * @param id id of the activity we search
     */
    public void findActivityById(long id) {
        repository.getActivityById(id);
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

    public void deleteActivityTimesByActivityId(long id) {
        repository.deleteTimesByActivityId(id);
    }



    //*********//
    // Getters //
    //*********//

    // edit, detail, add time

    /**
     * Returns an activity with its times. We searched it with findActivityByIdWithTimesEntity method.
     *
     * @return LiveData ActivityWithTimes, an activity with its times
     */
    public MutableLiveData<ActivityWithTimes> getActivityByIdWithTimesEntity() {
        return activityByIdWithTimesEntity;
    }

    // timer

    /**
     * Returns an activity. We searched it with findActivityById method.
     *
     * @return LiveData CustomActivity, an activity
     */
    public MutableLiveData<CustomActivity> getSingleActivity() {
        return singleActivity;
    }

    /**
     * Returns the signed in user. Its isLoggedIn field is true.
     *
     * @return the signed in user in LiveData
     */
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

    public MutableLiveData<Map<CustomActivity, List<ActivityTime>>> getActivityByIdWithTimes() {
        return activityByIdWithTimes;
    }


}
