package hu.janny.tomsschedule.model.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.Map;

import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.CustomActivity;

@Dao
public interface CustomActivityDao {

    @Insert
    long insertActivity(CustomActivity customActivity);

    @Update
    void updateActivity(CustomActivity customActivity);

    @Delete
    void deleteActivity(CustomActivity customActivity);

    @Query("DELETE FROM customactivities WHERE name = :name")
    void deleteActivityByName(String name);

    @Query("DELETE FROM customactivities WHERE activityId = :id")
    void deleteActivityById(int id);

    @Query("SELECT * FROM customactivities")
    LiveData<List<CustomActivity>> getActivitiesList();

    @Query("SELECT * FROM customactivities JOIN activitytimes ON customactivities.activityId = activitytimes.activityId")
    LiveData<Map<CustomActivity, List<ActivityTime>>> getAllActivitiesWithTimes();

    @Query("SELECT * FROM customactivities JOIN activitytimes ON customactivities.activityId = activitytimes.activityId " +
            "WHERE customactivities.name = :name")
    Map<CustomActivity, List<ActivityTime>> getActivityByNameWithTimes(String name);

    @Query("SELECT * FROM customactivities JOIN activitytimes ON customactivities.activityId = activitytimes.activityId " +
            "WHERE customactivities.activityId = :id")
    Map<CustomActivity, List<ActivityTime>> getActivityByIdWithTimes(long id);

    @Query("SELECT * FROM customactivities WHERE customactivities.name = :name")
    CustomActivity getActivityByName(String name);

    @Query("SELECT * FROM customactivities WHERE customactivities.activityId = :id")
    CustomActivity getActivityById(int id);

    @Query("SELECT activityId FROM customactivities WHERE customactivities.name = :name")
    int getIdByName(String name);
}
