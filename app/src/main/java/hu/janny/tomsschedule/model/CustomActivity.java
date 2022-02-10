package hu.janny.tomsschedule.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
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

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "activityId")
    public long id;

    @NonNull
    @ColumnInfo(name = "userId")
    public String userId;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "color")
    public int col;

    @ColumnInfo(name = "note")
    public String note;

    @ColumnInfo(name = "priority")
    public int pr;

    @ColumnInfo(name = "timeType")
    public int tT = 0;

    @ColumnInfo(name = "duration")
    public long dur = 0L;

    @ColumnInfo(name = "deadline")
    public long dl = 0L;

    @ColumnInfo(name = "regularity")
    public int reg = 0;

    @ColumnInfo(name = "hasFixedDays")
    public boolean hFD = false;

    @ColumnInfo(name = "startDay")
    public long sD = 0L;

    @ColumnInfo(name = "endDay")
    public long eD = 0L;

    @ColumnInfo(name = "notification")
    public boolean notif = false;

    @Embedded(prefix = "wd")
    public CustomWeekTime customWeekTime = new CustomWeekTime();

    @Ignore
    public CustomActivity() {
    }

    public CustomActivity(@NonNull long id, @NonNull String userId,@NonNull String name, int col, String note, int pr) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.col = col;
        this.note = note;
        this.pr = pr;

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

    public long getId() {
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

    public long getDl() {
        return dl;
    }

    public void setDl(long dl) {
        this.dl = dl;
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

    public boolean isNotif() {
        return notif;
    }

    public void setNotif(boolean notif) {
        this.notif = notif;
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
                ", dl=" + dl +
                ", reg=" + reg +
                ", hFD=" + hFD +
                ", sD=" + sD +
                ", eD=" + eD +
                ", notif=" + notif +
                ", customWeekTime=" + customWeekTime +
                '}';
    }

    @Exclude
    public void setEverythingToDefault() {
        tT = 0;
        dur = 0;
        dl = 0L;
        reg = 0;
        hFD = false;
        sD = 0L;
        eD = 0L;
        notif = false;
        customWeekTime.setEverythingToDefault();
    }
}
