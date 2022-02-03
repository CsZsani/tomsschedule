package hu.janny.tomsschedule.model;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class FixActivity {

    private final static List<String>  list = Arrays.asList(new String[]{"SLEEPING", "WORKOUT", "COOKING", "HOUSEWORK",
    "SHOPPING", "WORK", "SCHOOL", "LEARNING", "TRAVELLING", "READING", "RELAXATION", "HOBBY"});

    public static boolean isFixActivity(String string) {
        if(list.contains(string)) {
            return true;
        }
        return false;
    }
}
