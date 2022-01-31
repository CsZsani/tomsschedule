package hu.janny.tomsschedule.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Entity(foreignKeys = {@ForeignKey(entity = User.class,
//    parentColumns = "userId", childColumns = "userId", onDelete = ForeignKey.NO_ACTION, onUpdate = ForeignKey.RESTRICT)})
@Entity(tableName = "customactivities")
public class CustomActivity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "activityId")
    public int id;

    @NonNull
    @ColumnInfo(name = "userId")
    public String userId;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "color")
    public int color;

    @ColumnInfo(name = "note")
    public String note;

    @ColumnInfo(name = "priority")
    public int priority;

    @ColumnInfo(name = "time")
    public int time = 0;

    @ColumnInfo(name = "deadline")
    public long deadline = 0L;

    @ColumnInfo(name = "regularity")
    public int regularity = 0;

    @ColumnInfo(name = "hasFixedDays")
    public boolean hasFixedDays = false;

    @ColumnInfo(name = "startDay")
    public long startDay = 0L;

    @ColumnInfo(name = "endDay")
    public long endDay = 0L;

    @ColumnInfo(name = "notification")
    public boolean turnOffNotification = false;

    @Embedded public CustomWeekTime customWeekTime = new CustomWeekTime();

    public CustomActivity() {}

    public CustomActivity(@NonNull String userId,@NonNull String name, int color, String note, int priority) {
        this.userId = userId;
        this.name = name;
        this.color = color;
        this.note = note;
        this.priority = priority;
    }

    public CustomWeekTime getCustomWeekTime() {
        return customWeekTime;
    }

    public void setCustomWeekTime(CustomWeekTime customWeekTime) {
        this.customWeekTime = customWeekTime;
    }

    public void setCustomWeekTime(long mon, long tue, long wed, long thu, long fri, long sat, long sun) {
        this.customWeekTime.setMon(mon);
        this.customWeekTime.setTue(tue);
        this.customWeekTime.setWed(wed);
        this.customWeekTime.setThu(thu);
        this.customWeekTime.setFri(fri);
        this.customWeekTime.setSat(sat);
        this.customWeekTime.setSun(sun);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public int getRegularity() {
        return regularity;
    }

    public void setRegularity(int regularity) {
        this.regularity = regularity;
    }

    public boolean isHasFixedDays() {
        return hasFixedDays;
    }

    public void setHasFixedDays(boolean hasFixedDays) {
        this.hasFixedDays = hasFixedDays;
    }

    public long getStartDay() {
        return startDay;
    }

    public void setStartDay(long startDay) {
        this.startDay = startDay;
    }

    public long getEndDay() {
        return endDay;
    }

    public void setEndDay(long endDay) {
        this.endDay = endDay;
    }

    public boolean isTurnOffNotification() {
        return turnOffNotification;
    }

    public void setTurnOffNotification(boolean turnOffNotification) {
        this.turnOffNotification = turnOffNotification;
    }
}
