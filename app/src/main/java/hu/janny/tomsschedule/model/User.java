package hu.janny.tomsschedule.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import hu.janny.tomsschedule.R;

@Entity(tableName = "users")
@TypeConverters(DateTypeConverter.class)
public class User {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "userId")
    public String uid;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "birthDate")
    public String birthDate;

    @ColumnInfo(name = "ageGroup")
    public int ageGroup;

    @ColumnInfo(name = "gender")
    public String gender;

    @ColumnInfo(name = "lastSeenSer")
    public long lastSeenSer = 0L;

    @ColumnInfo(name = "lastSeenSys")
    public long lastSeenSys = 0L;

    @ColumnInfo(name = "isLoggedIn")
    public boolean isLoggedIn = false;

    //@Ignore
    public User() {

    }

    public User(@NonNull String uid, String email, String name, String birthDate, int ageGroup, String gender) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.ageGroup = ageGroup;
        this.gender = gender;
    }

    @Exclude
    private Gender stringToGender(String gender) {
        if(gender.equals("female")) {
            return Gender.FEMALE;
        }
        return Gender.MALE;
    }

    @Exclude
    public String ageGroup() {
        switch (ageGroup) {
            case 0: return "<20";
            case 1: return "20-30";
            case 2: return "30-40";
            case 3: return "40-50";
            case 4: return "50-60";
            case 5: return ">60";
            default: return "?";
        }
    }

    public String getGender() {return gender;}

    @Exclude
    public int getGenderForAccount() {
        if(gender.equals("female")) {
            return R.string.female;
        } else {
            return R.string.male;
        }
    }

    @Exclude
    public String getGenderString() {
        return gender;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public int getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(int ageGroup) {
        this.ageGroup = ageGroup;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getLastSeenSer() {
        return lastSeenSer;
    }

    public void setLastSeenSer(long lastSeenSer) {
        this.lastSeenSer = lastSeenSer;
    }

    public long getLastSeenSys() {
        return lastSeenSys;
    }

    public void setLastSeenSys(long lastSeenSys) {
        this.lastSeenSys = lastSeenSys;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    @Exclude
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", ageGroup=" + ageGroup +
                ", gender='" + gender + '\'' +
                ", lastSeenSer=" + lastSeenSer +
                ", lastSeenSys=" + lastSeenSys +
                ", isLoggedIn=" + isLoggedIn +
                '}';
    }
}
