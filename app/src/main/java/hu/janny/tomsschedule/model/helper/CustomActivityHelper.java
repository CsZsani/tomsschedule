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
        if (list.contains(string)) {
            return true;
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
        }
        return "ERROR";
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
    public static long getHowManyTimeWasSpentTodayOnAct(List<ActivityTime> list) {
        long todayMillis = CustomActivityHelper.todayMillis();
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

    public static long getHowManyTimeWasSpentOnActInInterval(List<ActivityTime> list, long from, long to) {
        List<ActivityTime> activityTime = list.stream()
                .filter(at -> at.getD() > from && at.getD() < to)
                .collect(Collectors.toList());
        long sumTime = 0L;
        for (ActivityTime at : activityTime) {
            sumTime += at.getT();
        }
        return sumTime;
    }

    public static long getHowManyTimeWasSpentFrom(List<ActivityTime> list, long from) {
        List<ActivityTime> activityTime = list.stream()
                .filter(at -> at.getD() > from)
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
            String text = DateConverter.longMillisToStringForSimpleDateDialog(activity.getsD())
                    + "-" + DateConverter.longMillisToStringForSimpleDateDialog(activity.geteD());
            return text;
        } else if (activity.getsD() == 0L && activity.geteD() != 0L) {
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
            case TUESDAY:
                if (customWeekTime.getTue() != -1L) {
                    return 2;
                }
            case WEDNESDAY:
                if (customWeekTime.getWed() != -1L) {
                    return 3;
                }
            case THURSDAY:
                if (customWeekTime.getThu() != -1L) {
                    return 4;
                }
            case FRIDAY:
                if (customWeekTime.getFri() != -1L) {
                    return 5;
                }
            case SATURDAY:
                if (customWeekTime.getSat() != -1L) {
                    return 6;
                }
            case SUNDAY:
                if (customWeekTime.getSun() != -1L) {
                    return 7;
                }
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
            case TUESDAY:
                if (customWeekTime.getTue() != -1L) {
                    return customWeekTime.getTue();
                }
            case WEDNESDAY:
                if (customWeekTime.getWed() != -1L) {
                    return customWeekTime.getWed();
                }
            case THURSDAY:
                if (customWeekTime.getThu() != -1L) {
                    return customWeekTime.getThu();
                }
            case FRIDAY:
                if (customWeekTime.getFri() != -1L) {
                    return customWeekTime.getFri();
                }
            case SATURDAY:
                if (customWeekTime.getSat() != -1L) {
                    return customWeekTime.getSat();
                }
            case SUNDAY:
                if (customWeekTime.getSun() != -1L) {
                    return customWeekTime.getSun();
                }
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
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 3:
                if (activity.getlD() < firstDayOfThisMonth()) {
                    return NULL_MIN;
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 4:
                if (activity.getlD() < thisMondayMillis()) {
                    return NULL_MIN;
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 5:
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 6:
            case 7:
                if (activity.getlD() < activity.getsD()) {
                    return NULL_MIN;
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
                return activity.getsF();
            case 3:
                if (activity.getlD() < firstDayOfThisMonth()) {
                    return 0L;
                }
                return activity.getsF();
            case 4:
                if (activity.getlD() < thisMondayMillis()) {
                    return 0L;
                }
                return activity.getsF();
            case 5:
                return activity.getsF();
            case 6:
            case 7:
                if (activity.getlD() < activity.getsD()) {
                    return 0L;
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
                if (activity.ishFD() && activity.getlD() != todayMillis()) {
                    return goalDurationForFixedDay(activity.getCustomWeekTime(), whatDayOfWeekToday());
                } else if (!activity.ishFD() && activity.getlD() != todayMillis()) {
                    return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 3:
                if (activity.getlD() < firstDayOfThisMonth()) {
                    return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 4:
                if (activity.ishFD() && activity.getlD() < thisMondayMillis()) {
                    return goalDurationForFixedDay(activity.getCustomWeekTime(), whatDayOfWeekToday());
                } else if (!activity.ishFD() && activity.getlD() < thisMondayMillis()) {
                    return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 5:
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 7:
                if (activity.getlD() < activity.getsD()) {
                    return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
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
                if (activity.ishFD() && activity.getlD() != todayMillis()) {
                    return goalDurationForFixedDayLong(activity.getCustomWeekTime(), whatDayOfWeekToday());
                } else if (!activity.ishFD() && activity.getlD() != todayMillis()) {
                    return activity.getDur();
                }
                return activity.getRe();
            case 3:
                if (activity.getlD() < firstDayOfThisMonth()) {
                    return activity.getDur();
                }
                return activity.getRe();
            case 4:
                if (activity.ishFD() && activity.getlD() < thisMondayMillis()) {
                    return goalDurationForFixedDayLong(activity.getCustomWeekTime(), whatDayOfWeekToday());
                } else if (!activity.ishFD() && activity.getlD() < thisMondayMillis()) {
                    return activity.getDur();
                }
                return activity.getRe();
            case 5:
                return activity.getRe();
            case 7:
                if (activity.getlD() < activity.getsD()) {
                    return activity.getDur();
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
     * @return the updated activity
     */
    public static CustomActivity updateActivity(CustomActivity customActivity, ActivityTime activityTime) {
        long todayMillis = CustomActivityHelper.todayMillis();
        long firstDayOfThisMonth = CustomActivityHelper.firstDayOfThisMonth();
        long thisMonday = CustomActivityHelper.thisMondayMillis();
        customActivity.setaT(customActivity.getaT() + activityTime.getT());
        boolean updateLastDay = false;
        switch (customActivity.gettN()) {
            case 2:
                if (customActivity.ishFD()) {
                    if (activityTime.getD() == todayMillis && CustomActivityHelper.todayIsAFixedDayAndWhat(customActivity.getCustomWeekTime()) != 0
                            && customActivity.geteD() == 0L) {
                        if (customActivity.getlD() != todayMillis) {
                            customActivity.setsF(activityTime.getT());
                            customActivity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(customActivity.getCustomWeekTime()) - activityTime.getT()), 0L));
                            updateLastDay = true;
                        } else {
                            customActivity.setsF(customActivity.getsF() + activityTime.getT());
                            customActivity.setRe(Math.max((customActivity.getRe() - activityTime.getT()), 0L));
                            updateLastDay = true;
                        }
                    } else if (activityTime.getD() == todayMillis && CustomActivityHelper.todayIsAFixedDayAndWhat(customActivity.getCustomWeekTime()) != 0
                            && customActivity.getsD() == 0L && customActivity.geteD() != 0L && customActivity.getlD() < customActivity.geteD()) {
                        if (customActivity.getlD() != todayMillis) {
                            customActivity.setsF(activityTime.getT());
                            customActivity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(customActivity.getCustomWeekTime()) - activityTime.getT()), 0L));
                            updateLastDay = true;
                        } else {
                            customActivity.setsF(customActivity.getsF() + activityTime.getT());
                            customActivity.setRe(Math.max((customActivity.getRe() - activityTime.getT()), 0L));
                            updateLastDay = true;
                        }
                    }
                } else {
                    if (activityTime.getD() == todayMillis) {
                        if (customActivity.getlD() != todayMillis) {
                            customActivity.setsF(activityTime.getT());
                            customActivity.setRe(Math.max((customActivity.getDur() - activityTime.getT()), 0L));
                            updateLastDay = true;
                        } else {
                            customActivity.setsF(customActivity.getsF() + activityTime.getT());
                            customActivity.setRe(Math.max((customActivity.getRe() - activityTime.getT()), 0L));
                            updateLastDay = true;
                        }
                    }
                }
            case 3:
                if (customActivity.geteD() == 0L) {
                    if (activityTime.getD() >= firstDayOfThisMonth && customActivity.getlD() < firstDayOfThisMonth) {
                        customActivity.setsF(activityTime.getT());
                        customActivity.setRe(Math.max((customActivity.getDur() - activityTime.getT()), 0L));
                        updateLastDay = true;
                    } else if (activityTime.getD() > firstDayOfThisMonth && customActivity.getlD() >= firstDayOfThisMonth) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        customActivity.setRe(Math.max((customActivity.getRe() - activityTime.getT()), 0L));
                        updateLastDay = true;
                    }
                } else {
                    if (activityTime.getD() >= firstDayOfThisMonth && customActivity.getlD() < firstDayOfThisMonth && todayMillis <= customActivity.geteD()) {
                        customActivity.setsF(activityTime.getT());
                        customActivity.setRe(Math.max((customActivity.getDur() - activityTime.getT()), 0L));
                        updateLastDay = true;
                    } else if (activityTime.getD() > firstDayOfThisMonth && customActivity.getlD() >= firstDayOfThisMonth && todayMillis < customActivity.geteD()) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        customActivity.setRe(Math.max((customActivity.getRe() - activityTime.getT()), 0L));
                        updateLastDay = true;
                    }
                }
            case 4:
                if (customActivity.geteD() == 0L) {
                    if (activityTime.getD() >= thisMonday && customActivity.getlD() < thisMonday) {
                        customActivity.setsF(activityTime.getT());
                        customActivity.setRe(Math.max((customActivity.getDur() - activityTime.getT()), 0L));
                        updateLastDay = true;
                    } else if (activityTime.getD() > thisMonday && customActivity.getlD() >= thisMonday) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        customActivity.setRe(Math.max((customActivity.getRe() - activityTime.getT()), 0L));
                        updateLastDay = true;
                    }
                } else {
                    if (activityTime.getD() >= thisMonday && customActivity.getlD() < thisMonday && todayMillis <= customActivity.geteD()) {
                        customActivity.setsF(activityTime.getT());
                        customActivity.setRe(Math.max((customActivity.getDur() - activityTime.getT()), 0L));
                        updateLastDay = true;
                    } else if (activityTime.getD() > thisMonday && customActivity.getlD() >= thisMonday && todayMillis < customActivity.geteD()) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        customActivity.setRe(Math.max((customActivity.getRe() - activityTime.getT()), 0L));
                        updateLastDay = true;
                    }
                }
            case 5:
                if (customActivity.geteD() == 0L) {
                    if (customActivity.getsF() < customActivity.getDur()) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        customActivity.setRe(Math.max((customActivity.getRe() - activityTime.getT()), 0L));
                        updateLastDay = true;
                    }
                } else {
                    if (customActivity.getsF() < customActivity.getDur() && todayMillis < customActivity.geteD()) {
                        customActivity.setsF(customActivity.getsF() + activityTime.getT());
                        customActivity.setRe(Math.max((customActivity.getRe() - activityTime.getT()), 0L));
                        updateLastDay = true;
                    }
                }
            case 6:
                if (activityTime.getD() >= customActivity.getsD() && activityTime.getD() <= customActivity.geteD()) {
                    customActivity.setsF(customActivity.getsF() + activityTime.getT());
                    updateLastDay = true;
                }
            case 7:
                System.out.println(todayMillis + " " + customActivity.getsD() + " " + customActivity.geteD() + " " + customActivity.getlD());
                if (activityTime.getD() >= customActivity.getsD() && activityTime.getD() <= customActivity.geteD() && customActivity.getlD() < customActivity.getsD()) {
                    customActivity.setsF(activityTime.getT());
                    customActivity.setRe(Math.max((customActivity.getDur() - activityTime.getT()), 0L));
                    updateLastDay = true;
                } else if (activityTime.getD() >= customActivity.getsD() && activityTime.getD() <= customActivity.geteD() && customActivity.getlD() >= customActivity.getsD()) {
                    customActivity.setsF(customActivity.getsF() + activityTime.getT());
                    customActivity.setRe(Math.max((customActivity.getRe() - activityTime.getT()), 0L));
                    updateLastDay = true;
                }
            case 8:
                if (customActivity.geteD() == 0L) {
                    if (activityTime.getD() >= thisMonday && CustomActivityHelper.todayIsAFixedDayAndWhat(customActivity.getCustomWeekTime()) != 0 && activityTime.getD() == todayMillis) {
                        if (customActivity.getlD() != todayMillis) {
                            customActivity.setsF(activityTime.getT());
                            customActivity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(customActivity.getCustomWeekTime()) - activityTime.getT()), 0L));
                            updateLastDay = true;
                        } else {
                            customActivity.setsF(customActivity.getsF() + activityTime.getT());
                            customActivity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(customActivity.getCustomWeekTime()) - activityTime.getT()), 0L));
                            updateLastDay = true;
                        }
                    }
                }
        }
        if (updateLastDay) {
            if (customActivity.getlD() < activityTime.getD()) {
                customActivity.setlD(activityTime.getD());
            }
        }
        return customActivity;
    }

    /*public static void setRemainingFieldFixedDays(long time) {
        customActivity.setRe(Math.max((CustomActivityHelper.todayIsAFixedDayAndDuration(customActivity.getCustomWeekTime()) - time), 0L));
    }

    public static void setRemainingFieldInsert(long time) {
        customActivity.setRe(Math.max((customActivity.getDur() - time), 0L));
    }

    public static void setRemainingFieldUpdate(long time) {
        customActivity.setRe(Math.max((customActivity.getRe() - time), 0L));
    }*/


}
