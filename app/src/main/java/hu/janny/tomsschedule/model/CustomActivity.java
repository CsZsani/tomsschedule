package hu.janny.tomsschedule.model;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomActivity {

    public String name, color, note;
    public int priority;

    public Boolean isTimeMeasured, isSumTime = false, isTime = false, isCustomTime = false;
    public long sumTime = 0, time = 0;
    public Map<String, Object> customTime = new HashMap<>();  // Long, Long

    public Boolean hasDeadline, regularity, daily = false, weekly = false, monthly = false, custom = false, isInterval = false /*areSingleDays = false*/;
    public long deadline = 0, customIntervalFrom = 0, customIntervalTo = 0;
    public List<Object> weeklyDays = new ArrayList<>(), monthlyDays = new ArrayList<>(); // String
    /*public List<Object> customDays = new ArrayList<>();*/ // Long

    public Map<String, Object> lastTenTime = new HashMap<>(); // Long, Long

    public CustomActivity() {}

    public CustomActivity(String name, String color, String note, int priority, Boolean isTimeMeasured,
                          Boolean hasDeadline, Boolean regularity) {
        this.name = name;
        this.color = color;
        this.note = note;
        this.priority = priority;
        this.isTimeMeasured = isTimeMeasured;
        this.hasDeadline = hasDeadline;
        this.regularity = regularity;
        weeklyDays.add("MONDAY");
        weeklyDays.add("FRIDAY");
        lastTenTime.put(String.valueOf(System.currentTimeMillis()), 1200L);
        lastTenTime.put(String.valueOf(System.currentTimeMillis()), 60L);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("color", color);
        result.put("priority", priority);
        result.put("note", note);
        result.put("isTimeMeasured", isTimeMeasured);
        result.put("isSumTime", isSumTime);
        result.put("isTime", isTime);
        result.put("isCustomTime", isCustomTime);
        result.put("sumTime", sumTime);
        result.put("time", time);
        result.put("customTime", customTime);
        result.put("hasDeadline", hasDeadline);
        result.put("regularity", regularity);
        result.put("daily", daily);
        result.put("weekly", weekly);
        result.put("monthly", monthly);
        result.put("custom", custom);
        result.put("isInterval", isInterval);
        //result.put("areSingleDays", areSingleDays);
        result.put("deadline", deadline);
        result.put("customIntervalFrom", customIntervalFrom);
        result.put("customIntervalTo", customIntervalTo);
        result.put("weeklyDays", weeklyDays);
        result.put("monthlyDays", monthlyDays);
        //result.put("customDays", customDays);
        result.put("lastTenTime", lastTenTime);

        return result;
    }

    public void setLastTenTime(Map<String, Object> lastTenTime) {
        this.lastTenTime = lastTenTime;
    }

    public void setIsSumTime(Boolean isSumTime) {
        this.isSumTime = isSumTime;
    }

    public void setIsTime(Boolean isTime) {
        this.isTime = isTime;
    }

    public void setIsCustomTime(Boolean isCustomTime) {
        this.isCustomTime = isCustomTime;
    }

    public void setSumTime(long sumTime) {
        this.sumTime = sumTime;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setCustomTime(Map<String, Object> customTime) {
        this.customTime = customTime;
    }

    public void setDaily(Boolean daily) {
        this.daily = daily;
    }

    public void setWeekly(Boolean weekly) {
        this.weekly = weekly;
    }

    public void setMonthly(Boolean monthly) {
        this.monthly = monthly;
    }

    public void setCustom(Boolean custom) {
        this.custom = custom;
    }

    public void setInterval(Boolean interval) {
        isInterval = interval;
    }

    /*public void setAreSingleDays(Boolean areSingleDays) {
        this.areSingleDays = areSingleDays;
    }*/

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public void setCustomIntervalFrom(long customIntervalFrom) {
        this.customIntervalFrom = customIntervalFrom;
    }

    public void setCustomIntervalTo(long customIntervalTo) {
        this.customIntervalTo = customIntervalTo;
    }

    public void setWeeklyDays(List<Object> weeklyDays) {
        this.weeklyDays = weeklyDays;
    }

    public void setMonthlyDays(List<Object> monthlyDays) {
        this.monthlyDays = monthlyDays;
    }

    /*public void setCustomDays(List<Object> customDays) {
        this.customDays = customDays;
    }*/
}