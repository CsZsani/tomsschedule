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
import hu.janny.tomsschedule.model.helper.SuccessCallback;

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
    // Times are searched in personal statistics
    private final MutableLiveData<List<ActivityTime>> allActivitiesTime = new MutableLiveData<>();
    // Times are searched in personal statistics
    private List<ActivityTime> allTimes;

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

    private final MutableLiveData<List<ActivityTime>> oneActivitiesTime = new MutableLiveData<>();

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

    /**
     * When we search for times of activities in personal statistics, we send an empty message,
     * so the mutable live data will get a new value.
     */
    Handler handlerAllTimes = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            allActivitiesTime.setValue(allTimes);
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

    // personal statistics

    /**
     * Searches the times of the activities in the list on the given day.
     *
     * @param date the day on which we want to find times
     * @param list the list of activities
     */
    public void getSomeExactDates(long date, List<Long> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getSomeExactDate(date, list);
            handlerAllTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    /**
     * Searches the times of the activities in the list from the given day to today.
     *
     * @param from the day from which we want to find times
     * @param list the list of activities
     */
    public void getSomeLaterDates(long from, List<Long> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getSomeLaterDates(from, list);
            handlerAllTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    /**
     * Searches the times of the activities in the list from the given day (from) to an other given day (to).
     *
     * @param from the day from which we want to find times
     * @param to   the day to which we want to find times
     * @param list the list of activities
     */
    public void getSomeBetweenTwoDates(long from, long to, List<Long> list) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            allTimes = activityTimeDao.getSomeBetweenTwoDates(from, to, list);
            handlerAllTimes.sendEmptyMessage(0);
        });
        executor.shutdown();
    }

    //*******************************//
    // Creating and restoring backup //
    //*******************************//

    /**
     * Waits until the executor service finishes its task and become shut down.
     *
     * @param threadPool the executor service
     */
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

    /**
     * Saves the data from local database to Firebase Realtime database. It saves the activities and
     * times that belong to the user with the given id.
     *
     * @param userId   the id of the user who creates backup
     * @param callback called when the Firebase finishes saving
     * @return false if getting data from the local database is failed, true is it was successful
     */
    public boolean saveData(String userId, SuccessCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<ActivityWithTimes>> future = executor.submit(() -> customActivityDao.getActivitiesWithTimesForBackup(userId));
        List<ActivityWithTimes> result;
        try {
            result = future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        future.cancel(true);
        executor.shutdown();
        saveToFirebase(result, callback);
        return true;
    }

    /**
     * Saves the given activities with times to Firebase Realtime database.
     *
     * @param result   activities with their times
     * @param callback called when the Firebase finishes saving
     */
    private void saveToFirebase(List<ActivityWithTimes> result, SuccessCallback callback) {
        List<CustomActivity> activities = new ArrayList<>();
        List<ActivityTime> times = new ArrayList<>();
        for (ActivityWithTimes at : result) {
            activities.add(at.customActivity);
            times.addAll(at.activityTimes);
        }
        FirebaseManager.saveToFirebaseActivities(activities, callback);
        FirebaseManager.saveToFirebaseTimes(times, callback);
    }

    /**
     * Deletes all the activities and times that belong to the user with the given id.
     *
     * @param userId the id of the user whose data we want to delete
     */
    public void deleteActivitiesByUserId(String userId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.deleteActivityByUserId(userId);

        });
        //executor.shutdown();
        awaitTerminationAfterShutdown(executor);
    }

    /**
     * Restores the data of the user with the given id from Firebase to local database.
     *
     * @param userId   id of the user who restores backup
     * @param callback called when the data is saved into the local database
     */
    public void restoreBackup(String userId, SuccessCallback callback) {
        restoreReady[0] = 0;
        deleteActivitiesByUserId(userId);
        restoreData(userId, callback);
    }

    /**
     * Gets data from Firebase to restore them into local database.
     *
     * @param userId   id of the user who restores backup
     * @param callback called when there is no data to restore or when it is done successfully
     */
    public void restoreData(String userId, SuccessCallback callback) {
        FirebaseManager.database.getReference().child("backups").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot ds = task.getResult();
                    if (ds != null) {
                        retrieveActivities(ds, callback);
                        retrieveTimes(ds, callback);
                    } else {
                        callback.onCallback(false);
                    }
                } else {
                    callback.onCallback(false);
                }
            }
        });
    }

    private void retrieveActivities(DataSnapshot ds, SuccessCallback callback) {
        List<CustomActivity> activityList = new ArrayList<>();
        DataSnapshot acts = ds.child("activities");
        for (DataSnapshot actSnapshot : acts.getChildren()) {
            CustomActivity activity = actSnapshot.getValue(CustomActivity.class);
            if (activity != null) {
                activityList.add(activity);
            }
        }
        restoreActivities(activityList, callback);
    }

    private void retrieveTimes(DataSnapshot ds, SuccessCallback callback) {
        List<ActivityTime> timeList = new ArrayList<>();
        DataSnapshot times = ds.child("times");
        for (DataSnapshot timeSnapshot : times.getChildren()) {
            ActivityTime time = timeSnapshot.getValue(ActivityTime.class);
            if (time != null) {
                timeList.add(time);
            }
        }
        restoreTimes(timeList, callback);
    }

    private final int[] restoreReady = new int[1];

    public void restoreActivities(List<CustomActivity> list, SuccessCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            customActivityDao.insertAll(list);
        });
        executor.shutdown();
        //awaitTerminationAfterShutdown(executor);
        restoreReady[0]++;
        if (restoreReady[0] == 2) {
            callback.onCallback(true);
        }
    }

    public void restoreTimes(List<ActivityTime> list, SuccessCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            activityTimeDao.insertAll(list);
        });
        executor.shutdown();
        //awaitTerminationAfterShutdown(executor);
        restoreReady[0]++;
        if (restoreReady[0] == 2) {
            callback.onCallback(true);
        }
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
     *
     * @return list of activities to filter
     */
    public LiveData<List<ActivityFilter>> getFilterActivities() {
        return filterActivities;
    }

    /**
     * Returns the list of times of activities that are searched in personal statistics.
     *
     * @return list of times of the activities we searched
     */
    public MutableLiveData<List<ActivityTime>> getAllActivitiesTime() {
        return allActivitiesTime;
    }

    public MutableLiveData<Map<CustomActivity, List<ActivityTime>>> getActivitiesWithTimesData() {
        return activitiesWithTimesData;
    }

    public LiveData<Map<CustomActivity, List<ActivityTime>>> getAllActivitiesWithTimes() {
        return allActivitiesWithTimes;
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

    //*******//
    // Shelf //
    //*******//

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

}
