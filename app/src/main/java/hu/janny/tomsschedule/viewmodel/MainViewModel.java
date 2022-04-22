package hu.janny.tomsschedule.viewmodel;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.ActivityWithTimes;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
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
    // All the activities with their times - home
    private final LiveData<List<ActivityWithTimes>> activitiesWithTimesList;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        userRepository = new UserRepository(application);

        user = userRepository.getCurrentUser();
        activityByIdWithTimesEntity = repository.getActivityWithTimesEntity();
        singleActivity = repository.getActivitiesData();
        activitiesWithTimesList = Transformations.map(repository.getActivitiesWithTimesEntities(), new DeserializerOfActivities());
    }

    /**
     * Filters the activity list ot show just those that belong to the current user.
     */
    private static class DeserializerOfActivities implements Function<List<ActivityWithTimes>, List<ActivityWithTimes>> {
        @Override
        public List<ActivityWithTimes> apply(List<ActivityWithTimes> liveData) {
            return liveData.stream().filter(ca -> ca.customActivity.getUserId().equals(FirebaseManager.user.getUid())).collect(Collectors.toList());
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
     * * and updates if we have added a time to that day before.
     *
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

    /**
     * Saves the time into database, local and Firebase. Then updates the activity in fields of soFar,
     * remaining and allTime. If the activity is fix, then we update in the Firebase.
     *
     * @param activityTime   the time with which we want to update
     * @param customActivity the activity we will update
     * @param currentUser    the logged in user - for saving time into Firebase
     */
    public void saveIntoDatabase(ActivityTime activityTime, CustomActivity customActivity, User currentUser) {
        // Fix activity - we have to wait that it was an insert or an update.
        // Insert - we add the time and increase the user count with one
        // Update - we add just the time
        if (CustomActivityHelper.isFixActivity(customActivity.getName())) {
            int isInsert = insertOrUpdateTime(activityTime);
            // If the insertOrUpdate was not successful (0), we try again
            while (isInsert == 0) {
                isInsert = insertOrUpdateTime(activityTime);
            }
            // 1 means - it was insert, 2 means - it wan update
            if (currentUser != null) {
                if (isInsert == 1) {
                    // add to Firebase
                    FirebaseManager.saveInsertedActivityTimeToFirebase(activityTime, customActivity.getName(), currentUser);
                } else if (isInsert == 2) {
                    // update in Firebase
                    FirebaseManager.saveUpdateActivityTimeToFirebase(activityTime, customActivity.getName(), currentUser);
                }
            }
        } else {
            // No fix activity - we just simply update in local database
            insertOrUpdateTimeSingle(activityTime);
        }
        // Updates the activity by soFar, remaining and allTime
        CustomActivityHelper.updateActivity(customActivity, activityTime);
        updateActivity(customActivity);
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

    // home

    /**
     * Returns the list of activities with their times.
     *
     * @return the list of activities with their time in LiveData
     */
    public LiveData<List<ActivityWithTimes>> getActivitiesWithTimesList() {
        return activitiesWithTimesList;
    }

}
