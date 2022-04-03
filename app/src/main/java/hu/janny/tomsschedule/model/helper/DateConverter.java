package hu.janny.tomsschedule.model.helper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * This class is for converting between displaying time and stored format of time.
 */
public final class DateConverter {
    private DateConverter() {
    }

    /**
     * Returns the date in string: month in 3 letters day year, for example "MAR 7 2022".
     *
     * @param day   day
     * @param month month (from 1 to 12)
     * @param year  year
     * @return date in string
     */
    public static String makeDateStringForSimpleDateDialog(int day, int month, int year) {
        return getMonthFormatFromInt(month) + " " + day + " " + year;
    }

    /**
     * Returns the date in long millis from string. The string is: month in 3 letters day year, for e. "MAR 7 2022".
     *
     * @param date date string
     * @return time in long millis
     */
    public static long stringFromSimpleDateDialogToLongMillis(String date) {
        Calendar cal = Calendar.getInstance();
        cal.set((int) Integer.parseInt(date.split(" ")[2]), getMonthIntFromMonthFormat(date.split(" ")[0]),
                (int) Integer.parseInt(date.split(" ")[1]));
        return cal.getTimeInMillis();
    }

    /**
     * Returns the date given in millis in string for UI to display.
     *
     * @param millis date in long millis
     * @return string for displaying
     */
    public static String longMillisToStringForSimpleDateDialog(long millis) {
        LocalDate ld = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate();
        return makeDateStringForSimpleDateDialog(ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear());
    }

    /**
     * Returns the age group from the birth date string.
     *
     * @param birthDateString format: month in 3 letters day year, for example "MAR 7 2022".
     * @return age group (0-5)
     */
    public static int birthDateFromSimpleDateDialogToAgeGroupInt(String birthDateString) {
        Calendar birthDateCal = Calendar.getInstance();
        birthDateCal.set(Integer.parseInt(birthDateString.split(" ")[2]), getMonthIntFromMonthFormat(birthDateString.split(" ")[0])-1,
                Integer.parseInt(birthDateString.split(" ")[1]));

        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());

        today.add(Calendar.YEAR, -20);
        int group = 0;
        while (!birthDateCal.after(today) && group <= 4) {
            today.add(Calendar.YEAR, -10);
            group++;
        }
        return group;
    }

    /**
     * Returns the given time in long millis.
     *
     * @param days    days
     * @param hours   hours
     * @param minutes minutes
     * @return given time in long millis
     */
    public static long durationTimeConverterFromIntToLong(int days, int hours, int minutes) {
        long dayMillis = days * 24L * 60L * 60L * 1000L;
        long hourMillis = hours * 60L * 60L * 1000L;
        long minMillis = minutes * 60L * 1000L;
        return dayMillis + hourMillis + minMillis;
    }

    /**
     * Returns the given time in long millis. This is used for one day.
     *
     * @param hours   hours
     * @param minutes minutes
     * @return given time in long millis
     */
    public static long durationTimeConverterFromIntToLongForDays(int hours, int minutes) {
        long hourMillis = hours * 60L * 60L * 1000L;
        long minMillis = minutes * 60L * 1000L;
        return hourMillis + minMillis;
    }

    /**
     * Returns string for display time for a day.
     *
     * @param milliseconds long milliseconds
     * @return string for UI
     */
    public static String durationConverterFromLongToStringForADay(long milliseconds) {
        long hr = durationConverterFromLongToHours(milliseconds);
        long min = durationConverterFromLongToMinutes(milliseconds);
        return String.format(Locale.getDefault(), "%dh %dm", hr, min);
    }

    /**
     * Returns string for display time.
     *
     * @param milliseconds long milliseconds
     * @return string for UI
     */
    public static String durationConverterFromLongToString(long milliseconds) {
        long dy = durationConverterFromLongToDays(milliseconds);
        long hr = durationConverterFromLongToHours(milliseconds);
        long min = durationConverterFromLongToMinutes(milliseconds);
        if (dy == 0) {
            return String.format(Locale.getDefault(), "%dh %dm", hr, min);
        } else {
            return String.format(Locale.getDefault(), "%dd %dh %dm", dy, hr, min);
        }
    }

    /**
     * Returns string for display time for timer.
     *
     * @param milliseconds long milliseconds
     * @return string for display time in timer
     */
    public static String durationConverterFromLongToStringToTimer(long milliseconds) {
        long dy = durationConverterFromLongToDays(milliseconds);
        long hr = durationConverterFromLongToHours(milliseconds);
        long min = durationConverterFromLongToMinutes(milliseconds);
        long sec = durationConverterFromLongToSeconds(milliseconds);
        if (dy == 0 && hr == 0) {
            return String.format(Locale.getDefault(), "%02d:%02d", min, sec);
        } else if (dy == 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hr, min, sec);
        } else {
            return String.format(Locale.getDefault(), "%d:%02d:%02d:%02d", dy, hr, min, sec);
        }
    }

    /**
     * Returns time in float hours for charts.
     *
     * @param milliseconds long milliseconds
     * @return time in float hours for barchart
     */
    public static float durationConverterFromLongToBarChart(long milliseconds) {
        long hr = durationConverterFromLongToHours(milliseconds);
        long min = durationConverterFromLongToMinutes(milliseconds);
        float h = Integer.parseInt(String.valueOf(hr));
        float m = Integer.parseInt(String.valueOf(min));
        return h + (m / 60f);
    }

    /**
     * Returns time in float minutes for pie charts.
     *
     * @param milliseconds float milliseconds
     * @return time in float minutes for pie chart
     */
    public static int durationConverterForPieChart(float milliseconds) {
        return Math.round(milliseconds / 1000 / 60);
    }

    /**
     * Returns time in float minutes for pie charts. At first it converts into int then returns in float.
     *
     * @param milliseconds long milliseconds
     * @return time in float minutes for pie chart
     */
    public static float durationConverterFromLongToChartInt(long milliseconds) {
        long hr = durationConverterFromLongToHours(milliseconds);
        long min = durationConverterFromLongToMinutes(milliseconds);
        int h = Integer.parseInt(String.valueOf(hr));
        int m = Integer.parseInt(String.valueOf(min));
        return h * 60 + m;
    }

    /**
     * Returns the number of days from long milliseconds.
     *
     * @param milliseconds long milliseconds
     * @return the number of days
     */
    public static long durationConverterFromLongToDays(long milliseconds) {
        long dy = TimeUnit.MILLISECONDS.toDays(milliseconds);
        return dy;
    }

    /**
     * Returns the number of hours from long milliseconds.
     *
     * @param milliseconds long milliseconds
     * @return the number of hours
     */
    public static long durationConverterFromLongToHours(long milliseconds) {
        long hr = TimeUnit.MILLISECONDS.toHours(milliseconds)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(milliseconds));
        return hr;
    }

    /**
     * Returns the number of minutes from long milliseconds.
     *
     * @param milliseconds long milliseconds
     * @return the number of minutes
     */
    public static long durationConverterFromLongToMinutes(long milliseconds) {
        long min = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));
        return min;
    }

    /**
     * Returns the number of seconds from long milliseconds.
     *
     * @param milliseconds long milliseconds
     * @return the number of seconds
     */
    public static long durationConverterFromLongToSeconds(long milliseconds) {
        long sec = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));
        return sec;
    }

    /**
     * Converts float value to string for axis.
     * @param value value in float
     * @return string e.g. "1h 23m"
     */
    public static String chartTimeConverter(float value) {
        int hours = (int) value;
        int minutes = (int) ((value - (float) hours) * 60.0f);
        return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
    }

    /**
     * Converts float value to string for axis.
     * @param value value in float
     * @return string e.g. "1h 23m"
     */
    public static String chartTimeConverterFromInt(float value) {
        int allMinutes = (int) value;
        int hours = (int) (allMinutes / 60);
        int minutes = allMinutes - (hours * 60);
        return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
    }

    public static void exampleLongToTime(long millis) {
        int h = (int) ((millis / 1000) / 3600);
        int m = (int) (((millis / 1000) / 60) % 60);
        int s = (int) ((millis / 1000) % 60);
    }

    /**
     * Returns month int (1-12) from 3 letters string ("JAN").
     *
     * @param s 3 letters string, beginning of the month in English
     * @return month int (1-12)
     */
    public static int getMonthIntFromMonthFormat(String s) {
        if (s.equals("JAN")) {
            return 1;
        }
        if (s.equals("FEB")) {
            return 2;
        }
        if (s.equals("MAR")) {
            return 3;
        }
        if (s.equals("APR")) {
            return 4;
        }
        if (s.equals("MAY")) {
            return 5;
        }
        if (s.equals("JUN")) {
            return 6;
        }
        if (s.equals("JUL")) {
            return 7;
        }
        if (s.equals("AUG")) {
            return 8;
        }
        if (s.equals("SEP")) {
            return 9;
        }
        if (s.equals("OKT")) {
            return 10;
        }
        if (s.equals("NOV")) {
            return 11;
        }
        if (s.equals("DEC")) {
            return 12;
        }

        return 1;
    }

    /**
     * Returns month 3 letters string from int (1-12).
     *
     * @param month month int (1-12)
     * @return month in 3 letters string
     */
    public static String getMonthFormatFromInt(int month) {
        if (month == 1) {
            return "JAN";
        }
        if (month == 2) {
            return "FEB";
        }
        if (month == 3) {
            return "MAR";
        }
        if (month == 4) {
            return "APR";
        }
        if (month == 5) {
            return "MAY";
        }
        if (month == 6) {
            return "JUN";
        }
        if (month == 7) {
            return "JUL";
        }
        if (month == 8) {
            return "AUG";
        }
        if (month == 9) {
            return "SEP";
        }
        if (month == 10) {
            return "OKT";
        }
        if (month == 11) {
            return "NOV";
        }
        if (month == 12) {
            return "DEC";
        }

        return "JAN";
    }
}
