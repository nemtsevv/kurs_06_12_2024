package com.example.kurs_06_12_2024;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

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
                                    showCalendarListDialog(selectedUser);
                                })
                                .setNegativeButton("Отмена", null)
                                .show();
                    } else {
                        Toast.makeText(this, "Failed to load users: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCalendarListDialog(String userEmail) {
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

                        // 1. Сохраняем календарь у получателя
                        db.collection("shared_calendars").document(userId).collection("calendars")
                                .document(calendar.getCalendarId())
                                .set(calendar)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("OtherCalendarsActivity", "Календарь успешно поделён: " + calendar.getCalendarId());
                                    Toast.makeText(this, "Календарь успешно поделён", Toast.LENGTH_SHORT).show();

                                    // 2. Копируем события в календарь получателя
                                    copyEventsToSharedCalendar(userId, calendar);
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


    private void copyEventsToSharedCalendar(String userId, MyCalendar calendar) {
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid()) // Это текущий пользователь, который делится календарем
                .collection("calendars")
                .document(calendar.getCalendarId()) // ID календаря, который делится
                .collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> events = task.getResult().getDocuments();
                        if (events.isEmpty()) {
                            Log.d("OtherCalendarsActivity", "События не найдены в исходном календаре.");
                        }

                        // Копируем каждое событие в календарь получателя
                        for (DocumentSnapshot eventDoc : events) {
                            CarEvent event = eventDoc.toObject(CarEvent.class);
                            if (event != null) {
                                Log.d("OtherCalendarsActivity", "Копируем событие: " + event.getEventDescription());

                                // Добавляем событие в коллекцию events получателя с помощью Map
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                CollectionReference eventsRef = db.collection("shared_calendars")
                                        .document(userId)  // Пользователь, с которым делятся календарем
                                        .collection("calendars")
                                        .document(calendar.getCalendarId())  // ID календаря
                                        .collection("events");

                                Map<String, Object> eventData = new HashMap<>();
                                eventData.put("calendarName", event.getCalendarName());
                                eventData.put("carBrand", event.getCarBrand());
                                eventData.put("carModel", event.getCarModel());
                                eventData.put("eventDate", event.getEventDate());
                                eventData.put("eventDescription", event.getEventDescription());
                                eventData.put("eventCost", event.getEventCost());
                                eventData.put("eventMileage", event.getEventMileage());

                                // Записываем в Firestore
                                eventsRef.add(eventData)
                                        .addOnSuccessListener(documentReference -> {
                                            Log.d("OtherCalendarsActivity", "Событие успешно скопировано: " + event.getEventDescription());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("OtherCalendarsActivity", "Ошибка при копировании события", e);
                                        });
                            }
                        }
                    } else {
                        Log.e("OtherCalendarsActivity", "Ошибка при получении событий исходного календаря", task.getException());
                    }
                });
    }






    public void loadEventsForCalendar(MyCalendar calendar, MaterialCalendarView materialCalendarView) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Загружаем события из коллекции shared_calendars
        db.collection("shared_calendars").document(userId).collection("calendars")
                .document(calendar.getCalendarId()).collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> events = task.getResult().getDocuments();
                        for (DocumentSnapshot event : events) {
                            String eventDate = event.getString("eventDate");

                            try {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                LocalDate localDate = LocalDate.parse(eventDate, formatter);

                                CalendarDay calendarDay = CalendarDay.from(localDate);
                                materialCalendarView.setDateSelected(calendarDay, true);

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
                    } else {
                        Toast.makeText(this, "Не удалось найти календарь", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showEventDetailsForDate(MyCalendar calendar, CalendarDay selectedDate, MaterialCalendarView materialCalendarView) {
        int year = selectedDate.getYear();
        int month = selectedDate.getMonth() - 1;
        // Месяцы в Java Calendar начинаются с 0
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
                    } else {
                        Toast.makeText(this, "Ошибка загрузки событий: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
