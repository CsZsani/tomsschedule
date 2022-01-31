package hu.janny.tomsschedule.model.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.model.UserState;

public class Repository {

    private final MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activitiesWithTimesData = new MutableLiveData<>();
    private final MutableLiveData<CustomActivity> activitiesData = new MutableLiveData<>();
    private Map<CustomActivity, List<ActivityTime>> activitiesWithTimes;
    private final LiveData<Map<CustomActivity, List<ActivityTime>>> allActivitiesWithTimes;
    private CustomActivity activity;

    private final MutableLiveData<User> userData = new MutableLiveData<>();
    private User user;

    private final MutableLiveData<List<ActivityTime>> allActivitiesTime = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityTime>> oneActivitiesTime = new MutableLiveData<>();
    private List<ActivityTime> allTimes;
    private List<ActivityTime> oneTimes;

    private final CustomActivityDao customActivityDao;
    private final ActivityTimeDao activityTimeDao;
    private final UserDao userDao;

    public Repository(Application application) {
        ActivityRoomDatabase db;
        db = ActivityRoomDatabase.getDatabase(application);
        customActivityDao = db.customActivityDao();
        activityTimeDao = db.activityTimeDao();
        userDao = db.userDao();
        allActivitiesWithTimes = customActivityDao.getAllActivitiesWithTimes(UserState.getUser().uid);
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            activitiesWithTimesData.setValue(activitiesWithTimes);
        }
    };

    Handler handlerSingleActivity = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            activitiesData.setValue(activity);
        }
    };

    Handler handlerUser = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            userData.setValue(user);
        }
    };

    Handler handlerAllTimes = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            allActivitiesTime.setValue(allTimes);
        }
    };

    Handler handlerOneTimes = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            oneActivitiesTime.setValue(oneTimes);
        }
    };

    public void insertActivity(CustomActivity customActivity) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.insertActivity(customActivity);
        });
        executor.shutdown();
    }

    public void deleteActivity(CustomActivity customActivity) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.deleteActivity(customActivity);
        });
        executor.shutdown();
    }

    public void deleteActivityByName(String name) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.deleteActivityByName(name);
        });
        executor.shutdown();
    }

    public void deleteActivityById(int id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.deleteActivityById(id);
        });
        executor.shutdown();
    }

    public void getActivityByNameWithTimes(String name) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activitiesWithTimes = customActivityDao.getActivityByNameWithTimes(UserState.getUser().uid, name);
            handler.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getActivityByNameWithTimes(int id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activitiesWithTimes = customActivityDao.getActivityByIdWithTimes(UserState.getUser().uid, id);
            handler.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getActivityByName(String name) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activity = customActivityDao.getActivityByName(UserState.getUser().uid, name);
            handlerSingleActivity.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getActivityByName(int id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activity = customActivityDao.getActivityById(UserState.getUser().uid, id);
            handlerSingleActivity.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void insertUser(User user) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            userDao.insertUser(user);
        });
        executor.shutdown();
    }

    public void updateUser(User user) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            userDao.updateUser(user);
        });
        executor.shutdown();
    }

    public void getUserById(String id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            user = userDao.getUserById(id);
            handlerUser.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void insertTime(ActivityTime activityTime) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityTimeDao.insertActivityTime(activityTime);
        });
        executor.shutdown();
    }

    public void updateTime(ActivityTime activityTime) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityTimeDao.updateActivityTime(activityTime);
        });
        executor.shutdown();
    }

    public void deleteTime(ActivityTime activityTime) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityTimeDao.deleteActivityTime(activityTime);
        });
        executor.shutdown();
    }

    public void getAllLaterDates(long from) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getAllLaterDates(from);
            handlerAllTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getAllBetweenTwoDates(long from, long to) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getAllBetweenTwoDates(from, to);
            handlerAllTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getOneByIdLaterDates(int id, long from) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            oneTimes = activityTimeDao.getOneByIdLaterDates(id, from);
            handlerOneTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getOneByIdBetweenTwoDates(int id, long from, long to) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            oneTimes = activityTimeDao.getOneByIdBetweenTwoDates(id, from, to);
            handlerOneTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public MutableLiveData<Map<CustomActivity, List<ActivityTime>>> getActivitiesWithTimesData() {
        return activitiesWithTimesData;
    }

    public MutableLiveData<CustomActivity> getActivitiesData() {
        return activitiesData;
    }

    public LiveData<Map<CustomActivity, List<ActivityTime>>> getAllActivitiesWithTimes() {
        return allActivitiesWithTimes;
    }

    public MutableLiveData<User> getUserData() {
        return userData;
    }

    public MutableLiveData<List<ActivityTime>> getAllActivitiesTime() {
        return allActivitiesTime;
    }

    public MutableLiveData<List<ActivityTime>> getOneActivitiesTime() {
        return oneActivitiesTime;
    }
}
