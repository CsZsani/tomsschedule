package hu.janny.tomsschedule.model.entities;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

@Entity(tableName = "activitytimes", primaryKeys = {"actId", "date"}, foreignKeys = {@ForeignKey(onDelete = CASCADE, entity = CustomActivity.class,
        parentColumns = "activityId", childColumns = "actId")})
public class ActivityTime implements Comparable<ActivityTime> {

    // Id of activity to which the ActivityTime belongs
    @NonNull
    @ColumnInfo(name = "actId")
    public long aId;

    // Date
    @NonNull
    @ColumnInfo(name = "date")
    public long d;

    // Time spent with this activity on the given date
    @ColumnInfo(name = "time")
    public long t = 0L;

    // Constructors

    public ActivityTime() {
    }

    @Ignore
    public ActivityTime(@NonNull long aId, @NonNull long d, long t) {
        this.aId = aId;
        this.d = d;
        this.t = t;
    }

    // Getters and setters

    public long getaId() {
        return aId;
    }

    public void setaId(long aId) {
        this.aId = aId;
    }

    public long getD() {
        return d;
    }

    public void setD(long d) {
        this.d = d;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }

    @NonNull
    @Override
    public String toString() {
        return "activityId: " + aId + ", date: " + d + ", time: " + t;
    }

    @Override
    public int compareTo(ActivityTime activityTime) {
        return Long.compare(0L, this.d - activityTime.d);
    }
}
