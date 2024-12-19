package com.example.kurs_06_12_2024;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ImageButton logoutButton;
    private Button myCarsButton, myCalendarsButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Context context;

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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
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
        logoutButton.setOnClickListener(v -> signOut());

        // Обработчик нажатия на кнопку "Мои автомобили"
        myCarsButton.setOnClickListener(v -> openCarsActivity());

        // Обработчик нажатия на кнопку "Мои календари"
        myCalendarsButton.setOnClickListener(v -> openCalendarsActivity());

        // Загрузка календарей и планирование уведомлений
        loadCalendarsAndScheduleNotifications();
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

    // Загрузка календарей и планирование уведомлений
    private void loadCalendarsAndScheduleNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).collection("calendars")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot calendarDoc : task.getResult()) {
                            MyCalendar calendar = calendarDoc.toObject(MyCalendar.class);
                            if (calendar != null) {
                                Log.d("MainActivity", "Processing calendar: " + calendar.getCalendarName());
                                // Планируем уведомления для каждого календаря
                                scheduleNotificationsForUpcomingEvents(calendar);
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Нет доступных календарей", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Планирование уведомлений для всех событий выбранного календаря
    private void scheduleNotificationsForUpcomingEvents(MyCalendar calendar) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (userId == null) {
            Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId).collection("calendars")
                .whereEqualTo("calendarName", calendar.getCalendarName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot calendarDoc = task.getResult().getDocuments().get(0);

                        db.collection("users").document(userId).collection("calendars")
                                .document(calendarDoc.getId())
                                .collection("events")
                                .get()
                                .addOnCompleteListener(eventsTask -> {
                                    if (eventsTask.isSuccessful()) {
                                        for (DocumentSnapshot eventDoc : eventsTask.getResult()) {
                                            // Извлекаем данные о событии
                                            String eventDate = eventDoc.getString("eventDate");
                                            String eventMessage = eventDoc.getString("eventDescription");
                                            String calendarName = calendar.getCalendarName();

                                            Log.d("MainActivity", "Event found: " + eventMessage + " on " + eventDate);

                                            // Преобразуем строку в дату
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                            try {
                                                Date eventDateObj = sdf.parse(eventDate);
                                                if (eventDateObj != null) {
                                                    long eventTimeMillis = eventDateObj.getTime();

                                                    // Уведомление сразу при входе в приложение (на событие)
                                                    scheduleNotification(eventMessage, eventTimeMillis, eventMessage.hashCode());

                                                    // Уведомление за 24 часа до события
                                                    long oneDayBeforeMillis = eventTimeMillis - 24 * 60 * 60 * 1000;  // 24 часа до события
                                                    // Уведомление за 3 часа до события
                                                    long threeHoursBeforeMillis = eventTimeMillis - 3 * 60 * 60 * 1000;

                                                    // Форматируем сообщение для уведомления
                                                    String notificationMessage = "Дата события: " + eventDate + "\n" +
                                                            "Календарь: " + calendarName + "\n" +
                                                            "Событие: " + eventMessage;

                                                    // Проверяем, не запланировано ли уже уведомление за 24 часа
                                                    if (oneDayBeforeMillis > System.currentTimeMillis()) {
                                                        scheduleNotification(notificationMessage, oneDayBeforeMillis, eventMessage.hashCode() + 1);  // Уникальный ID
                                                    }

                                                    // Проверка, не запланировано ли уведомление за 3 часа
                                                    if (threeHoursBeforeMillis > System.currentTimeMillis()) {
                                                        scheduleNotification(notificationMessage, threeHoursBeforeMillis, eventMessage.hashCode() + 2); // Уникальный ID
                                                    }

                                                    // Планируем уведомления в день события (каждые 3 часа)
                                                    long startOfEventDayMillis = eventTimeMillis - eventTimeMillis % (24 * 60 * 60 * 1000); // 00:00 того дня
                                                    long endOfEventDayMillis = startOfEventDayMillis + 24 * 60 * 60 * 1000; // Конец дня (23:59)
                                                    for (long time = startOfEventDayMillis; time < endOfEventDayMillis; time += 3 * 60 * 60 * 1000) {
                                                        scheduleNotification(notificationMessage, time, eventMessage.hashCode() + 3);  // Уникальный ID
                                                    }

                                                    // Уведомления за сегодня, если событие завтра
                                                    if (eventTimeMillis - System.currentTimeMillis() <= 24 * 60 * 60 * 1000) {
                                                        long startOfDayMillis = System.currentTimeMillis() - (System.currentTimeMillis() % (24 * 60 * 60 * 1000));  // начало сегодняшнего дня
                                                        for (long time = startOfDayMillis; time < eventTimeMillis; time += 3 * 60 * 60 * 1000) {
                                                            scheduleNotification(eventMessage, time, eventMessage.hashCode() + 4);
                                                        }
                                                    }
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void scheduleNotification(String eventMessage, long triggerTimeMillis, int notificationId) {
        // Используем JobScheduler
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        // Логируем время, когда будет запланировано уведомление
        Log.d("MainActivity", "Scheduling notification with ID " + notificationId + " at: " + new Date(triggerTimeMillis));

        // Компонент для NotificationJobService
        ComponentName componentName = new ComponentName(this, NotificationJobService.class);

        // Передаем дополнительные данные
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString("eventMessage", eventMessage);
        bundle.putInt("notificationId", notificationId);  // Уникальный ID для уведомлений

        // Планируем задачу с использованием JobScheduler
        JobInfo jobInfo = new JobInfo.Builder(notificationId, componentName)
                .setMinimumLatency(triggerTimeMillis - System.currentTimeMillis())  // Задержка до выполнения задачи
                .setOverrideDeadline(triggerTimeMillis - System.currentTimeMillis())  // Максимальная задержка
                .setExtras(bundle)  // Передаем данные (например, сообщение)
                .build();

        // Запускаем задачу через JobScheduler
        if (jobScheduler != null) {
            jobScheduler.schedule(jobInfo);
        } else {
            Toast.makeText(this, "Ошибка при планировании уведомления", Toast.LENGTH_SHORT).show();
        }

        // Создаем только одно уведомление с полным текстом
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_notification)  // Укажите иконку уведомления
                .setContentTitle("Напоминание о событии")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(eventMessage))  // Только полный текст события
                .setAutoCancel(true)  // Уведомление исчезает при нажатии
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // Высокий приоритет для уведомления
                .setDefaults(Notification.DEFAULT_ALL);  // Все стандартные параметры (звук, вибрация и т.д.)

        // Получаем NotificationManager
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
    }





}
