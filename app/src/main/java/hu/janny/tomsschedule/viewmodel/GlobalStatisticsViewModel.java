package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.ActivityTimeFirebase;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.repository.GlobalStatisticsRepository;

public class GlobalStatisticsViewModel extends AndroidViewModel {

    private GlobalStatisticsRepository repository;
    private final MutableLiveData<List<ActivityTimeFirebase>> timesList;

    private int gender = 0;
    private int ageGroup = -1;
    private String name = "";
    private long from = 0L;
    private long to = 0L;

    public GlobalStatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new GlobalStatisticsRepository(application);
        timesList = repository.getActivityTimesFilterList();
    }

    public MutableLiveData<List<ActivityTimeFirebase>> getTimesList() {
        return timesList;
    }

    public void findYesterdayData(String name) {
        repository.getExactDayData(name, CustomActivityHelper.minusDaysMillis(1));
    }

    public void findExactDayData(String name, long millis) {
        repository.getExactDayData(name, millis);
    }

    public void findActivity(String name) {
        repository.getActivityData(name);
    }

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
}
