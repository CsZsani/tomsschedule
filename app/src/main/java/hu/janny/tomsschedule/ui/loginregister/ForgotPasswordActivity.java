package hu.janny.tomsschedule.ui.loginregister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import hu.janny.tomsschedule.R;
import hu.janny.tomsschedule.databinding.ActivityForgotPasswordBinding;
import hu.janny.tomsschedule.model.firebase.FirebaseManager;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Binds layout
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });
    }

    /**
     * Checks if every necessary data is provided by the user in correct form for password reset.
     */
    private void sendEmail() {
        String email = binding.emailAddress.getText().toString().trim();

        if (email.isEmpty()) {
            binding.emailAddress.setError(getString(R.string.forgot_password_email_required));
            binding.emailAddress.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailAddress.setError(getString(R.string.forgot_password_valid_email));
            binding.emailAddress.requestFocus();
            return;
        }

        sendOutEmail(email);
    }

    /**
     * Sends out an email to the given address for password reset.
     * @param email email address where the user is able to reset they password
     */
    private void sendOutEmail(String email) {
        progressVisible();

        FirebaseManager.auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, R.string.email_sending_successful, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, R.string.email_sending_fail, Toast.LENGTH_LONG).show();
                    progressGone();
                }
            }
        });
    }

    /**
     * Changes layout elements visibility during waiting for email sending.
     */
    private void progressVisible() {
        binding.forgotPasswordView.setVisibility(View.GONE);
        binding.sendingProgressBar.setVisibility(View.VISIBLE);
        binding.sendingProgressText.setVisibility(View.VISIBLE);
    }

    /**
     * Changes layout elements visibility if email sending fails.
     */
    private void progressGone() {
        binding.forgotPasswordView.setVisibility(View.VISIBLE);
        binding.sendingProgressBar.setVisibility(View.GONE);
        binding.sendingProgressText.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}