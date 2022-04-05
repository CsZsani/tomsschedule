package hu.janny.tomsschedule.ui.loginregister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

import hu.janny.tomsschedule.model.helper.InternetConnectionHelper;
import hu.janny.tomsschedule.viewmodel.LoginRegisterViewModel;
import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.ActivityRegisterBinding;
import hu.janny.tomsschedule.model.helper.DateConverter;
import hu.janny.tomsschedule.model.entities.User;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

/**
 * Register activity where users are able to create a new account.
 * It implements AdapterView.OnItemSelectedListener for gender picker spinner.
 */
public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityRegisterBinding binding;
    private DatePickerDialog datePickerDialog;

    private LoginRegisterViewModel viewModel;
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Binds layout
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Gets a LoginRegisterViewModel instance
        viewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);

        //Sets item selected listener on gender spinner
        binding.registerGender.setOnItemSelectedListener(this);

        initDatePicker();
        binding.registerBirthDate.setHint(R.string.birth_date);
    }

    /**
     * Checks if every necessary data is provided by the user in correct form for registration.
     *
     * @param view
     */
    public void registerUser(View view) {
        if (!InternetConnectionHelper.hasInternetConnection()) {
            Toast.makeText(this, R.string.connect_to_stable_internet, Toast.LENGTH_SHORT).show();
            return;
        }

        String email = binding.registerEmail.getText().toString().trim();
        String password = binding.registerPassword.getText().toString().trim();
        String name = binding.registerName.getText().toString().trim();
        String birthDate = binding.registerBirthDate.getText().toString().trim();
        String gender;
        if (binding.registerGender.getSelectedItem().toString().equals(getString(R.string.female))) {
            gender = "female";
        } else {
            gender = "male";
        }

        if (email.isEmpty()) {
            binding.registerEmail.setError(getString(R.string.registration_email_required));
            binding.registerEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.registerEmail.setError(getString(R.string.registration_valid_email));
            binding.registerEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.registerPassword.setError(getString(R.string.registration_password_required));
            binding.registerPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.registerPassword.setError(getString(R.string.registration_password_length));
            binding.registerPassword.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            binding.registerName.setError(getString(R.string.registration_name_required));
            binding.registerName.requestFocus();
            return;
        }

        if (birthDate.isEmpty()) {
            Toast.makeText(RegisterActivity.this, getString(R.string.registration_birth_date_required), Toast.LENGTH_LONG).show();
            return;
        }

        createNewUser(name, password, email, gender, birthDate);
    }

    /**
     * Registers the user in Firebase.
     *
     * @param name      Username of the user
     * @param password  Password of the user - required for login
     * @param email     Email address of the user - required for login
     * @param gender    Gender of the user
     * @param birthDate Birth date of the user
     */
    private void createNewUser(String name, String password, String email, String gender, String birthDate) {
        progressVisible();
        FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Creates a new user
                            User user = new User(FirebaseAuth.getInstance().getCurrentUser().getUid(), email, name,
                                    birthDate, DateConverter.birthDateFromSimpleDateDialogToAgeGroupInt(birthDate), gender);

                            // Saves the new user into Firebase Realtime Database under path "users/{userId}"
                            FirebaseManager.database.getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, R.string.user_registration_successful, Toast.LENGTH_LONG).show();

                                        // Saves new user into local database
                                        saveUserIntoLocalDb(user);
                                    } else {
                                        Toast.makeText(RegisterActivity.this, R.string.user_registration_failure, Toast.LENGTH_LONG).show();
                                        progressGone();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, R.string.user_registration_failure, Toast.LENGTH_LONG).show();
                            progressGone();
                        }
                    }
                });
    }

    /**
     * Saves new user into local database.
     *
     * @param user
     */
    private void saveUserIntoLocalDb(User user) {
        viewModel.insertUser(user);
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }

    /**
     * They are necessary for AdapterView.OnItemSelectedListener for gender spinner.
     */
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * Shows the date picker dialog.
     *
     * @param view
     */
    public void openDatePicker(View view) {
        datePickerDialog.show();
    }

    /**
     * Initializes calendar for birth day pick.
     */
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.clear();
                calendar.set(year, month, day);
                // Months counting begins with 0
                month = month + 1;
                String date = DateConverter.makeDateStringForSimpleDateDialog(day, month, year);
                binding.registerBirthDate.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    /**
     * Changes layout elements visibility during waiting for registration.
     */
    private void progressVisible() {
        binding.registrationView.setVisibility(View.GONE);
        binding.registerProgressBar.setVisibility(View.VISIBLE);
        binding.registrationProgressText.setVisibility(View.VISIBLE);
    }

    /**
     * Changes layout elements visibility if registration fails.
     */
    private void progressGone() {
        binding.registerProgressBar.setVisibility(View.GONE);
        binding.registrationProgressText.setVisibility(View.GONE);
        binding.registrationView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}