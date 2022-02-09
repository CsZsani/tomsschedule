package hu.janny.tomsschedule.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import hu.janny.tomsschedule.R;

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
        cal.set((int) Integer.parseInt(date.split(" ")[2]), getMonthIntFromMonthFormat(date.split(" ")[0]),
                (int) Integer.parseInt(date.split(" ")[1]));
        return cal.getTimeInMillis();
    }

    public static String dateMillisToString(Date inMillisDate) {
        return inMillisDate.toString();
    }

    public static String longToString(long inMillisLong) {return Long.toString(inMillisLong);}

    public static long stringMillisToLong(String inMillisString) {
        return Long.parseLong(inMillisString);
    }

    public static String longMillisToStringForSimpleDateDialog(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateStringForSimpleDateDialog(day, month, year);
    }

    public static int birthDateFromSimpleDateDialogToAgeGroupInt(String birthDateString) {
        Calendar birthDateCal = Calendar.getInstance();
        birthDateCal.set(Integer.parseInt(birthDateString.split(" ")[2]), getMonthIntFromMonthFormat(birthDateString.split(" ")[0]),
                Integer.parseInt(birthDateString.split(" ")[1]));

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());

        today.add(Calendar.YEAR, -20);
        int group = 0;
        while(!birthDateCal.after(today) && group <= 5) {
            today.add(Calendar.YEAR, -10);
            group++;
        }
        return group;
    }

    public static long durationTimeConverterFromIntToLong(int days, int hours, int minutes) {
        long dayMillis = days * 24L * 60L * 60L * 1000L;
        long hourMillis = hours * 60L * 60L * 1000L;
        long minMillis = minutes * 60L * 1000L;
        return dayMillis + hourMillis + minMillis;
    }

    public static long durationTimeConverterFromIntToLongForDays(int hours, int minutes) {
        long hourMillis = hours * 60L * 60L * 1000L;
        long minMillis = minutes * 60L * 1000L;
        return hourMillis + minMillis;
    }

    public static long durationTimeConverterFromStringToLong(String days, String hours, String minutes) {
        long dayMillis = Long.parseLong(days) * 24L * 60L * 60L * 1000L;
        long hourMillis = Long.parseLong(hours) * 60L * 60L * 1000L;
        long minMillis = Long.parseLong(minutes) * 60L * 1000L;
        return dayMillis + hourMillis + minMillis;
    }

    public static long durationTimeConverterFromStringToLongForDays(String hours, String minutes) {
        long hourMillis = Long.parseLong(hours) * 60L * 60L * 1000L;
        long minMillis = Long.parseLong(minutes) * 60L * 1000L;
        return hourMillis + minMillis;
    }

    public static String durationConverterFromLongToString(long milliseconds) {
        System.out.println(milliseconds);
        long dy = TimeUnit.MILLISECONDS.toDays(milliseconds);
        long hr = TimeUnit.MILLISECONDS.toHours(milliseconds)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(milliseconds));
        long min = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));
        System.out.println(dy + "nap");
        System.out.println(hr + "ora");
        System.out.println(min + "perc");
        if(dy == 0) {
            return String.format("%dh %dmin", hr, min);
        }else {
            return String.format("%dd %dh %dmin", dy, hr, min);
        }

    }

    public static void exampleLongToTime(long millis) {
        int h = (int) ((millis / 1000) / 3600);
        int m = (int) (((millis / 1000) / 60) % 60);
        int s = (int) ((millis / 1000) % 60);
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
