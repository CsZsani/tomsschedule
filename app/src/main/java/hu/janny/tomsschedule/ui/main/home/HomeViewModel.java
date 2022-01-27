package hu.janny.tomsschedule.ui.main.home;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.List;

import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;
import hu.janny.tomsschedule.model.firebase.FirebaseQueryLiveData;

public class HomeViewModel extends ViewModel {

    private static final DatabaseReference USERS_ACTIVITIES =
            FirebaseManager.database.getReference("/customactivities/" + FirebaseManager.user.getUid());

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(USERS_ACTIVITIES);

    private final LiveData<List<CustomActivity>> customActivityLiveData =
            Transformations.map(liveData, new Deserializer());

    private GenericTypeIndicator<List<CustomActivity>> typeIndicator = new GenericTypeIndicator<List<CustomActivity>>() {};

    private class Deserializer implements Function<DataSnapshot, List<CustomActivity>> {
        @Override
        public List<CustomActivity> apply(DataSnapshot dataSnapshot) {
            List<CustomActivity> list = new ArrayList<>();
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                CustomActivity animal = ds.getValue(CustomActivity.class);
                list.add(animal);
            }
            return list;
        }
    }

    @NonNull
    public LiveData<List<CustomActivity>> getCustomActivitiesLiveData() {
        return customActivityLiveData;
    }

    /*@NonNull
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }*/

    /*private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }*/
}