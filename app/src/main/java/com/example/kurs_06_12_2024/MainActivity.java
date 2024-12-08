package com.example.kurs_06_12_2024;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class MainActivity extends AppCompatActivity {

    private ImageButton logoutButton;
    private Button myCarsButton, myCalendarsButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация кнопок
        logoutButton = findViewById(R.id.logoutButton);
        myCarsButton = findViewById(R.id.myCarsButton);
        myCalendarsButton = findViewById(R.id.myCalendarsButton);

        mAuth = FirebaseAuth.getInstance();

        // Проверка, авторизован ли пользователь
        if (mAuth.getCurrentUser() == null) {
            // Если не авторизован, переходим на экран входа
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Настройка Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Убедитесь, что client_id в строках правильно задан
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Обработчик нажатия на кнопку "Выход"
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        // Обработчик нажатия на кнопку "Мои автомобили"
        myCarsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCarsActivity();
            }
        });

        // Обработчик нажатия на кнопку "Мои календари"
        myCalendarsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCalendarsActivity();
            }
        });
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // После выхода возвращаемся на экран входа
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    // Метод для открытия активности "Мои автомобили"
    private void openCarsActivity() {
        Intent intent = new Intent(MainActivity.this, CarsActivity.class);
        startActivity(intent);
    }

    // Метод для открытия активности "Мои календари"
    private void openCalendarsActivity() {
        Intent intent = new Intent(MainActivity.this, CalendarsActivity.class);
        startActivity(intent);
    }
}
