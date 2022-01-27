package hu.janny.tomsschedule.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import hu.janny.tomsschedule.R;

public class User {

    public String email, name, birthDate, ageGroup, gender;

    public User() {

    }

    public User(String email, String name, String birthDate, String ageGroup, String gender) {
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.ageGroup = ageGroup;
        this.gender = gender;
    }

    private Gender stringToGender(String gender) {
        if(gender == "Female") {
            return Gender.FEMALE;
        }
        return Gender.MALE;
    }

    public String ageGroup() {
        switch (ageGroup) {
            case "0": return "<20";
            case "1": return "20-30";
            case "2": return "30-40";
            case "3": return "40-50";
            case "4": return "50-60";
            case "5": return ">60";
            default: return "?";
        }
    }

    public int getGender() {
        if(gender.equals("Female")) {
            return R.string.female;
        } else {
            return R.string.male;
        }
    }
}
