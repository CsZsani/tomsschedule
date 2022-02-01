package hu.janny.tomsschedule.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

//@Entity(foreignKeys = {@ForeignKey(entity = CustomActivity.class,
 //parentColumns = "activityId", childColumns = "activityId", onDelete = ForeignKey.NO_ACTION, onUpdate = ForeignKey.RESTRICT)})
@Entity(tableName = "activitytimes")
public class ActivityTime {

    @NonNull
    @ColumnInfo(name = "activityId")
    public int activityId;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "date")
    public long date;

    @ColumnInfo(name = "time")
    public long time = 0L;

    @Ignore
    public ActivityTime() {}

    public ActivityTime(@NonNull int activityId,@NonNull long date, long time) {
        this.activityId = activityId;
        this.date = date;
        this.time = time;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
