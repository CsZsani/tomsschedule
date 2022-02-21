package hu.janny.tomsschedule.model.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import hu.janny.tomsschedule.model.ActivityFilter;
import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.ActivityWithTimes;
import hu.janny.tomsschedule.model.CustomActivity;

public class Repository {

    private final MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activitiesWithTimesData = new MutableLiveData<>();
    private final MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activityByIdWithTimesData = new MutableLiveData<>();
    private final MutableLiveData<CustomActivity> activitiesData = new MutableLiveData<>();
    private Map<CustomActivity, List<ActivityTime>> activitiesWithTimes;
    private Map<CustomActivity, List<ActivityTime>> activityByIdWithTimes;
    private final LiveData<Map<CustomActivity, List<ActivityTime>>> allActivitiesWithTimes;
    private final LiveData<List<CustomActivity>> activities;
    private final LiveData<List<ActivityWithTimes>> activitiesWithTimesEntities;
    private final LiveData<List<ActivityFilter>> filterActivities;
    private CustomActivity activity;

    private final MutableLiveData<ActivityWithTimes> activityWithTimesEntity = new MutableLiveData<>();
    private ActivityWithTimes activityWithTimes;

    private final MutableLiveData<List<ActivityWithTimes>> activityWithTimesFilterList = new MutableLiveData<>();
    private List<ActivityWithTimes> activityWithTimesFilter;

    private final MutableLiveData<List<ActivityTime>> allActivitiesTime = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityTime>> oneActivitiesTime = new MutableLiveData<>();
    private List<ActivityTime> allTimes;
    private List<ActivityTime> oneTimes;

    private final CustomActivityDao customActivityDao;
    private final ActivityTimeDao activityTimeDao;

    public Repository(Application application) {
        ActivityRoomDatabase db;
        db = ActivityRoomDatabase.getDatabase(application);
        customActivityDao = db.customActivityDao();
        activityTimeDao = db.activityTimeDao();

        allActivitiesWithTimes = customActivityDao.getAllActivitiesWithTimes();
        activities = customActivityDao.getActivitiesList();
        activitiesWithTimesEntities = customActivityDao.getActivitiesWithTimes();
        filterActivities = customActivityDao.getActivityFilter();
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

    Handler handlerSingleActivityWithTimesEntity = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            activityWithTimesEntity.setValue(activityWithTimes);
        }
    };

    Handler handlerFilter = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            activityWithTimesFilterList.setValue(activityWithTimesFilter);
        }
    };

    Handler handlerSingleActivityWithTimes = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            activityByIdWithTimesData.setValue(activityByIdWithTimes);
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

    public long insertActivity(CustomActivity customActivity) {
        final long[] id = new long[1];
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            id[0] = customActivityDao.insertActivity(customActivity);
        });
        executor.shutdown();
        return id[0];
    }

    public void insertFirstActivityTime(long id) {
        Calendar cal = Calendar.getInstance();
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date  = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year, month, date);
        long todayMillis = cal.getTimeInMillis();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityTimeDao.insertActivityTime(new ActivityTime(id, todayMillis, 0L));
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

    public void updateActivity(CustomActivity customActivity) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.updateActivity(customActivity);
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

    public void deleteActivityById(long id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.deleteActivityById(id);
        });
        executor.shutdown();
    }

    public void getActivityByNameWithTimes(String name) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activitiesWithTimes = customActivityDao.getActivityByNameWithTimes(name);
            handler.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getActivityByIdWithTimes(long id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activitiesWithTimes = customActivityDao.getActivityByIdWithTimes(id);
            handler.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getSingleActivityByIdWithTimes(long id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityByIdWithTimes = customActivityDao.getActivityByIdWithTimes(id);
            handlerSingleActivityWithTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getSingleActivityByIdWithTimesEntity(long id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityWithTimes = customActivityDao.getActivityWithTimesEntity(id);
            handlerSingleActivityWithTimesEntity.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getFilterSomeAcivity(List<Long> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityWithTimesFilter = customActivityDao.getActivitiesWithTimesFilter(list);
            handlerFilter.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getActivityByName(String name) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activity = customActivityDao.getActivityByName(name);
            handlerSingleActivity.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public int getActivityIdByName(String name) {
        final int[] id = new int[1];
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            id[0] = customActivityDao.getIdByName(name);
        });
        executor.shutdown();
        return id[0];
    }

    public void getActivityById(long id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activity = customActivityDao.getActivityById(id);
            handlerSingleActivity.sendEmptyMessage(0);
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

    public boolean updateOrInsertTime(ActivityTime activityTime) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> activityTimeDao.insertOrUpdateTime(activityTime));
        while(!future.isDone()) {
            Thread.sleep(300);
        }
        Boolean result = future.get();
        boolean canceled = future.cancel(true);
        executor.shutdown();
        return result;
    }

    public void updateOrInsertTimeSingle(ActivityTime activityTime) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> activityTimeDao.insertOrUpdateTime(activityTime));
        executor.shutdown();
    }

    public void deleteTime(ActivityTime activityTime) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityTimeDao.deleteActivityTime(activityTime);
        });
        executor.shutdown();
    }

    public void deleteTimesByActivityId(long id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityTimeDao.deleteActivityTimeByActivityId(id);
        });
        executor.shutdown();
    }

    public void getAllExactDates(long date) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getAllExactDate(date);
            handlerAllTimes.sendEmptyMessage(0);
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

    public void getSomeExactDates(long date, List<Long> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getSomeExactDate(date, list);
            handlerAllTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getSomeLaterDates(long from, List<Long> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getSomeLaterDates(from, list);
            handlerAllTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getSomeBetweenTwoDates(long from, long to, List<Long> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getSomeBetweenTwoDates(from, to, list);
            handlerAllTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    public void getAllTimes() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getAllAllTheTime();
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

    public MutableLiveData<ActivityWithTimes> getActivityWithTimesEntity() {
        return activityWithTimesEntity;
    }

    public MutableLiveData<CustomActivity> getActivitiesData() {
        return activitiesData;
    }

    public LiveData<Map<CustomActivity, List<ActivityTime>>> getAllActivitiesWithTimes() {
        return allActivitiesWithTimes;
    }

    public MutableLiveData<List<ActivityTime>> getAllActivitiesTime() {
        return allActivitiesTime;
    }

    public MutableLiveData<List<ActivityTime>> getOneActivitiesTime() {
        return oneActivitiesTime;
    }

    public LiveData<List<CustomActivity>> getActivities() {
        return activities;
    }

    public MutableLiveData<Map<CustomActivity, List<ActivityTime>>> getActivityByIdWithTimesData() {
        return activityByIdWithTimesData;
    }

    public LiveData<List<ActivityWithTimes>> getActivitiesWithTimesEntities() {
        return activitiesWithTimesEntities;
    }

    public LiveData<List<ActivityFilter>> getFilterActivities() {
        return filterActivities;
    }
}
