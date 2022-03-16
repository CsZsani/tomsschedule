package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import hu.janny.tomsschedule.model.helper.ActivityFilter;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.model.repository.Repository;
import hu.janny.tomsschedule.model.repository.UserRepository;

public class StatisticsViewModel extends AndroidViewModel {

    private final Repository repository;
    private final UserRepository userRepository;
    private final LiveData<User> user;
    private final LiveData<List<ActivityFilter>> filterActivities;
    private final MutableLiveData<List<ActivityTime>> timesList;

    private int pPeriodType = 0;
    private int pActivityNum = 0;
    private long fromTime = 0L;
    private long toTime = 0L;
    private List<Long> actsList = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    private List<String> names = new ArrayList<>();


    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        userRepository = new UserRepository(application);
        user = userRepository.getCurrentUser();
        filterActivities = repository.getFilterActivities();
        timesList = repository.getAllActivitiesTime();
    }

    public void filterExactDay(long day, List<Long> list) {
        if(list.isEmpty()) {
            repository.getAllExactDates(day);
        } else {
            repository.getSomeExactDates(day, list);
        }
    }

    public void filterFrom(long from, List<Long> list) {
        if(list.isEmpty()) {
            repository.getAllLaterDates(from);
        } else {
            repository.getSomeLaterDates(from, list);
        }
    }

    public void filterFromTo(long from, long to, List<Long> list) {
        if(list.isEmpty()) {
            repository.getAllBetweenTwoDates(from, to);
        } else {
            repository.getSomeBetweenTwoDates(from, to, list);
        }
    }

    public void filterAll(List<Long> list) {
        if(list.isEmpty()) {
            repository.getAllTimes();
        } else {
            repository.getSomeAllTimes(list);
        }
    }

    public LiveData<User> getUser() {
        return user;
    }

    /**
     * Returns the activities the user has.
     * @return list of activities to filter
     */
    public LiveData<List<ActivityFilter>> getFilterActivities() {
        return filterActivities;
    }

    public MutableLiveData<List<ActivityTime>> getTimesList() {
        return timesList;
    }

    public int getpPeriodType() {
        return pPeriodType;
    }

    public void setpPeriodType(int pPeriodType) {
        this.pPeriodType = pPeriodType;
    }

    public int getpActivityNum() {
        return pActivityNum;
    }

    public void setpActivityNum(int pActivityNum) {
        this.pActivityNum = pActivityNum;
    }

    public List<Long> getActsList() {
        return actsList;
    }

    public void setActsList(List<Long> actsList) {
        this.actsList = actsList;
    }

    public List<Integer> getColors() {
        return colors;
    }

    public void setColors(List<Integer> colors) {
        this.colors = colors;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public void setFromTime(long fromTime) {
        this.fromTime = fromTime;
    }

    public long getFromTime() {
        return fromTime;
    }

    public long getToTime() {
        return toTime;
    }

    public void setToTime(long toTime) {
        this.toTime = toTime;
    }
}
