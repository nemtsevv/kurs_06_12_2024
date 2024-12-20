package com.example.kurs_06_12_2024;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.LocalDate;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OtherCalendarsActivity extends AppCompatActivity {

    private RecyclerView otherCalendarsRecyclerView;
    private Button shareCalendarButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private OtherCalendarsAdapter otherCalendarsAdapter;
    private List<MyCalendar> otherCalendarList;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_calendars);

        otherCalendarsRecyclerView = findViewById(R.id.otherCalendarsRecyclerView);
        shareCalendarButton = findViewById(R.id.shareCalendarButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        otherCalendarList = new ArrayList<>();
        otherCalendarsAdapter = new OtherCalendarsAdapter(this, otherCalendarList);
        otherCalendarsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        otherCalendarsRecyclerView.setAdapter(otherCalendarsAdapter);

        loadSharedCalendars();

        shareCalendarButton.setOnClickListener(v -> showUserListDialog());
    }

    private void loadSharedCalendars() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("shared_calendars").document(userId).collection("calendars")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        otherCalendarList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String calendarId = document.getString("calendarId");
                            String calendarName = document.getString("calendarName");
                            String carBrand = document.getString("carBrand");
                            String carModel = document.getString("carModel");

                            otherCalendarList.add(new MyCalendar(calendarId, calendarName, carBrand, carModel));
                        }
                        otherCalendarsAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(OtherCalendarsActivity.this, "Failed to load shared calendars: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showUserListDialog() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> userList = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String userEmail = document.getString("email");
                            if (userEmail != null && !userEmail.equals(mAuth.getCurrentUser().getEmail())) {
                                userList.add(userEmail);
                            }
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Выберите пользователя для обмена")
                                .setItems(userList.toArray(new String[0]), (dialog, which) -> {
                                    String selectedUser = userList.get(which);
                                    shareCalendarWithUser(selectedUser);
                                })
                                .setNegativeButton("Отмена", null)
                                .show();
                    } else {
                        Toast.makeText(this, "Failed to load users: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void shareCalendarWithUser(String userEmail) {
        // Покажем пользователю список его календарей для выбора
        db.collection("users").document(mAuth.getCurrentUser().getUid()).collection("calendars")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MyCalendar> userCalendars = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String calendarId = document.getId();
                            String calendarName = document.getString("calendarName");
                            String carBrand = document.getString("carBrand");
                            String carModel = document.getString("carModel");
                            userCalendars.add(new MyCalendar(calendarId, calendarName, carBrand, carModel));
                        }

                        // Создаем список имен календарей для отображения в диалоге
                        String[] calendarNames = new String[userCalendars.size()];
                        for (int i = 0; i < userCalendars.size(); i++) {
                            calendarNames[i] = userCalendars.get(i).getCalendarName();
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Выберите календарь для обмена")
                                .setItems(calendarNames, (dialog, which) -> {
                                    MyCalendar selectedCalendar = userCalendars.get(which);
                                    shareCalendarWithUser(userEmail, selectedCalendar);
                                })
                                .setNegativeButton("Отмена", null)
                                .show();
                    } else {
                        Toast.makeText(this, "Failed to load calendars: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void shareCalendarWithUser(String userEmail, MyCalendar calendar) {
        db.collection("users").whereEqualTo("email", userEmail).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                        String userId = userDoc.getId();

                        db.collection("shared_calendars").document(userId).collection("calendars")
                                .document(calendar.getCalendarId())
                                .set(calendar)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("OtherCalendarsActivity", "Календарь успешно поделён: " + calendar.getCalendarId());
                                    Toast.makeText(this, "Календарь успешно поделён", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("OtherCalendarsActivity", "Ошибка при обмене календарем", e);
                                    Toast.makeText(this, "Ошибка при обмене календарем: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.d("OtherCalendarsActivity", "Пользователь не найден");
                        Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void saveEventToFirestore(MyCalendar calendar, String selectedDate, String selectedCategory, String selectedAction, double eventCost, double eventMileage) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (userId == null) {
            Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        CarEvent event = new CarEvent(
                calendar.getCalendarName(),
                calendar.getCarBrand(),
                calendar.getCarModel(),
                selectedDate,
                selectedCategory + ": " + selectedAction,
                eventCost,
                eventMileage
        );

        Log.d("OtherCalendarsActivity", "Сохраняем событие: " + event); // Лог события

        db.collection("shared_calendars").document(userId).collection("calendars")
                .whereEqualTo("calendarName", calendar.getCalendarName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot calendarDoc = task.getResult().getDocuments().get(0);

                        db.collection("shared_calendars").document(userId).collection("calendars")
                                .document(calendarDoc.getId())
                                .collection("events")
                                .add(event)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(context, "Событие сохранено", Toast.LENGTH_SHORT).show();
                                    Log.d("OtherCalendarsActivity", "Событие успешно сохранено: " + documentReference.getId());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Ошибка сохранения события", Toast.LENGTH_SHORT).show();
                                    Log.e("OtherCalendarsActivity", "Ошибка сохранения события", e);
                                });
                    } else {
                        Log.d("OtherCalendarsActivity", "Календарь не найден для события");
                    }
                });
    }










    private void loadEventsForCalendar(MyCalendar calendar, MaterialCalendarView materialCalendarView) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("shared_calendars").document(userId).collection("calendars")
                .whereEqualTo("calendarName", calendar.getCalendarName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot calendarDoc = task.getResult().getDocuments().get(0);
                        db.collection("shared_calendars").document(userId).collection("calendars")
                                .document(calendarDoc.getId())
                                .collection("events")
                                .get()
                                .addOnCompleteListener(eventsTask -> {
                                    if (eventsTask.isSuccessful()) {
                                        List<DocumentSnapshot> events = eventsTask.getResult().getDocuments();
                                        for (DocumentSnapshot event : events) {
                                            String eventDate = event.getString("eventDate");

                                            try {
                                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                                LocalDate localDate = LocalDate.parse(eventDate, formatter);

                                                // Используем LocalDate для CalendarDay
                                                CalendarDay calendarDay = CalendarDay.from(localDate);

                                                // Добавляем дату в календарь
                                                materialCalendarView.setDateSelected(calendarDay, true);

                                                // Добавляем красный кружок
                                                Drawable redCircle = getResources().getDrawable(R.drawable.event_circle);
                                                EventDecorator decorator = new EventDecorator(calendarDay, redCircle);
                                                materialCalendarView.addDecorator(decorator);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        materialCalendarView.setOnDateChangedListener((widget, date, selected) -> {
                                            showEventDetailsForDate(calendar, date, materialCalendarView);
                                        });
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Не удалось найти календарь", Toast.LENGTH_SHORT).show();
                    }
                });
    }







    private void showEventDetailsForDate(MyCalendar calendar, CalendarDay selectedDate, MaterialCalendarView materialCalendarView) {
        int year = selectedDate.getYear();
        int month = selectedDate.getMonth() - 1; // Месяцы в Java Calendar начинаются с 0
        int day = selectedDate.getDay();

        Calendar calendarInstance = Calendar.getInstance();
        calendarInstance.set(year, month, day, 0, 0, 0);
        Date date = calendarInstance.getTime();

        String selectedDateString = new SimpleDateFormat("yyyy-MM-dd").format(date);

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("shared_calendars").document(userId)
                .collection("calendars").document(calendar.getCalendarId())
                .collection("events").whereEqualTo("eventDate", selectedDateString)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> events = task.getResult().getDocuments();
                        if (!events.isEmpty()) {
                            StringBuilder eventDetails = new StringBuilder();
                            for (DocumentSnapshot event : events) {
                                eventDetails.append(event.getString("eventDescription")).append("\n");
                            }

                            new AlertDialog.Builder(this)
                                    .setTitle("События на " + selectedDateString)
                                    .setMessage(eventDetails.toString())
                                    .setPositiveButton("ОК", null)
                                    .show();
                        } else {
                            Toast.makeText(this, "Нет событий на эту дату", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




}
