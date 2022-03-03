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
import java.util.Calendar;
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
    public String birthDateToAgeGroupInt() {
        Calendar birthDateCal = Calendar.getInstance();
        birthDateCal.set(Integer.parseInt(birthDate.split(" ")[2]), getMonthIntFromMonthFormat(birthDate.split(" ")[0]),
                Integer.parseInt(birthDate.split(" ")[1]));

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());

        today.add(Calendar.YEAR, -20);
        int group = 0;
        while(!birthDateCal.after(today) && group <= 4) {
            today.add(Calendar.YEAR, -10);
            group++;
        }
        return ageGroup(group);
    }

    @Exclude
    public String ageGroup(int ageGroup) {
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

    /**
     * Returns the string for age group from age group integer (0-5).
     * @return age group string for UI
     */
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

    /**
     * Returns the month int from 3 letter string, JAN is 1, and DEC is 12.
     * @param s 3 letter string in capital letters which is short for months
     * @return integer for months from 1 to 12
     */
    @Exclude
    private int getMonthIntFromMonthFormat(String s) {
        if(s.equals("JAN")) {
            return 1;
        }
        if(s.equals("FEB")) {
            return 2;
        }
        if(s.equals("MAR")) {
            return 3;
        }
        if(s.equals("APR")) {
            return 4;
        }
        if(s.equals("MAY")) {
            return 5;
        }
        if(s.equals("JUN")) {
            return 6;
        }
        if(s.equals("JUL")) {
            return 7;
        }
        if(s.equals("AUG")) {
            return 8;
        }
        if(s.equals("SEP")) {
            return 9;
        }
        if(s.equals("OKT")) {
            return 10;
        }
        if(s.equals("NOV")) {
            return 11;
        }
        if(s.equals("DEC")) {
            return 12;
        }

        return 1;
    }

    /**
     * Returns the gender int, male is , female is 2.
     * @return gender int for user
     */
    @Exclude
    public int getGenderInt() {
        if(gender.equals("female")) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * Returns the gender string resource.
     * @return string resource for gender of user
     */
    @Exclude
    public int getGenderForAccount() {
        if(gender.equals("female")) {
            return R.string.female;
        } else {
            return R.string.male;
        }
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

    public String getGender() {return gender;}

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
