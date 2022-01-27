package hu.janny.tomsschedule.model.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public final class FirebaseManager {

    public static FirebaseAuth auth;
    public static FirebaseDatabase database;
    public static FirebaseUser user;

    public static void onStart() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://toms-schedule-2022-default-rtdb.europe-west1.firebasedatabase.app");
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    public static boolean isUserLoggedIn() {
        return user != null;
    }

    public static void setUserLoggedIn(FirebaseUser user) {
        FirebaseManager.user = user;
    }

    public static void logoutUser() {
        FirebaseManager.auth.signOut();
        FirebaseManager.user = null;
    }
}
