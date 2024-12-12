package com.example.kurs_06_12_2024;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isPasswordVisible = false; // Флаг для видимости пароля

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Обработчик для регистрации пользователя
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Обработчик для кнопки переключения видимости пароля
        ImageView passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle);
        passwordVisibilityToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });
    }

    // Метод для регистрации пользователя
    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Проверка, что email не пустой
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        // Проверка на валидность email
        if (!isValidEmail(email)) {
            emailEditText.setError("Please enter a valid email address");
            return;
        }

        // Проверка, что пароль не пустой
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        // Проверка на валидность пароля
        if (!isValidPassword(password)) {
            passwordEditText.setError("Password must be at least 6 characters long and contain at least one digit and one uppercase letter");
            return;
        }

        // Регистрация пользователя в Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Регистрация успешна, сохраняем данные в Firestore
                            String userId = mAuth.getCurrentUser().getUid();
                            saveUserData(userId, email);

                            // Переход к следующей активности
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // Ошибка регистрации
                            Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Метод для сохранения данных пользователя в Firestore
    private void saveUserData(String userId, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);

        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "User data saved.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to save user data.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Метод для проверки формата email
    public boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
        return email.matches(emailPattern);
    }

    // Метод для проверки пароля
    public boolean isValidPassword(String password) {
        // Проверка на длину пароля
        if (password.length() < 8) {
            return false;
        }
        // Проверка на наличие хотя бы одной цифры и одной заглавной буквы
        boolean hasDigit = false;
        boolean hasUpperCase = false;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            }
        }
        return hasDigit && hasUpperCase;
    }

    // Метод для переключения видимости пароля
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
        } else {
            passwordEditText.setTransformationMethod(android.text.method.HideReturnsTransformationMethod.getInstance());
        }
        isPasswordVisible = !isPasswordVisible; // Меняем состояние
    }

    // Метод для перехода в экран входа
    public void goToLogin(View view) {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

}
