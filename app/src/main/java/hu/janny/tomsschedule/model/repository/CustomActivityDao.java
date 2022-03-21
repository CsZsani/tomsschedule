package hu.janny.tomsschedule.model.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import hu.janny.tomsschedule.model.helper.ActivityFilter;
import hu.janny.tomsschedule.model.entities.ActivityWithTimes;
import hu.janny.tomsschedule.model.entities.CustomActivity;

@Dao
public interface CustomActivityDao {

    /**
     * Inserts a new activity into the database.
     *
     * @param customActivity the activity to be inserted
     */
    @Insert
    void insertActivity(CustomActivity customActivity);

    /**
     * Inserts all the activities from the given list.
     *
     * @param activityList list of activities to be inserted
     */
    @Transaction
    @Insert
    void insertAll(List<CustomActivity> activityList);

    /**
     * Updates the given activity in local database.
     *
     * @param customActivity the activity to be updated
     */
    @Update
    void updateActivity(CustomActivity customActivity);

    /**
     * Deletes the activities and their times of the user with the given id.
     *
     * @param uid id of the user whose activities will be deleted
     */
    @Transaction
    @Query("DELETE FROM customactivities WHERE userId = :uid")
    void deleteActivityByUserId(String uid);

    /**
     * Deletes an activity based on its id.
     *
     * @param id the id of the activity we want to delete
     */
    @Query("DELETE FROM customactivities WHERE activityId = :id")
    void deleteActivityById(long id);

    /**
     * Gets an activity based on id.
     *
     * @param id id of the activity we search for
     * @return the activity with the given id
     */
    @Query("SELECT * FROM customactivities WHERE customactivities.activityId = :id")
    CustomActivity getActivityById(long id);

    /**
     * Searches an activity with its times based on its id.
     *
     * @param id id of the activity we search for
     * @return the activity with times with the given id
     */
    @Transaction
    @Query("SELECT * FROM customactivities WHERE activityId = :id")
    ActivityWithTimes getActivityWithTimesEntity(long id);

    /**
     * Returns the list of activities with their times ordered by priority (desc), remaining (desc) and last day added (asc).
     *
     * @return the list of activities with their time in LiveData
     */
    @Transaction
    @Query("SELECT * FROM customactivities ORDER BY customactivities.priority DESC, customactivities.remaining DESC, customactivities.lastDayAdded ASC")
    LiveData<List<ActivityWithTimes>> getActivitiesWithTimes();

    /**
     * Returns the list of activities with their times (basically for creating backup). It gives back
     * the activities that belong to the user with the given uid.
     *
     * @param uid id of the user
     * @return list of activities with their times
     */
    @Transaction
    @Query("SELECT * FROM customactivities WHERE userId == :uid ")
    List<ActivityWithTimes> getActivitiesWithTimesForBackup(String uid);

    /**
     * Returns the list of activities the given user has to filter.
     *
     * @param uid the uid of the user
     * @return the list of activities to filter
     */
    @Query("SELECT activityId, name, color from customactivities WHERE userId = :uid")
    LiveData<List<ActivityFilter>> getActivityFilter(String uid);

}
