package hu.janny.tomsschedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

import hu.janny.tomsschedule.databinding.ActivityLoginBinding;
import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginRegisterViewModel viewModel;
    private List<User> us;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);

        viewModel.getUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                us = users;
            }
        });

        binding.registerButtonLogin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                    }
                }
        );
    }

    public void loginUser(View view) {
        String email = binding.loginEmail.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();

        if(email.isEmpty()) {
            binding.loginEmail.setError("Email is required!");
            binding.loginEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.loginEmail.setError("Please provide valid email!");
            binding.loginEmail.requestFocus();
            return;
        }

        if(password.isEmpty()) {
            binding.loginPassword.setError("Password is requird!");
            binding.loginPassword.requestFocus();
            return;
        }

        if(password.length() < 6) {
            binding.loginPassword.setError("Min 6 characters!");
            binding.loginPassword.requestFocus();
            return;
        }

        binding.loginProgressBar.setVisibility(View.VISIBLE);

        FirebaseManager.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    binding.loginProgressBar.setVisibility(View.GONE);
                    FirebaseUser user = FirebaseManager.auth.getCurrentUser();

                    // Check email verification for user when login
                    /*if(user.isEmailVerified()) {
                        FirebaseManager.setUserLoggedIn(user);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        user.sendEmailVerification();
                        Toast.makeText(LoginActivity.this, R.string.email_verification, Toast.LENGTH_LONG).show();
                    }*/

                    // Temporary login - for test phase - you don't need to verify your account

                    manageLogin(user);

                } else {
                    Toast.makeText(LoginActivity.this, R.string.user_login_failure, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void manageLogin(FirebaseUser user) {
        if(user != null) {
            User u = us.stream().filter(e -> e.getUid().equals(user.getUid())).findAny().orElse(null);
            if(u == null){
                FirebaseManager.database.getReference("users").child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Can't login! Check your internet connection!", Toast.LENGTH_LONG).show();
                        } else {
                            User newUser = task.getResult().getValue(User.class);
                            System.out.println(newUser.toString() + " new user to db");
                            newUser.setLoggedIn(true);
                            viewModel.insertUser(newUser);
                            FirebaseManager.setUserLoggedIn(user);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }
                    }
                });
            } else {
                viewModel.loginUser(user.getUid());
                FirebaseManager.setUserLoggedIn(user);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}