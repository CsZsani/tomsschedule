package hu.janny.tomsschedule.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

//@Entity(foreignKeys = {@ForeignKey(entity = CustomActivity.class,
//parentColumns = "activityId", childColumns = "activityId", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.RESTRICT)})
@Entity(tableName = "customweektime")
public class CustomWeekTime {

    /*@NonNull
    @ColumnInfo(name = "activityId")
    public int activityId;
    */
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int id;

    @ColumnInfo(name = "monday")
    public long mon = -1L;
    @ColumnInfo(name = "tuesday")
    public long tue = -1L;
    @ColumnInfo(name = "wednesday")
    public long wed = -1L;
    @ColumnInfo(name = "thursday")
    public long thu = -1L;
    @ColumnInfo(name = "friday")
    public long fri = -1L;
    @ColumnInfo(name = "saturday")
    public long sat = -1L;
    @ColumnInfo(name = "sunday")
    public long sun = -1L;

    public CustomWeekTime() {}
    /*public CustomWeekTime(int activityId) {
        this.activityId = activityId;
    }*/

    /*public CustomWeekTime(@NonNull int activityId) {
        this.activityId = activityId;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }*/

    public long getMon() {
        return mon;
    }

    public void setMon(long mon) {
        this.mon = mon;
    }

    public long getTue() {
        return tue;
    }

    public void setTue(long tue) {
        this.tue = tue;
    }

    public long getWed() {
        return wed;
    }

    public void setWed(long wed) {
        this.wed = wed;
    }

    public long getThu() {
        return thu;
    }

    public void setThu(long thu) {
        this.thu = thu;
    }

    public long getFri() {
        return fri;
    }

    public void setFri(long fri) {
        this.fri = fri;
    }

    public long getSat() {
        return sat;
    }

    public void setSat(long sat) {
        this.sat = sat;
    }

    public long getSun() {
        return sun;
    }

    public void setSun(long sun) {
        this.sun = sun;
    }
}
