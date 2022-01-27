package hu.janny.tomsschedule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hu.janny.tomsschedule.model.UserState;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseManager.onStart();
        if(FirebaseManager.isUserLoggedIn()){
            UserState.setUser();
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        }
        finish();
        //setContentView(R.layout.activity_splash_screen);
    }
}