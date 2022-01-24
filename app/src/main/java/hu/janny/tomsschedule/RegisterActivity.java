package hu.janny.tomsschedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import hu.janny.tomsschedule.databinding.ActivityRegisterBinding;
import hu.janny.tomsschedule.model.User;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        binding.registerGender.setOnItemSelectedListener(this);
        initDatePicker();
        binding.registerBirthDate.setHint(R.string.birth_date);
    }

    public void registerUser(View view) {
        String email = binding.registerEmail.getText().toString().trim();
        String password = binding.registerPassword.getText().toString().trim();
        String name = binding.registerName.getText().toString().trim();
        String birthDate = binding.registerBirthDate.getText().toString().trim();
        String gender = binding.registerGender.getSelectedItem().toString().trim();

        if(email.isEmpty()) {
            binding.registerEmail.setError("Email is required!");
            binding.registerEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.registerEmail.setError("Please provide valid email!");
            binding.registerEmail.requestFocus();
            return;
        }

        if(password.isEmpty()) {
            binding.registerPassword.setError("Password is requird!");
            binding.registerPassword.requestFocus();
            return;
        }

        if(password.length() < 6) {
            binding.registerPassword.setError("Min 6 characters!");
            binding.registerPassword.requestFocus();
            return;
        }

        if(name.isEmpty()) {
            binding.registerName.setError("Name is required!");
            binding.registerName.requestFocus();
            return;
        }

        if(birthDate.isEmpty()) {
            binding.registerBirthDate.setError("Birth date is required!");
            binding.registerBirthDate.requestFocus();
            return;
        }

        binding.registerProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            User user = new User(email, name, birthDate, gender);

                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, R.string.user_registration_successful, Toast.LENGTH_LONG).show();
                                        binding.registerProgressBar.setVisibility(View.GONE);

                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    } else {
                                        Toast.makeText(RegisterActivity.this, R.string.user_registration_failure, Toast.LENGTH_LONG).show();
                                        binding.registerProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, R.string.user_registration_failure, Toast.LENGTH_LONG).show();
                            binding.registerProgressBar.setVisibility(View.GONE);
                        }
                    }
                });

    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void openDatePicker(View view) {
        datePickerDialog.show();
    }

    private String getTodayDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return makeDateString(day, month, year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(day, month, year);
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

    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month) {
        if(month == 1) {
            return "JAN";
        }
        if(month == 2) {
            return "FEB";
        }
        if(month == 3) {
            return "MAR";
        }
        if(month == 4) {
            return "APR";
        }
        if(month == 5) {
            return "MAY";
        }
        if(month == 6) {
            return "JUN";
        }
        if(month == 7) {
            return "JUL";
        }
        if(month == 8) {
            return "AUG";
        }
        if(month == 9) {
            return "SEP";
        }
        if(month == 10) {
            return "OKT";
        }
        if(month == 11) {
            return "NOV";
        }
        if(month == 12) {
            return "DEC";
        }

        return "JAN";
    }
}