package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import hu.janny.tomsschedule.model.entities.ActivityTimeFirebase;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.repository.GlobalStatisticsRepository;

/**
 * View model of the global statistics.
 */
public class GlobalStatisticsViewModel extends AndroidViewModel {

    private final GlobalStatisticsRepository repository;
    private final MutableLiveData<List<ActivityTimeFirebase>> timesList;

    // Parameters to share between filter fragment and the global statistics fragment
    private int gender = 0;
    private int ageGroup = -1;
    private String name = "";
    private long from = 0L;
    private long to = 0L;
    private boolean loading = false;

    public GlobalStatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new GlobalStatisticsRepository(application);
        timesList = repository.getActivityTimesFilterList();
    }

    public MutableLiveData<List<ActivityTimeFirebase>> getTimesList() {
        return timesList;
    }

    /**
     * Searches for the global data of the given activity on yesterday.
     * @param name name of the fix activity
     */
    public void findYesterdayData(String name) {
        repository.getExactDayData(name, CustomActivityHelper.minusDaysMillis(1));
    }

    /**
     * Searches for the global data of the given activity on the given day.
     * @param name name of the fix activity
     * @param millis day in long millis
     */
    public void findExactDayData(String name, long millis) {
        repository.getExactDayData(name, millis);
    }

    /**
     * Searches for the global data of the given activity. This is used when we search a longer period,
     * not just a day.
     * @param name name of the fix activity
     */
    public void findActivity(String name) {
        repository.getActivityData(name);
    }

    // Setters and getters of parameters to share between filter fragment and the global statistics fragment

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(int ageGroup) {
        this.ageGroup = ageGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }
}
