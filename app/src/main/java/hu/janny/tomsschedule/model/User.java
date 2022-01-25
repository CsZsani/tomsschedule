package hu.janny.tomsschedule.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class User {

    public String email, name, birthDate, gender;

    public User() {

    }

    public User(String email, String name, String birthDate, String gender) {
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    private Gender stringToGender(String gender) {
        if(gender == "Female") {
            return Gender.FEMALE;
        }
        return Gender.MALE;
    }
}
