package hu.janny.tomsschedule.model.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import com.google.firebase.database.Exclude;

/**
 * This entity is for supporting the CustomActivity class and recording the duration added to
 * days of week when it has fixed days.
 * It is -1L if the day is not selected, 1L if it is selected but no duration set, and >0L
 * if it is selected and duration added (in long millis).
 */
public class CustomWeekTime {

    // Id of the custom week time
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

    // Getters and setters

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

    /**
     * Returns true if no day is selected (all -1L), false otherwise.
     * @return true if no day is selected, false otherwise
     */
    @Exclude
    public boolean nothingSet() {
        return mon == -1L && tue == -1L && wed == -1L && thu == -1L && fri == -1L && sat == -1L && sun == -1L;
    }

    /**
     * Sets every day to default -1L.
     */
    @Exclude
    public void setEverythingToDefault() {
        mon = -1L;
        tue = -1L;
        wed = -1L;
        thu = -1L;
        fri = -1L;
        sat = -1L;
        sun = -1L;
    }

    @NonNull
    @Override
    public String toString() {
        return mon + " " + tue + " " + wed + " " + thu + " " + fri + " " + sat + " " + sun;
    }
}
