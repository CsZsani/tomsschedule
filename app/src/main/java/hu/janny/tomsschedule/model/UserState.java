package hu.janny.tomsschedule.model;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public final class UserState {

    private static User user;

    public static void setUser() {
        if(FirebaseManager.isUserLoggedIn()) {
            FirebaseManager.database.getReference("users").child(FirebaseManager.user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            user = snapshot.getValue(User.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    public static User getUser() {return user;}

    public static String ageGroup() {
        switch (user.ageGroup) {
            case 0: return "<20";
            case 1: return "20-30";
            case 2: return "30-40";
            case 3: return "40-50";
            case 4: return "50-60";
            case 5: return ">60";
            default: return "?";
        }
    }
}
