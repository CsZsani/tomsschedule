package hu.janny.tomsschedule.model.repository;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

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

    /**
     * Inserts all the times from the given list.
     *
     * @param activityTimes list of times to be inserted
     */
    @Transaction
    @Insert
    public abstract void insertAll(List<ActivityTime> activityTimes);

    // ActivityTime - insert or update

    /**
     * Returns -1L if we could not insert, because that will not be an unique row. This is how we are
     * able to check if we have to update a row or just insert.
     *
     * @param activityTime the time to be inserted or updated
     * @return if it is -1L, then there is a row for the activity and date in activityTime
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

    // all
    @Transaction
    @Query("select * from activitytimes")
    public abstract List<ActivityTime> getAllAllTheTime();

    // For some act. all times
    @Transaction
    @Query("select * from activitytimes where actId in (:list)")
    public abstract List<ActivityTime> getSomeAll(List<Long> list);

}
