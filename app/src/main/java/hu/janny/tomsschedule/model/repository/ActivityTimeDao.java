package hu.janny.tomsschedule.model.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import hu.janny.tomsschedule.model.ActivityTime;

@Dao
public abstract class ActivityTimeDao {

    /*@Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(ActivityTime activityTime);*/

    @Insert
    public abstract void insertActivityTime(ActivityTime activityTime);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insertIgnore(ActivityTime activityTime);

    @Query("UPDATE activitytimes SET time = time + :timeAmount WHERE actId = :activityId and date = :date")
    public abstract void update(long activityId, long date, long timeAmount);

    @Transaction
    public boolean insertOrUpdateTime(ActivityTime activityTime) {
        if (insertIgnore(activityTime) == -1L) {
            update(activityTime.getaId(), activityTime.getD(), activityTime.getT());
            return false;
        }
        return true;
    }

    @Update
    public abstract void updateActivityTime(ActivityTime activityTime);

    @Delete
    public abstract void deleteActivityTime(ActivityTime activityTime);

    @Query("DELETE FROM activitytimes WHERE actId = :id")
    public abstract void deleteActivityTimeByActivityId(long id);

    @Query("select * from activitytimes where activitytimes.date >= :from")
    public abstract List<ActivityTime> getAllLaterDates(long from);

    @Query("select * from activitytimes where activitytimes.date >= :from and activitytimes.date <= :to")
    public abstract List<ActivityTime> getAllBetweenTwoDates(long from, long to);

    @Query("select * from activitytimes where activitytimes.actId = :id and activitytimes.date >= :from")
    public abstract List<ActivityTime> getOneByIdLaterDates(long id, long from);

    @Query("select * from activitytimes where activitytimes.actId = :id and activitytimes.date >= :from and activitytimes.date <= :to")
    public abstract List<ActivityTime> getOneByIdBetweenTwoDates(long id, long from, long to);
}
