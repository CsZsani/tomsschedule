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

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    public int id;

    @NonNull
    @ColumnInfo(name = "activityId")
    public long aId;

    @NonNull
    @ColumnInfo(name = "date")
    public long d;

    @ColumnInfo(name = "time")
    public long t = 0L;

    @Ignore
    public ActivityTime() {}

    public ActivityTime(@NonNull long aId,@NonNull long d, long t) {
        this.aId = aId;
        this.d = d;
        this.t = t;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getaId() {
        return aId;
    }

    public void setaId(int aId) {
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
}
