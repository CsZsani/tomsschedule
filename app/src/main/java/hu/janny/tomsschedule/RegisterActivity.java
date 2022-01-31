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
import hu.janny.tomsschedule.model.DateConverter;
import hu.janny.tomsschedule.model.User;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityRegisterBinding binding;
    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            User user = new User(FirebaseAuth.getInstance().getCurrentUser().getUid(),email, name,
                                    DateConverter.stringFromSimpleDateDialogToLongMillis(birthDate),
                                    DateConverter.birthDateFromSimpleDateDialogToAgeGroupInt(birthDate), gender);

                            FirebaseManager.database.getReference("users")
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



    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
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


}