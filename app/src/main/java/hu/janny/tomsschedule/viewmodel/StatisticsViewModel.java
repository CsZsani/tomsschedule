package hu.janny.tomsschedule.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import hu.janny.tomsschedule.model.ActivityFilter;
import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.model.repository.Repository;
import hu.janny.tomsschedule.model.repository.UserRepository;

public class StatisticsViewModel extends AndroidViewModel {

    private final Repository repository;
    private final UserRepository userRepository;
    private final LiveData<User> user;
    private final LiveData<List<ActivityFilter>> filterActivities;
    private final MutableLiveData<List<ActivityTime>> timesList;


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

    public void filterAll() {
        repository.getAllTimes();
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<List<ActivityFilter>> getFilterActivities() {
        return filterActivities;
    }
}
