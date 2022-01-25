package hu.janny.tomsschedule.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DateConverter {
    private DateConverter() {}

    public static String makeDateStringForSimpleDateDialog(int day, int month, int year) {
        return getMonthFormatFromInt(month) + " " + day + " " + year;
    }

    public static String getTodayDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return DateConverter.makeDateStringForSimpleDateDialog(day, month, year);
    }

    public static Date stringFromSimpleDateDialogToMillis(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date d = new Date(System.currentTimeMillis());
        try {
            d = sdf.parse(getMonthStringFromMonthFormat(date.split(" ")[0])
                    + "-" + date.split(" ")[1] + "-" + date.split(" ")[2]);
        }catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static long stringFromSimpleDateDialogToLongMillis(String date) {
        Calendar cal = Calendar.getInstance();
        cal.set((int) Integer.parseInt(date.split(" ")[2]), (int) Integer.parseInt(date.split(" ")[1]),
                getMonthIntFromMonthFormat(date.split(" ")[0]));
        return cal.getTimeInMillis();
    }

    public static String dateMillisToString(Date inMillis) {
        return inMillis.toString();
    }

    public static long stringMillisToLong(String millis) {
        return Long.parseLong(millis);
    }

    public static String longMillisToStringForSimpleDateDialog(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateStringForSimpleDateDialog(day, month, year);
    }

    public static String getMonthStringFromMonthFormat(String s) {
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

    public static int getMonthIntFromMonthFormat(String s) {
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

    public static String getMonthFormatFromInt(int month) {
        if(month == 1) {
            return "JAN";
        }
        if(month == 2) {
            return "FEB";
        }
        if(month == 3) {
            return "MAR";
        }
        if(month == 4) {
            return "APR";
        }
        if(month == 5) {
            return "MAY";
        }
        if(month == 6) {
            return "JUN";
        }
        if(month == 7) {
            return "JUL";
        }
        if(month == 8) {
            return "AUG";
        }
        if(month == 9) {
            return "SEP";
        }
        if(month == 10) {
            return "OKT";
        }
        if(month == 11) {
            return "NOV";
        }
        if(month == 12) {
            return "DEC";
        }

        return "JAN";
    }
}
