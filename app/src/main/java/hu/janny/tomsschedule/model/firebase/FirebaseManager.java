package hu.janny.tomsschedule.model.firebase;

import android.content.Context;
import android.widget.Toast;

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

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.model.entities.ActivityTime;
import hu.janny.tomsschedule.model.entities.ActivityTimeFirebase;
import hu.janny.tomsschedule.model.entities.CustomActivity;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.model.helper.SuccessCallback;

public final class FirebaseManager {

    public static FirebaseAuth auth;
    public static FirebaseDatabase database;
    public static FirebaseUser user;
    public static FirebaseStorage storage;

    /**
     * Gets the necessary Firebase instances.
     */
    public static void onStart() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://toms-schedule-2022-default-rtdb.europe-west1.firebasedatabase.app");
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Returns whether a user logged in.
     *
     * @return true if the user a logged in
     */
    public static boolean isUserLoggedIn() {
        return user != null;
    }

    /**
     * Sets the Firebase user to logged in in this class.
     *
     * @param user FirebaseUser
     */
    public static void setUserLoggedIn(FirebaseUser user) {
        FirebaseManager.user = user;
    }

    /**
     * Signs out user from Firebase.
     */
    public static void logoutUser() {
        FirebaseManager.user = null;
        FirebaseManager.auth.signOut();
    }

    /**
     * Saves the activity time into Firebase if it is a fix activity.
     * It uses the path "activityTimes/{activityName}/{gender}/{ageGroup}/{dayInMillis}"
     * This method used when the user is adding time to the given day for the first time.
     * In this case we have to increase the user count with 1 and add the time amount.
     *
     * @param activityTime activity time object
     * @param activityName name of activity
     * @param user         user
     */
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
                //System.out.println("Saved insert into firebase");
            }
        });
    }

    /**
     * Saves the activity time into Firebase if it is a fix activity.
     * It uses the path "activityTimes/{activityName}/{gender}/{ageGroup}/{dayInMillis}"
     * This method used when the user is adding time to the given day for not the first time.
     * In this case we have to add the time amount, but not increase user count because
     * we will count the average and that would be a misleading value.
     *
     * @param activityTime activity time object
     * @param activityName name of activity
     * @param user         user
     */
    public static void saveUpdateActivityTimeToFirebase(ActivityTime activityTime, String activityName, User user) {
        DatabaseReference ref = database.getReference("activityTimes").child(activityName).child(user.getGender())
                .child(String.valueOf(user.getAgeGroup())).child(String.valueOf(activityTime.getD()));
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ActivityTimeFirebase p = mutableData.getValue(ActivityTimeFirebase.class);
                if (p == null) {
                    if (activityTime.getT() > 0L) {
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
                //System.out.println("Saved update into firebase");
            }
        });
    }

    /**
     * Saves the activities to Firebase Realtime database during creating backup. Path "backups/{userId}/activities".
     *
     * @param list CustomActivity list to save
     */
    public static void saveToFirebaseActivities(List<CustomActivity> list, SuccessCallback callback) {
        database.getReference().child("backups").child(user.getUid()).child("activities").setValue(list).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callback.onCallback(task.isSuccessful());
            }
        });
    }

    /**
     * Saves the activity times to Firebase Realtime database during creating backup. Path "backups/{userId}/times".
     *
     * @param list ActivityTime list to save
     */
    public static void saveToFirebaseTimes(List<ActivityTime> list, SuccessCallback callback) {
        database.getReference().child("backups").child(user.getUid()).child("times").setValue(list).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callback.onCallback(task.isSuccessful());
            }
        });
    }

    /**
     * Updates the given user in Firebase Realtime database on the path "users/{userId}".
     * It replaces the whole user.
     *
     * @param user    user to be updated
     * @param context context where we want to show success message
     */
    public static void updateUser(User user, Context context) {
        database.getReference().child("users").child(user.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, R.string.edit_was_successful, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.edit_was_fail, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
