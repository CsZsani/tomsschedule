package hu.janny.tomsschedule.model;

import java.util.Arrays;
import java.util.List;

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
}
