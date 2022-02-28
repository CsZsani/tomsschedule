package hu.janny.tomsschedule.model.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;
import java.util.Map;

import hu.janny.tomsschedule.model.ActivityFilter;
import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.ActivityWithTimes;
import hu.janny.tomsschedule.model.CustomActivity;

@Dao
public interface CustomActivityDao {

    @Insert
    long insertActivity(CustomActivity customActivity);

    @Transaction
    @Insert
    void insertAll(List<CustomActivity> activityList);

    @Update
    void updateActivity(CustomActivity customActivity);

    @Delete
    void deleteActivity(CustomActivity customActivity);

    @Query("DELETE FROM customactivities WHERE name = :name")
    void deleteActivityByName(String name);

    @Query("DELETE FROM customactivities WHERE activityId = :id")
    void deleteActivityById(long id);

    @Transaction
    @Query("DELETE FROM customactivities WHERE userId = :id")
    void deleteActivityByUserId(String id);

    @Query("SELECT * FROM customactivities")
    LiveData<List<CustomActivity>> getActivitiesList();

    @Transaction
    @Query("SELECT * FROM customactivities JOIN activitytimes ON customactivities.activityId = activitytimes.actId")
    LiveData<Map<CustomActivity, List<ActivityTime>>> getAllActivitiesWithTimes();

    @Transaction
    @Query("SELECT * FROM customactivities JOIN activitytimes ON customactivities.activityId = activitytimes.actId " +
            "WHERE customactivities.name = :name")
    Map<CustomActivity, List<ActivityTime>> getActivityByNameWithTimes(String name);

    @Transaction
    @Query("SELECT * FROM customactivities JOIN activitytimes ON customactivities.activityId = activitytimes.actId " +
            "WHERE customactivities.activityId = :id")
    Map<CustomActivity, List<ActivityTime>> getActivityByIdWithTimes(long id);

    @Query("SELECT * FROM customactivities WHERE customactivities.name = :name")
    CustomActivity getActivityByName(String name);

    @Query("SELECT * FROM customactivities WHERE customactivities.activityId = :id")
    CustomActivity getActivityById(long id);

    @Query("SELECT activityId FROM customactivities WHERE customactivities.name = :name")
    int getIdByName(String name);

    @Query("SELECT activityId, name, color from customactivities")
    LiveData<List<ActivityFilter>> getActivityFilter();

    @Transaction
    @Query("SELECT * FROM customactivities ORDER BY customactivities.priority DESC, customactivities.remaining DESC, customactivities.lastDayAdded ASC")
    LiveData<List<ActivityWithTimes>> getActivitiesWithTimes();

    @Transaction
    @Query("SELECT * FROM customactivities where activityId in (:list)")
    List<ActivityWithTimes> getActivitiesWithTimesFilter(List<Long> list);

    @Transaction
    @Query("SELECT * FROM customactivities")
    LiveData<List<ActivityWithTimes>> getActivitiesWithTimesFilterAll();

    @Transaction
    @Query("SELECT * FROM customactivities WHERE activityId = :id")
    ActivityWithTimes getActivityWithTimesEntity(long id);
}
