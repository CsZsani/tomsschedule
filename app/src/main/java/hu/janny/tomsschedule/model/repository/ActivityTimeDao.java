package hu.janny.tomsschedule.model.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import hu.janny.tomsschedule.model.entities.ActivityTime;

@Dao
public abstract class ActivityTimeDao {

    /**
     * Inserts a new activity time into the database.
     *
     * @param activityTime the activity time to be inserted
     */
    @Insert
    public abstract void insertActivityTime(ActivityTime activityTime);

    @Transaction
    @Insert
    public abstract void insertAll(List<ActivityTime> activityTimes);

    // ActivityTime - insert or update

    /**
     * Returns -1L if we could not insert, because that will not be an unique row. This is how we are
     * able to check if we have to update a row or just insert.
     *
     * @param activityTime
     * @return
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insertIgnore(ActivityTime activityTime);

    /**
     * Insert or updates an activity time. If the given activity already has time on the given date,
     * then we update (returns false), if it has not the we insert (returns true).
     *
     * @param activityTime the time to be updated (or inserted)
     * @return true if we inserted, false if we updated
     */
    @Transaction
    public boolean insertOrUpdateTime(ActivityTime activityTime) {
        if (insertIgnore(activityTime) == -1L) {
            update(activityTime.getaId(), activityTime.getD(), activityTime.getT());
            return false;
        }
        return true;
    }

    /**
     * Updates the activity time. Adds time to the appropriate row based on activityId and date.
     *
     * @param activityId the id of the activity to which the time belongs to
     * @param date       date of activity time
     * @param timeAmount the amount of time we want to add
     */
    @Query("UPDATE activitytimes SET time = time + :timeAmount WHERE actId = :activityId and date = :date")
    public abstract void update(long activityId, long date, long timeAmount);

    @Query("DELETE FROM activitytimes WHERE actId = :id")
    public abstract void deleteActivityTimeByActivityId(long id);

    // Personal statistics

    // For act. today, yesterday

    /**
     * Searches the times of the activities in the given list on the given day.
     *
     * @param day  the day on which we want to find times
     * @param list the list of activities
     * @return the list of times that belongs to the given activities and day
     */
    @Query("select * from activitytimes where activitytimes.date == :day and actId in (:list) ORDER BY time DESC")
    public abstract List<ActivityTime> getSomeExactDate(long day, List<Long> list);

    // For act. last 3 day, last 1, 2 week, last 1, 3 month

    /**
     * Searches the times of the activities in the given list from the given day to today.
     *
     * @param from the day from which we want to find times
     * @param list the list of activities
     * @return the list of times that belongs to the given activities and interval
     */
    @Transaction
    @Query("select * from activitytimes where activitytimes.date >= :from and actId in (:list)")
    public abstract List<ActivityTime> getSomeLaterDates(long from, List<Long> list);

    // For act. interval

    /**
     * Searches the times of the activities in the given list from the given day (from) to an other given day (to).
     *
     * @param from the day from which we want to find times
     * @param to   the day to which we want to find times
     * @param list the list of activities
     * @return the list of times that belongs to the given activities and interval
     */
    @Transaction
    @Query("select * from activitytimes where activitytimes.date >= :from and activitytimes.date <= :to and actId in (:list)")
    public abstract List<ActivityTime> getSomeBetweenTwoDates(long from, long to, List<Long> list);

    // all
    @Transaction
    @Query("select * from activitytimes")
    public abstract List<ActivityTime> getAllAllTheTime();

    @Transaction
    @Query("select * from activitytimes")
    public abstract LiveData<List<ActivityTime>> getTimes();

    // For some act. all times
    @Transaction
    @Query("select * from activitytimes where actId in (:list)")
    public abstract List<ActivityTime> getSomeAll(List<Long> list);

    @Query("select * from activitytimes where activitytimes.actId = :id and activitytimes.date >= :from")
    public abstract List<ActivityTime> getOneByIdLaterDates(long id, long from);

    @Query("select * from activitytimes where activitytimes.actId = :id and activitytimes.date >= :from and activitytimes.date <= :to")
    public abstract List<ActivityTime> getOneByIdBetweenTwoDates(long id, long from, long to);

    //*******//
    // Shelf //
    //*******//

    // For all act. today, yesterday
    @Query("select * from activitytimes where activitytimes.date == :day ORDER BY time DESC")
    public abstract List<ActivityTime> getAllExactDate(long day);

    // For all act. last 3 day, last 1, 2 week, last 1, 3 month
    @Transaction
    @Query("select * from activitytimes where activitytimes.date >= :from")
    public abstract List<ActivityTime> getAllLaterDates(long from);

    // For all act. interval
    @Transaction
    @Query("select * from activitytimes where activitytimes.date >= :from and activitytimes.date <= :to")
    public abstract List<ActivityTime> getAllBetweenTwoDates(long from, long to);

}
