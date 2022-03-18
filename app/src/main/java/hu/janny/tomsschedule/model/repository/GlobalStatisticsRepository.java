package hu.janny.tomsschedule.model.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.janny.tomsschedule.model.entities.ActivityTimeFirebase;
import hu.janny.tomsschedule.model.helper.CustomActivityHelper;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

/**
 * Repository of global statistics, it has a connection with Firebase.
 */
public class GlobalStatisticsRepository {

    // List of times from Firebase
    private final MutableLiveData<List<ActivityTimeFirebase>> activityTimesFilterList = new MutableLiveData<>();
    // List of times from Firebase
    private List<ActivityTimeFirebase> activityTimesFilter;
    // It caches the times belongs to the fix activities, so we do not have to ask Firebase necessarily
    private final Map<String, List<ActivityTimeFirebase>> cache = new HashMap<>();
    // The counter we use when we search for data of just one day
    private final int[] counter = new int[1];

    private final FirebaseDatabase db;

    public GlobalStatisticsRepository(Application application) {
        db = FirebaseManager.database;
    }

    /**
     * When we search for an activity with its times, we send an empty message, so the mutable live data
     * will get a new value.
     */
    Handler handlerFilter = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            activityTimesFilterList.setValue(activityTimesFilter);
        }
    };

    /**
     * Gets the data of the given activity from Firebase. It gets the data from the last month.
     * @param name name of the activity
     */
    public void getActivityData(String name) {
        if(cache.containsKey(name)) {
            // If the cache already contains the data of the given activity, then we return that
            activityTimesFilter = cache.get(name);
            handlerFilter.sendEmptyMessage(0);
        } else {
            // If the cache does not contain the data of the given activity, then we ask Firebase
            long prevMonth = CustomActivityHelper.minusMonthMillis(1);
            long today = CustomActivityHelper.todayMillis();
            db.getReference().child("activityTimes").child(name)
                    .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        DataSnapshot ds = task.getResult();
                        List<ActivityTimeFirebase> list = new ArrayList<>();
                        if(ds != null) {
                            for (DataSnapshot genderSnapshot: ds.getChildren()) {
                                for (DataSnapshot ageGroupSnapshot: genderSnapshot.getChildren()) {
                                    for (DataSnapshot activitySnapshot: ageGroupSnapshot.getChildren()) {
                                        ActivityTimeFirebase time = activitySnapshot.getValue(ActivityTimeFirebase.class);
                                        if (time != null && time.getD() >= prevMonth) {
                                            list.add(time);
                                        }
                                    }
                                }
                            }
                        }
                        cache.put(name, list);
                        activityTimesFilter = cache.get(name);
                        handlerFilter.sendEmptyMessage(0);
                    }
                }
            });

        }
    }

    /**
     * Gets the data of the given day from Firebase. It gets data of female and man and age groups from 0 to 5.
     * @param name name of the activity
     * @param dayMillis day in long millis
     */
    public void getExactDayData(String name, long dayMillis) {
        counter[0] = 0;
        System.out.println(name);
        List<ActivityTimeFirebase> list = new ArrayList<>();
        DatabaseReference ref = db.getReference().child("activityTimes").child(name).getRef();
        ref.child("female").child("0").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);
                }
            }
        });
        ref.child("female").child("1").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);
                }
            }
        });
        ref.child("female").child("2").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);
                }
            }
        });
        ref.child("female").child("3").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);
                }
            }
        });
        ref.child("female").child("4").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);
                }
            }
        });
        ref.child("female").child("5").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);
                }
            }
        });
        ref.child("male").child("0").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);
                }
            }
        });
        ref.child("male").child("1").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);}
            }
        });
        ref.child("male").child("2").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);}
            }
        });
        ref.child("male").child("3").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);}
            }
        });
        ref.child("male").child("4").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);
                }
            }
        });
        ref.child("male").child("5").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult() != null) {
                        ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                        if(time != null) {
                            list.add(time);
                        }
                    }
                    counter[0]++;
                    dayReady(list);
                }
            }
        });

    }

    /**
     * If every query is finished in getExactDayData method, it adds the list to a mutable live data
     * with a handler.
     * @param list list of times from Firebase
     */
    private void dayReady(List<ActivityTimeFirebase> list) {
        if(counter[0] == 12) {
            activityTimesFilter = list;
            handlerFilter.sendEmptyMessage(0);
            //System.out.println(list);
        }
    }

    /**
     * Returns the list of times that come after a search from Firebase.
     * @return the list of times we have searched for in mutable live data
     */
    public MutableLiveData<List<ActivityTimeFirebase>> getActivityTimesFilterList() {
        return activityTimesFilterList;
    }
}
