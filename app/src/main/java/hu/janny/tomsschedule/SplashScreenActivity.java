package hu.janny.tomsschedule;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseManager.onStart();
        if(FirebaseManager.isUserLoggedIn()){
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        }
        finish();
        //setContentView(R.layout.activity_splash_screen);
    }
}