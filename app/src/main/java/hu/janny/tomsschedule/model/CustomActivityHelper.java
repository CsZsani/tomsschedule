package hu.janny.tomsschedule.model;

import android.view.View;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import hu.janny.tomsschedule.R;

public final class CustomActivityHelper {

    private final static List<String> list = Arrays.asList("SLEEPING", "WORKOUT", "COOKING", "HOUSEWORK",
            "SHOPPING", "WORK", "SCHOOL", "LEARNING", "TRAVELLING", "READING", "RELAXATION", "HOBBY");

    public static boolean isFixActivity(String string) {
        if(list.contains(string)) {
            return true;
        }
        return false;
    }

    private final static String NULL_MIN = "0h 0m";

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

    public static long getHowManyTimeWasSpentTodayOnAct(List<ActivityTime> list, long todayMillis) {
        ActivityTime activityTime = list.stream()
                .filter(at -> at.getD() == todayMillis)
                .findAny()
                .orElse(null);
        if(activityTime != null) {
            return activityTime.getT();
        } else {
            return 0L;
        }
    }

    public static long getHowManyTimeWasSpentOnActInInterval(List<ActivityTime> list, long from , long to) {
        List<ActivityTime> activityTime = list.stream()
                .filter(at -> at.getD() > from && at.getD() < to)
                .collect(Collectors.toList());
        long sumTime = 0L;
        for(ActivityTime at : activityTime) {
            sumTime += at.getT();
        }
        return sumTime;
    }

    public static long getHowManyTimeWasSpentFrom(List<ActivityTime> list, long from) {
        List<ActivityTime> activityTime = list.stream()
                .filter(at -> at.getD() > from)
                .collect(Collectors.toList());
        long sumTime = 0L;
        for(ActivityTime at : activityTime) {
            sumTime += at.getT();
        }
        return sumTime;
    }

    public static String detailsOnCardsDeadline(CustomActivity activity) {
        if(activity.getsD() != 0L && activity.geteD() != 0L) {
            String text = DateConverter.longMillisToStringForSimpleDateDialog(activity.getsD())
                    + "-" + DateConverter.longMillisToStringForSimpleDateDialog(activity.geteD());
            return text;
        } else if(activity.getsD() == 0L && activity.geteD() != 0L) {
            return DateConverter.longMillisToStringForSimpleDateDialog(activity.geteD());
        } else {
            return "";
        }
    }

    public static int detailsOnCardRegularity(CustomActivity activity) {
        if(activity.getReg() > 0) {
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

    public static String detailsOnCardDuration(CustomActivity activity) {
        if(activity.gettT() > 0 && activity.gettT() != 5) {
            return DateConverter.durationConverterFromLongToString(activity.getDur());
        }
        return "";
    }

    public static long remainingTime(CustomActivity activity, List<ActivityTime> list) {
        Calendar cal = Calendar.getInstance();
        return 0L;
    }

    public static long todayMillis() {
        LocalDate localDate = LocalDate.now();
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    public static long thisMondayMillis() {
        LocalDate localDate = LocalDate.now();
        LocalDate mon = localDate.with(DayOfWeek.MONDAY);
        Instant instant = mon.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    public static long firstDayOfThisMonth() {
        LocalDate localDate = LocalDate.now();
        LocalDate fd = localDate.withDayOfMonth(1);
        Instant instant = fd.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

    public static DayOfWeek whatDayOfWeekToday() {
        LocalDate dt = LocalDate.now();
        return dt.getDayOfWeek();
    }

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

    public static int todayIsAFixedDayAndWhat(CustomWeekTime customWeekTime) {
        DayOfWeek today = whatDayOfWeekToday();
        switch (today) {
            case MONDAY:
                if(customWeekTime.getMon() != -1L) {
                    return 1;
                }
            case TUESDAY:
                if(customWeekTime.getTue() != -1L) {
                    return 2;
                }
            case WEDNESDAY:
                if(customWeekTime.getWed() != -1L) {
                    return 3;
                }
            case THURSDAY:
                if(customWeekTime.getThu() != -1L) {
                    return 4;
                }
            case FRIDAY:
                if(customWeekTime.getFri() != -1L) {
                    return 5;
                }
            case SATURDAY:
                if(customWeekTime.getSat() != -1L) {
                    return 6;
                }
            case SUNDAY:
                if(customWeekTime.getSun() != -1L) {
                    return 7;
                }
        }
        return 0;
    }

    public static long todayIsAFixedDayAndDuration(CustomWeekTime customWeekTime) {
        DayOfWeek today = whatDayOfWeekToday();
        switch (today) {
            case MONDAY:
                if(customWeekTime.getMon() != -1L) {
                    return customWeekTime.getMon();
                }
            case TUESDAY:
                if(customWeekTime.getTue() != -1L) {
                    return customWeekTime.getTue();
                }
            case WEDNESDAY:
                if(customWeekTime.getWed() != -1L) {
                    return customWeekTime.getWed();
                }
            case THURSDAY:
                if(customWeekTime.getThu() != -1L) {
                    return customWeekTime.getThu();
                }
            case FRIDAY:
                if(customWeekTime.getFri() != -1L) {
                    return customWeekTime.getFri();
                }
            case SATURDAY:
                if(customWeekTime.getSat() != -1L) {
                    return customWeekTime.getSat();
                }
            case SUNDAY:
                if(customWeekTime.getSun() != -1L) {
                    return customWeekTime.getSun();
                }
        }
        return 0L;
    }

    public static String getSoFar(CustomActivity activity) {
        switch (activity.gettN()) {
            case 1:
                return "-";
            case 2:
            case 8:
                if(activity.getlD() != todayMillis()) {
                    return NULL_MIN;
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 3:
                if(activity.getlD() < firstDayOfThisMonth()) {
                    return NULL_MIN;
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 4:
                if(activity.getlD() < thisMondayMillis()) {
                    return NULL_MIN;
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 5:
                return DateConverter.durationConverterFromLongToString(activity.getsF());
            case 6:
            case 7:
                if(activity.getlD() < activity.getsD()) {
                    return NULL_MIN;
                }
                return DateConverter.durationConverterFromLongToString(activity.getsF());
        }
        return "?";
    }

    public static String getRemaining(CustomActivity activity) {
        switch (activity.gettN()) {
            case 1:
            case 6:
                return "-";
            case 2:
            case 8:
                if(activity.ishFD() && activity.getlD() != todayMillis()) {
                    return goalDurationForFixedDay(activity.getCustomWeekTime(), whatDayOfWeekToday());
                }else if(!activity.ishFD() && activity.getlD() != todayMillis()) {
                    return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 3:
                if(activity.getlD() < firstDayOfThisMonth()) {
                    return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 4:
                if(activity.ishFD() && activity.getlD() < thisMondayMillis()) {
                    return goalDurationForFixedDay(activity.getCustomWeekTime(), whatDayOfWeekToday());
                }else if(!activity.ishFD() && activity.getlD() < thisMondayMillis()) {
                    return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 5:
                return DateConverter.durationConverterFromLongToString(activity.getRe());
            case 7:
                if(activity.getlD() < activity.getsD()) {
                    return DateConverter.durationConverterFromLongToStringForADay(activity.getDur());
                }
                return DateConverter.durationConverterFromLongToString(activity.getRe());
        }
        return "?";
    }


}
