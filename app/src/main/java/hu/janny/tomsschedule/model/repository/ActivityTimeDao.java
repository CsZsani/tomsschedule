package hu.janny.tomsschedule.model.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import hu.janny.tomsschedule.model.ActivityTime;

@Dao
public interface ActivityTimeDao {

    @Insert
    void insertActivityTime(ActivityTime activityTime);

    @Update
    void updateActivityTime(ActivityTime activityTime);

    @Delete
    void deleteActivityTime(ActivityTime activityTime);

    @Query("DELETE FROM activitytimes WHERE activityId = :id")
    void deleteActivityTimeByActivityId(long id);

    @Query("select * from activitytimes where activitytimes.date >= :from")
    List<ActivityTime> getAllLaterDates(long from);

    @Query("select * from activitytimes where activitytimes.date >= :from and activitytimes.date <= :to")
    List<ActivityTime> getAllBetweenTwoDates(long from, long to);

    @Query("select * from activitytimes where activitytimes.activityId = :id and activitytimes.date >= :from")
    List<ActivityTime> getOneByIdLaterDates(int id, long from);

    @Query("select * from activitytimes where activitytimes.activityId = :id and activitytimes.date >= :from and activitytimes.date <= :to")
    List<ActivityTime> getOneByIdBetweenTwoDates(int id, long from, long to);
}
