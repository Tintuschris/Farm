package com.example.farm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends AppCompatActivity {

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String LOGGED_IN = "loggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        mUsernameEditText = findViewById(R.id.username);
        mPasswordEditText = findViewById(R.id.password);
        mLoginButton = findViewById(R.id.login_button);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mUsernameEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();

                if (!validateEmail(email) || !validatePassword(password)) {
                    return;
                }

                // Move Firebase login to a background thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(getApplicationContext(), R.string.login_successful, Toast.LENGTH_SHORT).show();
                                                setLoggedInStatus(true);
                                                navigateToHomePage();
                                            });
                                        } else {
                                            String errorMessage = task.getException() != null ?
                                                    task.getException().getMessage() : getString(R.string.login_failed);
                                            runOnUiThread(() -> Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show());
                                        }
                                    }
                                });
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        boolean loggedIn = sharedPreferences.getBoolean(LOGGED_IN, false);
        if (loggedIn && FirebaseAuth.getInstance().getCurrentUser() != null) {
            navigateToHomePage();
        }
    }
    // Method to validate email format
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

    // Method to validate password
    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            showToast(R.string.enter_password);
            return false;
        }
        return true;
    }

    // Navigate to HomePageActivity
    private void navigateToHomePage() {
        Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish();
    }

    // Set the login status in SharedPreferences
    private void setLoggedInStatus(boolean status) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOGGED_IN, status);
        editor.apply();
    }

    // Helper method to show toast messages
    private void showToast(int messageId) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), messageId, Toast.LENGTH_SHORT).show());
    }

    // Logout method for SharedPreferences
    public static void logout(SharedPreferences sharedPreferences, Activity activity) {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear SharedPreferences
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(LOGGED_IN, false);
            editor.apply();
        }

        // Navigate back to MainActivity
        if (activity != null) {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        }
    }
}
