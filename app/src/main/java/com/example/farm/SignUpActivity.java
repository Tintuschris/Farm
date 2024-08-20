package com.example.farm;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signUpButton;

    private FirebaseAuth mAuth;

    // Password policy: At least 1 digit, 1 lowercase, 1 uppercase, 1 special character, and 6+ characters
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +        // at least 1 digit
                    "(?=.*[a-z])" +        // at least 1 lowercase letter
                    "(?=.*[A-Z])" +        // at least 1 uppercase letter
                    "(?=.*[@#$%^&+=!])" +  // at least 1 special character
                    "(?=\\S+$)" +          // no whitespace allowed
                    ".{6,}" +              // at least 6 characters
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (!validateEmail(email) || !validatePassword(password)) {
                    return;
                }

                // Firebase registration moved to a background thread for better performance
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                                    R.string.registration_successful, Toast.LENGTH_SHORT).show());
                                            navigateToLogin();
                                        } else {
                                            String errorMessage = task.getException() != null ?
                                                    task.getException().getMessage() : getString(R.string.registration_failed);
                                            runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                                    errorMessage, Toast.LENGTH_LONG).show());
                                        }
                                    }
                                });
                    }
                }).start();
            }
        });
    }

    // Email validation with Patterns
    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            showToast(R.string.enter_email);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast(R.string.invalid_email);
            return false;
        }
        return true;
    }

    // Password validation based on the regex pattern
    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            showToast(R.string.enter_password);
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            showToast(R.string.invalid_password);
            return false;
        }
        return true;
    }

    // Navigate to LoginActivity
    private void navigateToLogin() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Helper method to show toast messages
    private void showToast(int messageId) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), messageId, Toast.LENGTH_SHORT).show());
    }
}
