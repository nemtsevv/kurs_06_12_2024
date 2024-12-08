package com.example.kurs_06_12_2024;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CalendarsActivity extends AppCompatActivity {

    private static final String TAG = "CalendarsActivity";

    private ImageButton addButton;
    private LinearLayout addCalendarLayout;
    private EditText calendarNameEditText;
    private Spinner carSpinner;
    private Button saveButton;
    private RecyclerView calendarRecyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private CalendarsAdapter calendarAdapter;
    private List<MyCalendar> calendarList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendars);

        addButton = findViewById(R.id.addButton);
        addCalendarLayout = findViewById(R.id.addCalendarLayout);
        calendarNameEditText = findViewById(R.id.calendarNameEditText);
        carSpinner = findViewById(R.id.carSpinner);
        saveButton = findViewById(R.id.saveButton);
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        calendarList = new ArrayList<>();
        // Передаем контекст в адаптер
        calendarAdapter = new CalendarsAdapter(this, calendarList);
        calendarRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        calendarRecyclerView.setAdapter(calendarAdapter);

        loadCars();
        loadCalendars();

        // Обработчик для кнопки добавления календаря
        addButton.setOnClickListener(v -> {
            Log.d(TAG, "Add button clicked");
            if (addCalendarLayout.getVisibility() == View.GONE) {
                addCalendarLayout.setVisibility(View.VISIBLE);
                Log.d(TAG, "addCalendarLayout set to VISIBLE");
            } else {
                addCalendarLayout.setVisibility(View.GONE);
                Log.d(TAG, "addCalendarLayout set to GONE");
            }
        });

        saveButton.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");
            saveCalendar();
        });
    }

    private void loadCars() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("cars")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> carList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String carBrand = document.getString("brand");
                            String carModel = document.getString("model");
                            if (carBrand != null && !carBrand.isEmpty() && carModel != null && !carModel.isEmpty()) {
                                String carDisplay = carBrand + " " + carModel;
                                carList.add(carDisplay);
                            }
                        }

                        if (carList.isEmpty()) {
                            Toast.makeText(CalendarsActivity.this, "No cars found!", Toast.LENGTH_SHORT).show();
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        carSpinner.setAdapter(adapter);

                    } else {
                        Toast.makeText(CalendarsActivity.this, "Failed to load cars: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCalendars() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("calendars")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        calendarList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String calendarName = document.getString("calendarName");
                            String carBrand = document.getString("carBrand");
                            String carModel = document.getString("carModel");
                            calendarList.add(new MyCalendar(calendarName, carBrand, carModel));
                        }
                        calendarAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(CalendarsActivity.this, "Failed to load calendars: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveCalendar() {
        String calendarName = calendarNameEditText.getText().toString();
        String selectedCar = (String) carSpinner.getSelectedItem();

        if (calendarName.isEmpty()) {
            Toast.makeText(CalendarsActivity.this, "Please enter a calendar name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCar == null || selectedCar.isEmpty()) {
            Toast.makeText(CalendarsActivity.this, "Please select a car", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        String[] carDetails = selectedCar.split(" ");
        String carBrand = carDetails[0];
        String carModel = carDetails[1];

        MyCalendar calendar = new MyCalendar(calendarName, carBrand, carModel);

        db.collection("users").document(userId).collection("calendars")
                .add(calendar)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CalendarsActivity.this, "Calendar added successfully", Toast.LENGTH_SHORT).show();
                    addCalendarLayout.setVisibility(View.GONE);
                    calendarNameEditText.setText("");
                    loadCalendars();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CalendarsActivity.this, "Error adding calendar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
