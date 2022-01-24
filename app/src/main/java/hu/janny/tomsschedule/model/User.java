package hu.janny.tomsschedule.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class User {

    public String email, name;
    public Date birthDate;
    public Gender gender;

    public User() {

    }

    public User(String email, String name, String birthDate, String gender) {
        this.email = email;
        this.name = name;
        this.birthDate = stringToMillis(birthDate);
        this.gender = stringToGender(gender);
    }

    public User(String email, String name, Date birthDate, Gender gender) {
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    private Date stringToMillis(String birthDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date(System.currentTimeMillis());
        try {
            date = sdf.parse(getMonth(birthDate.split(" ")[0]) + "-" + birthDate.split(" ")[1] + "-" + birthDate.split(" ")[2]);
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private String getMonth(String s) {
        if(s.equals("JAN")) {
            return "01";
        }
        if(s.equals("FEB")) {
            return "02";
        }
        if(s.equals("MAR")) {
            return "03";
        }
        if(s.equals("APR")) {
            return "04";
        }
        if(s.equals("MAY")) {
            return "05";
        }
        if(s.equals("JUN")) {
            return "06";
        }
        if(s.equals("JUL")) {
            return "07";
        }
        if(s.equals("AUG")) {
            return "08";
        }
        if(s.equals("SEP")) {
            return "09";
        }
        if(s.equals("OKT")) {
            return "10";
        }
        if(s.equals("NOV")) {
            return "11";
        }
        if(s.equals("DEC")) {
            return "12";
        }

        return "01";
    }

    private Gender stringToGender(String gender) {
        if(gender == "Female") {
            return Gender.FEMALE;
        }
        return Gender.MALE;
    }
}
