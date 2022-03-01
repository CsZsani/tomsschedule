package hu.janny.tomsschedule.model.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import hu.janny.tomsschedule.model.ActivityTime;
import hu.janny.tomsschedule.model.ActivityTimeFirebase;
import hu.janny.tomsschedule.model.CustomActivity;
import hu.janny.tomsschedule.model.User;

public final class FirebaseManager {

    public static FirebaseAuth auth;
    public static FirebaseDatabase database;
    public static FirebaseUser user;
    public static FirebaseStorage storage;

    public static void onStart() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://toms-schedule-2022-default-rtdb.europe-west1.firebasedatabase.app");
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
    }

    public static boolean isUserLoggedIn() {
        return user != null;
    }

    public static void setUserLoggedIn(FirebaseUser user) {
        FirebaseManager.user = user;
    }

    public static void logoutUser() {
        FirebaseManager.user = null;
        FirebaseManager.auth.signOut();
    }

    private static String userToType(String name, int ageGroup) {
        return name + "-" + String.valueOf(ageGroup);
    }

    public static void saveInsertedActivityTimeToFirebase(ActivityTime activityTime, String activityName, User user) {
        DatabaseReference ref = database.getReference("activityTimes").child(activityName).child(user.getGender())
                .child(String.valueOf(user.getAgeGroup())).child(String.valueOf(activityTime.getD()));
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ActivityTimeFirebase p = mutableData.getValue(ActivityTimeFirebase.class);
                if (p == null) {
                    mutableData.setValue(new ActivityTimeFirebase(activityTime.getD(), activityTime.getT(), 1, user.getGenderInt(), user.getAgeGroup()));
                    return Transaction.success(mutableData);
                }

                p.t = p.t + activityTime.getT();
                p.c = p.c + 1;

                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed,
                                   DataSnapshot currentData) {
                System.out.println("Saved insert into firebase");
            }
        });
    }

    public static void saveUpdateActivityTimeToFirebase(ActivityTime activityTime, String activityName, User user) {
        DatabaseReference ref = database.getReference("activityTimes").child(activityName).child(user.getGender())
                .child(String.valueOf(user.getAgeGroup())).child(String.valueOf(activityTime.getD()));
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ActivityTimeFirebase p = mutableData.getValue(ActivityTimeFirebase.class);
                if (p == null) {
                    if(activityTime.getT() > 0L){
                        mutableData.setValue(new ActivityTimeFirebase(activityTime.getD(), activityTime.getT(), 1, user.getGenderInt(), user.getAgeGroup()));
                    }
                    return Transaction.success(mutableData);
                }

                p.t = p.t + activityTime.getT();

                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed,
                                   DataSnapshot currentData) {
                System.out.println("Saved update into firebase");
            }
        });
    }

    public static void saveToFirebaseActivities(List<CustomActivity> list) {
        database.getReference().child("backups").child(user.getUid()).child("activities").setValue(list).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()) {
                    System.out.println("Saved activities into firebase");
                }
            }
        });
    }

    public static void saveToFirebaseTimes(List<ActivityTime> list) {
        database.getReference().child("backups").child(user.getUid()).child("times").setValue(list).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()) {
                    System.out.println("Saved times into firebase");
                }
            }
        });
    }
}
