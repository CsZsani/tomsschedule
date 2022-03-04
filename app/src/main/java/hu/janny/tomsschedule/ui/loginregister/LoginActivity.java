package hu.janny.tomsschedule.ui.loginregister;

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

import hu.janny.tomsschedule.model.helper.InternetConnectionHelper;
import hu.janny.tomsschedule.viewmodel.LoginRegisterViewModel;
import hu.janny.tomsschedule.MainActivity;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.ActivityLoginBinding;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginRegisterViewModel viewModel;

    // Users in database
    private List<User> us;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Binds layout
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Gets a LoginRegisterViewModel instance
        viewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);

        // Gets the available users int database
        viewModel.getUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                us = users;
            }
        });

        // Sets on click listener for register button
        binding.registerButtonLogin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                    }
                }
        );

        // Sets on click listener for forgot password button
        binding.forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    /**
     * Checks if every necessary data is provided by the user in correct form for login.
     *
     * @param view
     */
    public void loginUser(View view) {
        if (!InternetConnectionHelper.hasInternetConnection()) {
            Toast.makeText(this, R.string.connect_to_stable_internet, Toast.LENGTH_SHORT).show();
            return;
        }

        String email = binding.loginEmail.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            binding.loginEmail.setError(getString(R.string.login_email_required));
            binding.loginEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.loginEmail.setError(getString(R.string.login_valid_email));
            binding.loginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.loginPassword.setError(getString(R.string.login_password_required));
            binding.loginPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.loginPassword.setError(getString(R.string.login_password_length));
            binding.loginPassword.requestFocus();
            return;
        }

        login(email, password);
    }

    /**
     * Sets the state of user to logged in via Firebase.
     *
     * @param email
     * @param password
     */
    private void login(String email, String password) {
        progressVisible();

        // Signs in user
        FirebaseManager.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Asks Firebase for user's data
                    FirebaseUser user = FirebaseManager.auth.getCurrentUser();

                    // Checks email verification for user when login
                    /*if(user.isEmailVerified()) {
                        manageLogin(user);
                    } else {
                        user.sendEmailVerification();
                        Toast.makeText(LoginActivity.this, R.string.email_verification, Toast.LENGTH_LONG).show();
                    }*/

                    // Temporary login - for test phase - you don't need to verify your account

                    manageLogin(user);

                } else {
                    Toast.makeText(LoginActivity.this, R.string.user_login_failure, Toast.LENGTH_LONG).show();
                    progressGone();
                }
            }
        });
    }

    /**
     * Adds user to local database if it is they first login in this device.
     * First, it checks if the local includes the current user. Then,
     *
     * @param user Firebase user
     */
    private void manageLogin(FirebaseUser user) {
        if (user != null) {
            User u = us.stream().filter(e -> e.getUid().equals(user.getUid())).findAny().orElse(null);
            if (u == null) {
                FirebaseManager.database.getReference("users").child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.user_login_failure, Toast.LENGTH_LONG).show();
                            progressGone();
                        } else {
                            // Gets user and insert into database
                            if (task.getResult() != null) {
                                User newUser = task.getResult().getValue(User.class);
                                if (newUser != null) {
                                    newUser.setLoggedIn(true);
                                    viewModel.insertUser(newUser);
                                    FirebaseManager.setUserLoggedIn(user);
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                } else {
                                    Toast.makeText(LoginActivity.this, R.string.login_went_wrong, Toast.LENGTH_LONG).show();
                                    progressGone();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.login_went_wrong, Toast.LENGTH_LONG).show();
                                progressGone();
                            }
                        }
                    }
                });
            } else {
                // We already have user in database, just need to set logged in
                viewModel.loginUser(user.getUid());
                FirebaseManager.setUserLoggedIn(user);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        }
    }

    /**
     * Changes layout elements visibility during waiting for login.
     */
    private void progressVisible() {
        binding.loginProgressBar.setVisibility(View.VISIBLE);
        binding.loginProgressText.setVisibility(View.VISIBLE);
        binding.loginView.setVisibility(View.GONE);
    }

    /**
     * Changes layout elements visibility if login fails.
     */
    private void progressGone() {
        binding.loginProgressBar.setVisibility(View.GONE);
        binding.loginProgressText.setVisibility(View.GONE);
        binding.loginView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}