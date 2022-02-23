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

import hu.janny.tomsschedule.model.ActivityTimeFirebase;
import hu.janny.tomsschedule.model.ActivityWithTimes;
import hu.janny.tomsschedule.model.CustomActivityHelper;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class GlobalStatisticsRepository {

    private final MutableLiveData<List<ActivityTimeFirebase>> activityTimesFilterList = new MutableLiveData<>();
    private List<ActivityTimeFirebase> activityTimesFilter;
    private Map<String, List<ActivityTimeFirebase>> cache = new HashMap<>();
    private int[] counter = new int[1];

    private final FirebaseDatabase db;

    public GlobalStatisticsRepository(Application application) {
        db = FirebaseManager.database;
    }

    Handler handlerFilter = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            activityTimesFilterList.setValue(activityTimesFilter);
        }
    };

    public void getActivityData(String name) {
        if(cache.containsKey(name)) {
            activityTimesFilter = cache.get(name);
            handlerFilter.sendEmptyMessage(0);
        } else {
            long prevMonth = CustomActivityHelper.minusMonthMillis(1);
            db.getReference().child("activityTimes").child(name).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        DataSnapshot ds = task.getResult();
                        List<ActivityTimeFirebase> list = new ArrayList<>();
                        for (DataSnapshot genderSnapshot: ds.getChildren()) {
                            for (DataSnapshot ageGroupSnapshot: genderSnapshot.getChildren()) {
                                ActivityTimeFirebase time = ageGroupSnapshot.getValue(ActivityTimeFirebase.class);
                                if(time != null && time.getD() >= prevMonth) {
                                    list.add(time);
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

    public void getExactDayData(String name, long dayMillis) {
        counter[0] = 0;
        List<ActivityTimeFirebase> list = new ArrayList<>();
        DatabaseReference ref = db.getReference().child("activityTimes").child(name).getRef();
        ref.child("female").child("0").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("female").child("1").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("female").child("2").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("female").child("3").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("female").child("4").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("female").child("5").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("male").child("0").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("male").child("1").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("male").child("2").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("male").child("3").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("male").child("4").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });
        ref.child("male").child("5").child(String.valueOf(dayMillis)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()) {
                    ActivityTimeFirebase time = task.getResult().getValue(ActivityTimeFirebase.class);
                    if(time != null) {
                        list.add(time);
                        counter[0]++;
                        dayReady(list);
                    }
                }
            }
        });

    }

    private void dayReady(List<ActivityTimeFirebase> list) {
        if(counter[0] == 12) {
            activityTimesFilter = list;
            handlerFilter.sendEmptyMessage(0);
        }
    }

    public MutableLiveData<List<ActivityTimeFirebase>> getActivityTimesFilterList() {
        return activityTimesFilterList;
    }
}
