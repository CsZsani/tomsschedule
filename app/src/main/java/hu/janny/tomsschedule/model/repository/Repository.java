package hu.janny.tomsschedule.model.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import hu.janny.tomsschedule.model.helper.ActivityFilter;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.ActivityWithTimes;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class Repository {

    private final CustomActivityDao customActivityDao;
    private final ActivityTimeDao activityTimeDao;

    // An activity with its times - edit, detail
    private final MutableLiveData<ActivityWithTimes> activityWithTimesEntity = new MutableLiveData<>();
    // An activity with its times - edit, detail
    private ActivityWithTimes activityWithTimes;
    // An activity - add time, timer
    private final MutableLiveData<CustomActivity> activitiesData = new MutableLiveData<>();
    // An activity - add time, timer
    private CustomActivity activity;
    // All the activities with their times - home
    private final LiveData<List<ActivityWithTimes>> activitiesWithTimesEntities;
    // List of activities to filter
    private final LiveData<List<ActivityFilter>> filterActivities;


    private final MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activitiesWithTimesData = new MutableLiveData<>();
    private final MutableLiveData<Map<CustomActivity, List<ActivityTime>>> activityByIdWithTimesData = new MutableLiveData<>();

    private Map<CustomActivity, List<ActivityTime>> activitiesWithTimes;
    private Map<CustomActivity, List<ActivityTime>> activityByIdWithTimes;
    private final LiveData<Map<CustomActivity, List<ActivityTime>>> allActivitiesWithTimes;
    // All activities in list
    private final LiveData<List<CustomActivity>> activities;
    // All times in list
    private final LiveData<List<ActivityTime>> times;

    private final MutableLiveData<List<ActivityWithTimes>> activityWithTimesFilterList = new MutableLiveData<>();
    private List<ActivityWithTimes> activityWithTimesFilter;

    private final MutableLiveData<List<ActivityTime>> allActivitiesTime = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityTime>> oneActivitiesTime = new MutableLiveData<>();
    private List<ActivityTime> allTimes;
    private List<ActivityTime> oneTimes;

    private final MutableLiveData<Boolean> ready = new MutableLiveData<>();
    private MediatorLiveData<List<CustomActivity>> mediatorActivity = new MediatorLiveData<>();

    public Repository(Application application) {
        ActivityRoomDatabase db;
        db = ActivityRoomDatabase.getDatabase(application);
        customActivityDao = db.customActivityDao();
        activityTimeDao = db.activityTimeDao();
        activitiesWithTimesEntities = customActivityDao.getActivitiesWithTimes();
        filterActivities = customActivityDao.getActivityFilter(FirebaseManager.auth.getUid());

        allActivitiesWithTimes = customActivityDao.getAllActivitiesWithTimes();
        activities = customActivityDao.getActivitiesList();
        times = activityTimeDao.getTimes();
    }

    //**********//
    // Handlers //
    //**********//

    /**
     * When we search for an activity with its times, we send an empty message, so the mutable live data
     * will get a new value.
     */
    Handler handlerSingleActivityWithTimesEntity = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            activityWithTimesEntity.setValue(activityWithTimes);
        }
    };

    /**
     * When we search for an activity (without its times), we send an empty message, so the mutable
     * live data will get a new value.
     */
    Handler handlerSingleActivity = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            activitiesData.setValue(activity);
        }
    };

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            activitiesWithTimesData.setValue(activitiesWithTimes);
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

    //************************//
    // Insert, update, delete //
    //************************//

    /**
     * Inserts new activity with the first activity time because it is necessary for showing the deteails.
     *
     * @param customActivity the activity to be inserted
     */
    public void insertActivity(CustomActivity customActivity) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.insertActivity(customActivity);
            LocalDate localDate = LocalDate.now();
            Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            long todayMillis = instant.toEpochMilli();
            activityTimeDao.insertActivityTime(new ActivityTime(customActivity.getId(), todayMillis, 0L));
        });
        executor.shutdown();
    }

    /**
     * Updates the given activity in the local database.
     *
     * @param customActivity the activity to be updated
     */
    public void updateActivity(CustomActivity customActivity) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.updateActivity(customActivity);
        });
        executor.shutdown();
    }

    /**
     * Updates an activity time. It actually inserts if there is no time added for that activity and day,
     * and updates if we have added a time to that day before. If it was an insert it returns true,
     * if it was an update it returns false.
     *
     * @param activityTime the time to be updated (or inserted)
     * @return true if it was an insert, false if it was an update
     * @throws ExecutionException   the database transaction was not successful
     * @throws InterruptedException the database transaction was not successful
     */
    public boolean updateOrInsertTime(ActivityTime activityTime) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> activityTimeDao.insertOrUpdateTime(activityTime));
        while (!future.isDone()) {
            Thread.sleep(300);
        }
        Boolean result = future.get();
        boolean canceled = future.cancel(true);
        executor.shutdown();
        return result;
    }

    /**
     * Updates an activity time which does not belong to a fix activity. It actually inserts if there is no time added for that activity and day,
     * and updates if we have added a time to that day before.
     *
     * @param activityTime the time to be updated (or inserted)
     */
    public void updateOrInsertTimeSingle(ActivityTime activityTime) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> activityTimeDao.insertOrUpdateTime(activityTime));
        executor.shutdown();
    }

    /**
     * Deletes an activity by the given id.
     * CAUTION: it deletes the corresponding activity times as well.
     *
     * @param id the id of the activity we want to delete
     */
    public void deleteActivityById(long id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.deleteActivityById(id);
        });
        executor.shutdown();
    }

    //********//
    // Search //
    //********//

    // timer

    /**
     * Gets an activity based on id. Then it adds to a mutable live data to present in the UI.
     *
     * @param id id of the activity we search for
     */
    public void getActivityById(long id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activity = customActivityDao.getActivityById(id);
            handlerSingleActivity.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    // edit, detail, add time

    /**
     * Searches an activity with its times based on its id. Then it adds to a mutable live data to present in the UI.
     *
     * @param id id of the activity we search for
     */
    public void getSingleActivityByIdWithTimesEntity(long id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityWithTimes = customActivityDao.getActivityWithTimesEntity(id);
            handlerSingleActivityWithTimesEntity.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    //***************//
    // Getting lists //
    //***************//

    //*******************************//
    // Creating and restoring backup //
    //*******************************//

    public static <T> T getValue(LiveData<T> liveData) throws InterruptedException {
        final Object[] objects = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);

        Observer observer = new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                objects[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        latch.await(2, TimeUnit.SECONDS);
        return (T) objects[0];
    }

    public void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


    public void saveData(String userId) {
        List<CustomActivity> activitiesSecond;
        List<ActivityTime> timesSecond;
        try {
            activitiesSecond = getValue(activities);
            timesSecond = getValue(times);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        if (activitiesSecond == null) {
            System.out.println("null in saving data activities");
            return;
        }
        if (timesSecond == null) {
            System.out.println("null in saving data times");
            return;
        }

        List<CustomActivity> customActivities = new ArrayList<>();
        List<ActivityTime> activityTimes = new ArrayList<>();
        for (CustomActivity activity : activitiesSecond) {
            if (activity.getUserId().equals(userId)) {
                customActivities.add(activity);
                List<ActivityTime> tmp = timesSecond.stream().filter(a -> a.getaId() == activity.getId()).collect(Collectors.toList());
                activityTimes.addAll(tmp);
            }
        }
        FirebaseManager.saveToFirebaseActivities(customActivities);
        FirebaseManager.saveToFirebaseTimes(activityTimes);
    }

    public void deleteActivitiesByUserId(String userId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.deleteActivityByUserId(userId);

        });
        //executor.shutdown();
        awaitTerminationAfterShutdown(executor);
    }

    public MutableLiveData<Boolean> getReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready.setValue(ready);
    }

    public void restoreBackup(String userId) {
        restoreReady[0] = 0;
        deleteActivitiesByUserId(userId);
        restoreData(userId);
    }

    public void restoreData(String userId) {
        FirebaseManager.database.getReference().child("backups").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot ds = task.getResult();
                    if (ds != null) {
                        retrieveActivities(ds);
                        retrieveTimes(ds);
                    }
                }
            }
        });
    }

    private void retrieveActivities(DataSnapshot ds) {
        List<CustomActivity> activityList = new ArrayList<>();
        DataSnapshot acts = ds.child("activities");
        for (DataSnapshot actSnapshot : acts.getChildren()) {
            CustomActivity activity = actSnapshot.getValue(CustomActivity.class);
            if (activity != null) {
                activityList.add(activity);
            }
        }
        restoreActivities(activityList);
    }

    private void retrieveTimes(DataSnapshot ds) {
        List<ActivityTime> timeList = new ArrayList<>();
        DataSnapshot times = ds.child("times");
        for (DataSnapshot timeSnapshot : times.getChildren()) {
            ActivityTime time = timeSnapshot.getValue(ActivityTime.class);
            if (time != null) {
                timeList.add(time);
            }
        }
        restoreTimes(timeList);
    }

    private int[] restoreReady = new int[1];

    public void restoreActivities(List<CustomActivity> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.insertAll(list);
        });
        //executor.shutdown();
        awaitTerminationAfterShutdown(executor);
        restoreReady[0]++;
        if (restoreReady[0] == 2) {
            ready.setValue(true);
        }
    }

    public void restoreTimes(List<ActivityTime> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityTimeDao.insertAll(list);
        });
        //executor.shutdown();
        awaitTerminationAfterShutdown(executor);
        restoreReady[0]++;
        if (restoreReady[0] == 2) {
            ready.setValue(true);
        }
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


    public void getFilterSomeAcivity(List<Long> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityWithTimesFilter = customActivityDao.getActivitiesWithTimesFilter(list);
            handlerFilter.sendEmptyMessage(0);
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

    public void insertTime(ActivityTime activityTime) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityTimeDao.insertActivityTime(activityTime);
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

    public void getSomeAllTimes(List<Long> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getSomeAll(list);
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

    //*********//
    // Getters //
    //*********//

    // edit, detail

    /**
     * Returns an activity with its times.
     *
     * @return LiveData ActivityWithTimes, an activity with its times
     */
    public MutableLiveData<ActivityWithTimes> getActivityWithTimesEntity() {
        return activityWithTimesEntity;
    }

    // add time

    /**
     * Returns an activity.
     *
     * @return LiveData CustomActivity, without times
     */
    public MutableLiveData<CustomActivity> getActivitiesData() {
        return activitiesData;
    }

    // home

    /**
     * Returns the list of activities with their times.
     *
     * @return the list of activities with their time in LiveData
     */
    public LiveData<List<ActivityWithTimes>> getActivitiesWithTimesEntities() {
        return activitiesWithTimesEntities;
    }

    /**
     * Returns the list of activities the user has.
     * @return list of activities to filter
     */
    public LiveData<List<ActivityFilter>> getFilterActivities() {
        return filterActivities;
    }


    public MutableLiveData<Map<CustomActivity, List<ActivityTime>>> getActivitiesWithTimesData() {
        return activitiesWithTimesData;
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

}
