package hu.janny.tomsschedule.model.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.database.Exclude;

@Entity(tableName = "customactivities")
public class CustomActivity {

    // Id of activity - usually the creation time of the activity
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "activityId")
    public long id;

    // Id of user who adds this activity
    @NonNull
    @ColumnInfo(name = "userId")
    public String userId;

    // Name
    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    // Colour theme of activity
    @ColumnInfo(name = "color")
    public int col;

    // Note
    @ColumnInfo(name = "note")
    public String note;

    // Priority - ranging from 1 to 10 (10 is the highest priority, the most important)
    @ColumnInfo(name = "priority")
    public int pr;

    // Type of duration we set to the activity
    // 0 - we do not want to give a special duration
    // 1 - when we give all time
    // 2 - when we give the same duration for every day
    // 3 - when we give the same duration for every week
    // 4 - when we give the same duration for every month
    // 5 - when we give different custom duration for every selected day in a week
    @ColumnInfo(name = "timeType")
    public int tT = 0;

    // The length of duration in long millis
    @ColumnInfo(name = "duration")
    public long dur = 0L;

    // Regularity
    // 0 - when we do not choose that this is a regular activity
    // 1 - daily
    // 2 - weekly
    // 3 - monthly
    @ColumnInfo(name = "regularity")
    public int reg = 0;

    // Info about whether fixed days were set or did not
    // true - when we selected fixed days of week
    // false - otherwise
    @ColumnInfo(name = "hasFixedDays")
    public boolean hFD = false;

    // Start day of interval in long millis
    // If it is 0L that means we do not choose that this is an "interval" activity
    // (If it is 0L, but eD is not, that means it is an end date)
    @ColumnInfo(name = "startDay")
    public long sD = 0L;

    // End day of interval in long millis
    // If it is 0L that means we do not choose that this is an "interval" activity
    // If it is not 0L, but sD is, then it is an end date
    @ColumnInfo(name = "endDay")
    public long eD = 0L;

    // How many time the user have spent on this activity so far
    @ColumnInfo(name = "soFar")
    public long sF = 0L;

    // How many time is remaining to the goal duration
    @ColumnInfo(name = "remaining")
    public long re = 0L;

    // All time spent in the current activity
    @ColumnInfo(name = "allTime")
    public long aT = 0L;

    // The last day when the user added time to this activity
    @ColumnInfo(name = "lastDayAdded")
    public long lD = 0L;

    // The type of activity - makes easier to decide how to display some details
    /**
     * 1 - neither, no duration
     * - regular, daily, no dur.
     * - regular, weekly, fixed days, no end date, no duration
     * - regular, weekly, fixed days, end date, no duration
     * 2 - interval, duration, daily
     * - regular, daily, duration, daily
     * - regular, weekly, fixed days, no end date, duration, daily
     * - regular, weekly, fixed days, end date, duration, daily
     * 3 - regular, monthly, no end date
     * - regular, monthly, end date
     * 4 - regular, weekly, no fixed days, no end date
     * - regular, weekly, no fixed days, end date
     * - regular, weekly, fixed days, no end date, duration, weekly
     * - regular, weekly, fixed days, end date, duration, weekly
     * 5 - neither, duration
     * - regular, weekly, fixed days, end date, duration, all time
     * 6 - interval, no duration
     * 7 - interval, duration, all time
     * 8 - regular, weekly, fixed days, no end date, duration, custom
     * - regular, weekly, fixed days, end date, duration, custom
     */
    @ColumnInfo(name = "typeNumber")
    public int tN = 0;

    // Do you want notification about this activity?
    // NOT USED
    @ColumnInfo(name = "notification")
    public boolean notif = false;

    // The selected days of week
    // -1 - that day is not selected
    // 0 - that day is selected, but no given duration
    // greater than 0 - that day is selected and duration in long millis
    @Embedded(prefix = "wd")
    public CustomWeekTime customWeekTime = new CustomWeekTime();

    // Constructors

    public CustomActivity() {
    }

    @Ignore
    public CustomActivity(long id, @NonNull String userId, @NonNull String name, int col, String note, int pr) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.col = col;
        this.note = note;
        this.pr = pr;

    }

    @Ignore
    public CustomActivity(long id, @NonNull String userId, @NonNull String name, int col, String note, int pr, int tT, long dur, int reg, boolean hFD, long sD, long eD, long sF, long re, long aT, long lD, int tN, boolean notif, CustomWeekTime customWeekTime) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.col = col;
        this.note = note;
        this.pr = pr;
        this.tT = tT;
        this.dur = dur;
        this.reg = reg;
        this.hFD = hFD;
        this.sD = sD;
        this.eD = eD;
        this.sF = sF;
        this.re = re;
        this.aT = aT;
        this.lD = lD;
        this.tN = tN;
        this.notif = notif;
        this.customWeekTime = customWeekTime;
    }

    // Getters and setters

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getPr() {
        return pr;
    }

    public void setPr(int pr) {
        this.pr = pr;
    }

    public int gettT() {
        return tT;
    }

    public void settT(int tT) {
        this.tT = tT;
    }

    public long getDur() {
        return dur;
    }

    public void setDur(long dur) {
        this.dur = dur;
    }

    public int getReg() {
        return reg;
    }

    public void setReg(int reg) {
        this.reg = reg;
    }

    public boolean ishFD() {
        return hFD;
    }

    public void sethFD(boolean hFD) {
        this.hFD = hFD;
    }

    public long getsD() {
        return sD;
    }

    public void setsD(long sD) {
        this.sD = sD;
    }

    public long geteD() {
        return eD;
    }

    public void seteD(long eD) {
        this.eD = eD;
    }

    public long getsF() {
        return sF;
    }

    public void setsF(long sF) {
        this.sF = sF;
    }

    public long getRe() {
        return re;
    }

    public void setRe(long re) {
        this.re = re;
    }

    public long getaT() {
        return aT;
    }

    public void setaT(long aT) {
        this.aT = aT;
    }

    public long getlD() {
        return lD;
    }

    public void setlD(long lD) {
        this.lD = lD;
    }

    public int gettN() {
        return tN;
    }

    public void settN(int tN) {
        this.tN = tN;
    }

    public boolean isNotif() {
        return notif;
    }

    public void setNotif(boolean notif) {
        this.notif = notif;
    }

    @Exclude
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Exclude
    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }


    @Override
    public String toString() {
        return "CustomActivity{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", col=" + col +
                ", note='" + note + '\'' +
                ", pr=" + pr +
                ", tT=" + tT +
                ", dur=" + dur +
                ", reg=" + reg +
                ", hFD=" + hFD +
                ", sD=" + sD +
                ", eD=" + eD +
                ", sF=" + sF +
                ", re=" + re +
                ", aT=" + aT +
                ", lD=" + lD +
                ", tN=" + tN +
                ", notif=" + notif +
                ", customWeekTime=" + customWeekTime +
                '}';
    }

    // Sets everything activity feature to default
    @Exclude
    public void setEverythingToDefault() {
        tT = 0;
        dur = 0;
        reg = 0;
        hFD = false;
        sD = 0L;
        eD = 0L;
        tN = 0;
        notif = false;
        customWeekTime.setEverythingToDefault();
    }
}
