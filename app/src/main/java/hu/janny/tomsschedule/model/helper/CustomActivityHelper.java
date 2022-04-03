package hu.janny.tomsschedule.model.helper;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.entities.CustomWeekTime;

/**
 * This class is for helping converting CustomActivity format in database to UI displaying.
 */
public final class CustomActivityHelper {

    // List of fix activities
    private final static List<String> list = Arrays.asList("SLEEPING", "WORKOUT", "COOKING", "HOUSEWORK",
            "SHOPPING", "WORK", "SCHOOL", "LEARNING", "TRAVELLING", "READING", "RELAXATION", "HOBBY");
    // Displaying 0 hours and 0 minutes
    public final static String NULL_MIN = "0h 0m";

    /**
     * Confirms if the given activity name is a fix activity
     *
     * @param string name of activity
     * @return true if it is a fix activity, false otherwise
     */
    public static boolean isFixActivity(String string) {
        for (int i = 0; i < list.size(); i++) {
            if (string.equals(list.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the database name of a given fix activity. If it not a fix activity the method returns "ERROR".
     *
     * @param d name of the activity
     * @return database name of fix activity
     */
    public static String getSelectedFixActivityName(String d) {
        switch (d) {
            case "Sleeping":
            case "Alvás":
                return "SLEEPING";
            case "Cooking":
            case "Főzés":
                return "COOKING";
            case "Workout":
            case "Edzés":
                return "WORKOUT";
            case "Housework":
            case "Házimunka":
                return "HOUSEWORK";
            case "Shopping":
            case "Bevásárlás":
                return "SHOPPING";
            case "Work":
            case "Munka":
                return "WORK";
            case "School":
            case "Iskola":
                return "SCHOOL";
            case "Learning":
            case "Tanulás":
                return "LEARNING";
            case "Travelling":
            case "Utazás":
                return "TRAVELLING";
            case "Hobby":
            case "Hobbi":
                return "HOBBY";
            case "Relaxation":
            case "Kikapcsolódás":
                return "RELAXATION";
            case "Reading":
            case "Olvasás":
                return "READING";
            default:
                return "ERROR";
        }
    }

    /**
     * Returns the string resource int of a given fix activity. If it not a fix activity the method returns "ERROR" resource string.
     *
     * @param d name of the activity
     * @return resource string of fix activity
     */
    public static int getStringResourceOfFixActivity(String d) {
        switch (d) {
            case "SLEEPING":
                return R.string.fa_sleeping;
            case "COOKING":
                return R.string.fa_cooking;
            case "WORKOUT":
                return R.string.fa_workout;
            case "HOUSEWORK":
                return R.string.fa_housework;
            case "SHOPPING":
                return R.string.fa_shopping;
            case "WORK":
                return R.string.fa_work;
            case "SCHOOL":
                return R.string.fa_school;
            case "LEARNING":
                return R.string.fa_learning;
            case "TRAVELLING":
                return R.string.fa_travelling;
            case "HOBBY":
                return R.string.fa_hobby;
            case "RELAXATION":
                return R.string.fa_relaxation;
            case "READING":
                return R.string.fa_reading;
        }
        return R.string.error;
    }

    /**
     * Returns the time spent on an activity today. Works properly if the list includes just one activity's times.
     *
     * @param list the times of an activity
     * @return time spent today
     */
    public static long getHowManyTimeWasSpentTodayOnAct(List<ActivityTime> list, long todayMillis) {
        ActivityTime activityTime = list.stream()
                .filter(at -> at.getD() == todayMillis)
                .findAny()
                .orElse(null);
        if (activityTime != null) {
            return activityTime.getT();
        } else {
            return 0L;
        }
    }

    /**
     * Returns the sum time spent on an activity in the given interval.
     *
     * @param list the times of an activity
     * @param from the beginning date of the interval in epoch millis
     * @param to   the end date of the interval in epoch millis
     * @return time spent in the interval
     */
    public static long getHowManyTimeWasSpentOnActInInterval(List<ActivityTime> list, long from, long to) {
        List<ActivityTime> activityTime = list.stream()
                .filter(at -> at.getD() >= from && at.getD() <= to)
                .collect(Collectors.toList());
        long sumTime = 0L;
        for (ActivityTime at : activityTime) {
            sumTime += at.getT();
        }
        return sumTime;
    }

    /**
     * Returns the sum time spent on an activity from the given date.
     *
     * @param list the times of an activity
     * @param from the beginning date of the interval in epoch millis
     * @return time spent from the given date
     */
    public static long getHowManyTimeWasSpentFrom(List<ActivityTime> list, long from) {
        List<ActivityTime> activityTime = list.stream()
                .filter(at -> at.getD() >= from)
                .collect(Collectors.toList());
        long sumTime = 0L;
        for (ActivityTime at : activityTime) {
            sumTime += at.getT();
        }
        return sumTime;
    }

    /**
     * Returns a string for displaying deadlines, end dates of an activity.
     *
     * @param activity the activity we want to display
     * @return string to be displayed
     */
    public static String detailsOnCardsDeadline(CustomActivity activity) {
        if (activity.getsD() != 0L && activity.geteD() != 0L) {
            // Deadline
            return DateConverter.longMillisToStringForSimpleDateDialog(activity.getsD())
                    + "-" + DateConverter.longMillisToStringForSimpleDateDialog(activity.geteD());
        } else if (activity.getsD() == 0L && activity.geteD() != 0L) {
            // End date
            return DateConverter.longMillisToStringForSimpleDateDialog(activity.geteD());
        } else {
            return "";
        }
    }

    /**
     * Returns a string resource for displaying regularity type of an activity.
     *
     * @param activity the activity we want to display
     * @return string resource to be displayed
     */
    public static int detailsOnCardRegularity(CustomActivity activity) {
        if (activity.getReg() > 0) {
            switch (activity.getReg()) {
                case 1:
                    return R.string.details_daily;
                case 2:
                    return R.string.details_weekly;
                case 3:
                    return R.string.details_monthly;
            }
        }
        return 0;
    }

    /**
     * Returns a string for displaying duration of an activity.
     *
     * @param activity the activity we want to display
     * @return string time to be displayed
     */
    public static String detailsOnCardDuration(CustomActivity activity) {
        if (activity.gettT() > 0 && activity.gettT() != 5) {
            return DateConverter.durationConverterFromLongToString(activity.getDur());
        }
        return "";
    }

    /**
     * Returns today in long millis.
     *
     * @return today long millis
     */
    public static long todayMillis() {
        LocalDate localDate = LocalDate.now();
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * Returns long millis of the given number of day before today.
     *
     * @param days the number of day we want backwards from today
     * @return long millis of the given number of day before today
     */
    public static long minusDaysMillis(int days) {
        LocalDate localDate = LocalDate.now();
        LocalDate returnValue = localDate.minusDays(days);
        Instant instant = returnValue.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * Returns long millis of the given number of week before today.
     *
     * @param week the number of weeks we want backwards from today
     * @return long millis of the given number of week before today
     */
    public static long minusWeekMillis(int week) {
        LocalDate localDate = LocalDate.now();
        LocalDate returnValue = localDate.minusWeeks(week);
        Instant instant = returnValue.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * Returns long millis of the given number of month before today.
     *
     * @param month the number of months we want backwards from today
     * @return long millis of the given number of month before today
     */
    public static long minusMonthMillis(int month) {
        LocalDate localDate = LocalDate.now();
        LocalDate returnValue = localDate.minusMonths(month);
        Instant instant = returnValue.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * Returns the long millis of this monday.
     *
     * @return long millis of this monday
     */
    public static long thisMondayMillis() {
        LocalDate localDate = LocalDate.now();
        LocalDate mon = localDate.with(DayOfWeek.MONDAY);
        Instant instant = mon.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * Returns the long millis of the first day of this month.
     *
     * @return long millis of the first day of this month
     */
    public static long firstDayOfThisMonth() {
        LocalDate localDate = LocalDate.now();
        LocalDate fd = localDate.withDayOfMonth(1);
        Instant instant = fd.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    /**
     * Returns what day is today.
     *
     * @return day of today
     */
    public static DayOfWeek whatDayOfWeekToday() {
        LocalDate dt = LocalDate.now();
        return dt.getDayOfWeek();
    }

    /**
     * Returns the goal duration in string for the given day of an activity.
     *
     * @param customWeekTime the custom week time of an activity
     * @param today          today in DayOfWeek
     * @return time string to be displayed
     */
    public static String goalDurationForFixedDay(CustomWeekTime customWeekTime, DayOfWeek today) {
        switch (today) {
            case MONDAY:
                return DateConverter.durationConverterFromLongToStringForADay(customWeekTime.getMon());
            case TUESDAY:
                return DateConverter.durationConverterFromLongToStringForADay(customWeekTime.getTue());
            case WEDNESDAY:
                return DateConverter.durationConverterFromLongToStringForADay(customWeekTime.getWed());
            case THURSDAY:
                return DateConverter.durationConverterFromLongToStringForADay(customWeekTime.getThu());
            case FRIDAY:
                return DateConverter.durationConverterFromLongToStringForADay(customWeekTime.getFri());
            case SATURDAY:
                return DateConverter.durationConverterFromLongToStringForADay(customWeekTime.getSat());
            case SUNDAY:
                return DateConverter.durationConverterFromLongToStringForADay(customWeekTime.getSun());
        }
        return "?";
    }

    /**
     * Returns the goal duration in long for the given day of an activity.
     *
     * @param customWeekTime the custom week time of an activity
     * @param today          today in DayOfWeek
     * @return time string to be displayed
     */
    public static long goalDurationForFixedDayLong(CustomWeekTime customWeekTime, DayOfWeek today) {
        switch (today) {
            case MONDAY:
                return customWeekTime.getMon();
            case TUESDAY:
                return customWeekTime.getTue();
            case WEDNESDAY:
                return customWeekTime.getWed();
            case THURSDAY:
                return customWeekTime.getThu();
            case FRIDAY:
                return customWeekTime.getFri();
            case SATURDAY:
                return customWeekTime.getSat();
            case SUNDAY:
                return customWeekTime.getSun();
        }
        return -1L;
    }

    /**
     * Returns the number of day in week if today is set in custom week time of an activity.
     * Returns 0 if today is not set in custom week time.
     *
     * @param customWeekTime the custom week time of an activity
     * @return number of day in week if today is set, 0 otherwise
     */
    public static int todayIsAFixedDayAndWhat(CustomWeekTime customWeekTime) {
        DayOfWeek today = whatDayOfWeekToday();
        switch (today) {
            case MONDAY:
                if (customWeekTime.getMon() != -1L) {
                    return 1;
                }
                break;
            case TUESDAY:
                if (customWeekTime.getTue() != -1L) {
                    return 2;
                }
                break;
            case WEDNESDAY:
                if (customWeekTime.getWed() != -1L) {
                    return 3;
                }
                break;
            case THURSDAY:
                if (customWeekTime.getThu() != -1L) {
                    return 4;
                }
                break;
            case FRIDAY:
                if (customWeekTime.getFri() != -1L) {
                    return 5;
                }
                break;
            case SATURDAY:
                if (customWeekTime.getSat() != -1L) {
                    return 6;
                }
                break;
            case SUNDAY:
                if (customWeekTime.getSun() != -1L) {
                    return 7;
                }
                break;
        }
        return 0;
    }

    /**
     * Returns long millis set if today is set in custom week time of an activity.
     * Returns 0 if today is not set in custom week time.
     *
     * @param customWeekTime the custom week time of an activity
     * @return long millis set if today is set, 0 otherwise
     */
    public static long todayIsAFixedDayAndDuration(CustomWeekTime customWeekTime) {
        DayOfWeek today = whatDayOfWeekToday();
        switch (today) {
            case MONDAY:
                if (customWeekTime.getMon() != -1L) {
                    return customWeekTime.getMon();
                }
                break;
            case TUESDAY:
                if (customWeekTime.getTue() != -1L) {
                    return customWeekTime.getTue();
                }
                break;
            case WEDNESDAY:
                if (customWeekTime.getWed() != -1L) {
                    return customWeekTime.getWed();
                }
                break;
            case THURSDAY:
                if (customWeekTime.getThu() != -1L) {
                    return customWeekTime.getThu();
                }
                break;
            case FRIDAY:
                if (customWeekTime.getFri() != -1L) {
                    return customWeekTime.getFri();
                }
                break;
            case SATURDAY:
                if (customWeekTime.getSat() != -1L) {
                    return customWeekTime.getSat();
                }
                break;
            case SUNDAY:
                if (customWeekTime.getSun() != -1L) {
                    return customWeekTime.getSun();
                }
                break;
        }
        return 0L;
    }

    /**
     * Returns the string time is done so far for the goal of an activity.
     *
     * @param activity activity
     * @return time in string to display
     */
    public static String getSoFar(CustomActivity activity) {
        switch (activity.gettN()) {
            case 1:
                return "-";
            case 2:
            case 8:
                if (activity.getlD() != todayMillis()) {
                    return NULL_MIN;
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return "-";
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 3:
                if (activity.getlD() < firstDayOfThisMonth()) {
                    return NULL_MIN;
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return "-";
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 4:
                if (activity.getlD() < thisMondayMillis()) {
                    return NULL_MIN;
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return "-";
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 5:
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return "-";
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 6:
            case 7:
                if (activity.getlD() < activity.getsD()) {
                    return NULL_MIN;
                }
                if (todayMillis() > activity.geteD()) {
                    return "-";
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
        }
        return "?";
    }

    /**
     * Returns the time is done so far for the goal of an activity.
     *
     * @param activity activity
     * @return time in long millis
     */
    public static long getSoFarLong(CustomActivity activity) {
        switch (activity.gettN()) {
            case 1:
                return -1;
            case 2:
            case 8:
                if (activity.getlD() != todayMillis()) {
                    return 0L;
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return -1L;
                }
                return activity.getsF();
            case 3:
                if (activity.getlD() < firstDayOfThisMonth()) {
                    return 0L;
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return -1L;
                }
                return activity.getsF();
            case 4:
                if (activity.getlD() < thisMondayMillis()) {
                    return 0L;
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return -1L;
                }
                return activity.getsF();
            case 5:
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return -1L;
                }
                return activity.getsF();
            case 6:
            case 7:
                if (activity.getlD() < activity.getsD() || todayMillis() > activity.geteD()) {
                    return -1L;
                }
                return activity.getsF();
        }
        return -1L;
    }

    /**
     * Returns the time in string is remaining for the goal of an activity.
     *
     * @param activity activity
     * @return time ins string for displaying
     */
    public static String getRemaining(CustomActivity activity) {
        switch (activity.gettN()) {
            case 1:
            case 6:
                return "-";
            case 2:
            case 8:
                if (activity.getlD() != todayMillis()) {
                    if (activity.ishFD()) {
                        return goalDurationForFixedDay(activity.getCustomWeekTime(), whatDayOfWeekToday());
                    } else {
                        return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                    }
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return "-";
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 3:
                if (activity.getlD() < firstDayOfThisMonth()) {
                    return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return "-";
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 4:
                if (activity.getlD() < thisMondayMillis()) {
                    if (activity.ishFD()) {
                        return goalDurationForFixedDay(activity.getCustomWeekTime(), whatDayOfWeekToday());
                    } else {
                        return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                    }
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return "-";
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 5:
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return "-";
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 7:
                if (activity.getlD() < activity.getsD()) {
                    return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                }
                if (todayMillis() > activity.geteD()) {
                    return "-";
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
        }
        return "?";
    }

    /**
     * Returns the time in long millis is remaining for the goal of an activity.
     *
     * @param activity activity
     * @return time ins string for displaying
     */
    public static long getRemainingLong(CustomActivity activity) {
        switch (activity.gettN()) {
            case 1:
            case 6:
                return -1L;
            case 2:
            case 8:
                if (activity.getlD() != todayMillis()) {
                    if (activity.ishFD()) {
                        return goalDurationForFixedDayLong(activity.getCustomWeekTime(), whatDayOfWeekToday());
                    } else {
                        return activity.getDur();
                    }
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return -1L;
                }
                return activity.getRe();
            case 3:
                if (activity.getlD() < firstDayOfThisMonth()) {
                    return activity.getDur();
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return -1L;
                }
                return activity.getRe();
            case 4:
                if (activity.getlD() < thisMondayMillis()) {
                    if (activity.ishFD()) {
                        return goalDurationForFixedDayLong(activity.getCustomWeekTime(), whatDayOfWeekToday());
                    } else {
                        return activity.getDur();
                    }
                }
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return -1L;
                }
                return activity.getRe();
            case 5:
                if (activity.getsD() == 0L && activity.geteD() != 0L && todayMillis() > activity.geteD()) {
                    return -1L;
                }
                return activity.getRe();
            case 7:
                if (activity.getlD() < activity.getsD()) {
                    return activity.getDur();
                }
                if (todayMillis() > activity.geteD()) {
                    return -1L;
                }
                return activity.getRe();
        }
        return -1L;
    }

    /**
     * Updates the activity when we add a time to its ActivityTimes.
     *
     * @param customActivity the activity to be updated
     * @param activityTime   the added activity time
     */
    public static void updateActivity(CustomActivity customActivity, ActivityTime activityTime) {
        customActivity.setaT(customActivity.getaT() + activityTime.getT());
        switch (customActivity.gettN()) {
            case 2:
                updateDaily(customActivity, activityTime);
                break;
            case 3:
                updateMonthly(customActivity, activityTime);
                break;
            case 4:
                updateWeekly(customActivity, activityTime);
                break;
            case 5:
                updateSumTime(customActivity, activityTime);
                break;
            case 6:
                if (activityTime.getD() >= customActivity.getsD() && activityTime.getD() <= customActivity.geteD()) {
                    customActivity.setsF(customActivity.getsF() + activityTime.getT());
                }
                break;
            case 7:
                if (activityTime.getD() >= customActivity.getsD() && activityTime.getD() <= customActivity.geteD()) {
                    if (customActivity.getlD() < customActivity.getsD()) {
                        customActivity.setsF(activityTime.getT());
                    } else {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                    }
                    customActivity.setRe(Math.max((customActivity.getDur() - customActivity.getsF()), 0L));
                }
                break;
            case 8:
                updateCustomTime(customActivity, activityTime);
                break;
        }
        updateLastDay(customActivity, activityTime);
    }

    /**
     * Updates the activity when we add a time to its ActivityTimes if its regularity is "daily".
     *
     * @param customActivity the activity to be updated
     * @param activityTime   the added activity time
     */
    private static void updateDaily(CustomActivity customActivity, ActivityTime activityTime) {
        long todayMillis = CustomActivityHelper.todayMillis();
        if (customActivity.ishFD()) {
            if (activityTime.getD() == todayMillis && CustomActivityHelper.todayIsAFixedDayAndWhat(customActivity.getCustomWeekTime()) != 0) {
                if (customActivity.geteD() == 0L || activityTime.getD() <= customActivity.geteD()) {
                    updateDailyFields(customActivity, activityTime, todayMillis);
                }
            }
        } else {
            if (activityTime.getD() == todayMillis) {
                updateDailyFields(customActivity, activityTime, todayMillis);
            }
        }
    }

    /**
     * Updates soFar and remaining fields of the activity when we add a time to its ActivityTimes if its regularity is "daily".
     *
     * @param customActivity the activity to be updated
     * @param activityTime   the added activity time
     * @param todayMillis    today in epoch millis
     */
    private static void updateDailyFields(CustomActivity customActivity, ActivityTime activityTime, long todayMillis) {
        if (customActivity.getlD() != todayMillis) {
            customActivity.setsF(activityTime.getT());
        } else {
            customActivity.setsF(customActivity.getsF() + activityTime.getT());
        }
        customActivity.setRe(Math.max((customActivity.getDur() - customActivity.getsF()), 0L));
    }

    /**
     * Updates the activity when we add a time to its ActivityTimes if its regularity is "monthly".
     *
     * @param customActivity the activity to be updated
     * @param activityTime   the added activity time
     */
    private static void updateMonthly(CustomActivity customActivity, ActivityTime activityTime) {
        long firstDayOfThisMonth = CustomActivityHelper.firstDayOfThisMonth();
        if (customActivity.geteD() == 0L) {
            if (activityTime.getD() >= firstDayOfThisMonth) {
                updateMonthlyFields(customActivity, activityTime, firstDayOfThisMonth);
            }
        } else {
            if (activityTime.getD() >= firstDayOfThisMonth && activityTime.getD() <= customActivity.geteD()) {
                updateMonthlyFields(customActivity, activityTime, firstDayOfThisMonth);
            }
        }
    }

    /**
     * Updates soFar and remaining fields of the activity when we add a time to its ActivityTimes if its regularity is "monthly".
     *
     * @param customActivity      the activity to be updated
     * @param activityTime        the added activity time
     * @param firstDayOfThisMonth today in epoch millis
     */
    private static void updateMonthlyFields(CustomActivity customActivity, ActivityTime activityTime, long firstDayOfThisMonth) {
        if (customActivity.getlD() < firstDayOfThisMonth) {
            customActivity.setsF(activityTime.getT());
        } else {
            customActivity.setsF(customActivity.getsF() + activityTime.getT());
        }
        customActivity.setRe(Math.max((customActivity.getDur() - customActivity.getsF()), 0L));
    }

    /**
     * Updates the activity when we add a time to its ActivityTimes if its regularity is "weekly".
     *
     * @param customActivity the activity to be updated
     * @param activityTime   the added activity time
     */
    private static void updateWeekly(CustomActivity customActivity, ActivityTime activityTime) {
        long thisMonday = CustomActivityHelper.thisMondayMillis();
        if (customActivity.geteD() == 0L) {
            if (activityTime.getD() >= thisMonday) {
                updateWeeklyFields(customActivity, activityTime, thisMonday);
            }
        } else {
            if (activityTime.getD() >= thisMonday && activityTime.getD() <= customActivity.geteD()) {
                updateWeeklyFields(customActivity, activityTime, thisMonday);
            }
        }
    }

    /**
     * Updates soFar and remaining fields of the activity when we add a time to its ActivityTimes if its regularity is "weekly".
     *
     * @param customActivity the activity to be updated
     * @param activityTime   the added activity time
     * @param thisMonday     today in epoch millis
     */
    private static void updateWeeklyFields(CustomActivity customActivity, ActivityTime activityTime, long thisMonday) {
        if (customActivity.getlD() < thisMonday) {
            customActivity.setsF(activityTime.getT());
        } else {
            customActivity.setsF(customActivity.getsF() + activityTime.getT());
        }
        customActivity.setRe(Math.max((customActivity.getDur() - customActivity.getsF()), 0L));
    }

    /**
     * Updates the activity when we add a time to its ActivityTimes if its duration is "all time/sum time".
     *
     * @param customActivity the activity to be updated
     * @param activityTime   the added activity time
     */
    private static void updateSumTime(CustomActivity customActivity, ActivityTime activityTime) {
        if (customActivity.geteD() == 0L) {
            //if (customActivity.getsF() < customActivity.getDur()) {
            customActivity.setsF(customActivity.getsF() + activityTime.getT());
            customActivity.setRe(Math.max((customActivity.getDur() - customActivity.getsF()), 0L));
            //}
        } else {
            //if (customActivity.getsF() < customActivity.getDur() && activityTime.getD() <= customActivity.geteD()) {
            if (activityTime.getD() <= customActivity.geteD()) {
                customActivity.setsF(customActivity.getsF() + activityTime.getT());
                customActivity.setRe(Math.max((customActivity.getDur() - customActivity.getsF()), 0L));
            }
        }
    }

    /**
     * Updates the activity when we add a time to its ActivityTimes if its duration is "custom time",
     * different duration for the selected days.
     *
     * @param customActivity the activity to be updated
     * @param activityTime   the added activity time
     */
    private static void updateCustomTime(CustomActivity customActivity, ActivityTime activityTime) {
        long todayMillis = CustomActivityHelper.todayMillis();
        if (customActivity.geteD() == 0L) {
            if (activityTime.getD() == todayMillis && CustomActivityHelper.todayIsAFixedDayAndWhat(customActivity.getCustomWeekTime()) != 0) {
                updateCustomFields(customActivity, activityTime, todayMillis);
            }
        } else {
            if (activityTime.getD() == todayMillis && CustomActivityHelper.todayIsAFixedDayAndWhat(customActivity.getCustomWeekTime()) != 0 &&
                    activityTime.getD() <= customActivity.geteD()) {
                updateCustomFields(customActivity, activityTime, todayMillis);
            }
        }
    }

    /**
     * Updates soFar and remaining fields of the activity when we add a time to its ActivityTimes if its duration is "custom time",
     * different duration for the selected days.
     *
     * @param customActivity the activity to be updated
     * @param activityTime   the added activity time
     * @param todayMillis    today in epoch millis
     */
    private static void updateCustomFields(CustomActivity customActivity, ActivityTime activityTime, long todayMillis) {
        if (customActivity.getlD() != todayMillis) {
            customActivity.setsF(activityTime.getT());
        } else {
            customActivity.setsF(customActivity.getsF() + activityTime.getT());
        }
        customActivity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(customActivity.getCustomWeekTime()) - customActivity.getsF()), 0L));
    }

    /**
     * Updates lastDay field, if we add a new time record to the activity.
     *
     * @param customActivity activity to which we added a time
     * @param activityTime   the activity time we added
     */
    private static void updateLastDay(CustomActivity customActivity, ActivityTime activityTime) {
        if (customActivity.getlD() < activityTime.getD()) {
            customActivity.setlD(activityTime.getD());
        }
    }

    /**
     * Recalculates the soFar and remaining fields of an activity. It used when we edit an activity
     * and want to keep the consistency.
     *
     * @param activity the activity we edited, but with new parameters to recalculate soFar and remaining
     * @param times    the times that the activity has and used for recalculating soFar and remaining
     */
    public static void recalculateAfterEditActivity(CustomActivity activity, List<ActivityTime> times) {
        long todayMillis = CustomActivityHelper.todayMillis();
        long firstDayOfThisMonth = CustomActivityHelper.firstDayOfThisMonth();
        long thisMonday = CustomActivityHelper.thisMondayMillis();
        switch (activity.gettN()) {
            case 2:
                if (activity.ishFD()) {
                    if (activity.geteD() < todayMillis && CustomActivityHelper.todayIsAFixedDayAndWhat(activity.getCustomWeekTime()) != 0) {
                        if (activity.getlD() == todayMillis) {
                            activity.setsF(getHowManyTimeWasSpentTodayOnAct(times, todayMillis));
                            activity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(activity.getCustomWeekTime()) - activity.getsF()), 0L));
                        } else {
                            activity.setsF(0L);
                            activity.setRe(CustomActivityHelper.todayIsAFixedDayAndDuration(activity.getCustomWeekTime()));
                        }
                    } else {
                        activity.setsF(0L);
                        activity.setRe(0L);
                    }
                } else {
                    if (activity.getlD() == todayMillis) {
                        activity.setsF(getHowManyTimeWasSpentTodayOnAct(times, todayMillis));
                        activity.setRe(Math.max(activity.getDur() - activity.getsF(), 0L));
                    } else {
                        activity.setsF(0L);
                        activity.setRe(activity.getDur());
                    }
                }
                break;
            case 3:
                if (activity.geteD() != 0L && activity.geteD() < todayMillis) {
                    activity.setsF(0L);
                    activity.setRe(0L);
                } else {
                    if (activity.getlD() >= firstDayOfThisMonth) {
                        activity.setsF(getHowManyTimeWasSpentFrom(times, firstDayOfThisMonth));
                        activity.setRe(Math.max(activity.getDur() - activity.getsF(), 0L));
                    } else {
                        activity.setsF(0L);
                        activity.setRe(activity.getDur());
                    }
                }
                break;
            case 4:
                if (activity.geteD() != 0L && activity.geteD() < todayMillis) {
                    activity.setsF(0L);
                    activity.setRe(0L);
                } else {
                    if (activity.getlD() >= thisMonday) {
                        activity.setsF(getHowManyTimeWasSpentFrom(times, thisMonday));
                        activity.setRe(Math.max(activity.getDur() - activity.getsF(), 0L));
                    } else {
                        activity.setsF(0L);
                        activity.setRe(activity.getDur());
                    }
                }
                break;
            case 5:
                if (activity.geteD() != 0L && todayMillis > activity.geteD()) {
                    activity.setsF(0L);
                    activity.setRe(0L);
                } else {
                    long sumTime = 0L;
                    for (ActivityTime at : times) {
                        sumTime += at.getT();
                    }
                    activity.setsF(sumTime);
                    activity.setRe(Math.max(activity.getDur() - activity.getsF(), 0L));
                }
                break;
            case 6:
                activity.setsF(getHowManyTimeWasSpentOnActInInterval(times, activity.getsD(), activity.geteD()));
                activity.setRe(0L);
                break;
            case 7:
                if (todayMillis > activity.geteD()) {
                    activity.setsF(0L);
                    activity.setRe(0L);
                } else {
                    activity.setsF(getHowManyTimeWasSpentOnActInInterval(times, activity.getsD(), activity.geteD()));
                    activity.setRe(Math.max(activity.getDur() - activity.getsF(), 0L));
                }
                break;
            case 8:
                if (activity.geteD() != 0L && todayMillis > activity.geteD()) {
                    activity.setsF(0L);
                    activity.setRe(0L);
                } else {
                    if (activity.getlD() == todayMillis && CustomActivityHelper.todayIsAFixedDayAndWhat(activity.getCustomWeekTime()) != 0) {
                        activity.setsF(getHowManyTimeWasSpentTodayOnAct(times, todayMillis));
                        activity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(activity.getCustomWeekTime()) - activity.getsF()), 0L));
                    } else {
                        activity.setsF(0L);
                        activity.setRe(CustomActivityHelper.todayIsAFixedDayAndDuration(activity.getCustomWeekTime()));
                    }
                }
                break;
        }
    }

}
