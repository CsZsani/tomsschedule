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
        if(activity.getDl() != 0L) {
            return DateConverter.longMillisToStringForSimpleDateDialog(activity.getDl());
        } else if(activity.getsD() != 0L && activity.geteD() != 0L) {
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
}
