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
    void insertActivity(CustomActivity customActivity);

    @Update
    void updateActivity(CustomActivity customActivity);

    @Delete
    void deleteActivity(CustomActivity customActivity);

    @Query("DELETE FROM customactivities WHERE name = :name")
    void deleteActivityByName(String name);

    @Query("DELETE FROM customactivities WHERE activityId = :id")
    void deleteActivityById(int id);

    @Query("SELECT * FROM customactivities JOIN activitytimes ON customactivities.activityId = activitytimes.activityId " +
            "WHERE customactivities.userId = :userId")
    LiveData<Map<CustomActivity, List<ActivityTime>>> getAllActivitiesWithTimes(String userId);

    @Query("SELECT * FROM customactivities JOIN activitytimes ON customactivities.activityId = activitytimes.activityId " +
            "WHERE customactivities.name = :name and customactivities.userId = :userId")
    Map<CustomActivity, List<ActivityTime>> getActivityByNameWithTimes(String userId, String name);

    @Query("SELECT * FROM customactivities JOIN activitytimes ON customactivities.activityId = activitytimes.activityId " +
            "WHERE customactivities.activityId = :id and customactivities.userId = :userId")
    Map<CustomActivity, List<ActivityTime>> getActivityByIdWithTimes(String userId, int id);

    @Query("SELECT * FROM customactivities WHERE customactivities.name = :name and customactivities.userId = :userId")
    CustomActivity getActivityByName(String userId, String name);

    @Query("SELECT * FROM customactivities WHERE customactivities.activityId = :id and customactivities.userId = :userId")
    CustomActivity getActivityById(String userId, int id);
}
