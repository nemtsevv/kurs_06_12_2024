package com.example.kurs_06_12_2024;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        // Получаем сообщение уведомления и ID уведомления
        String eventMessage = params.getExtras().getString("eventMessage");
        int notificationId = params.getExtras().getInt("notificationId");

        // Показываем уведомление
        showNotification(eventMessage, notificationId);

        // Завершаем выполнение работы
        return false;  // false означает, что задача завершена
    }

    private void showNotification(String eventMessage, int notificationId) {
        // Создаем PendingIntent (если нужно делать уведомление кликабельным)
        Intent intent = new Intent(this, MainActivity.class); // Вместо нового Activity, используем существующее
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Создаем уведомление с использованием BigTextStyle для длинного текста
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_notification)  // Иконка уведомления
                .setContentTitle("Напоминание о событии")
                .setContentText(eventMessage)  // Краткое описание уведомления (когда оно свернуто)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(eventMessage))  // Развернутый текст (если уведомление длинное)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification))  // Большая иконка
                .setContentIntent(pendingIntent);  // Устанавливаем PendingIntent для открытия активности

        // Получаем NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Создание канала уведомлений для Android 8.0 и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default";
            NotificationChannel existingChannel = notificationManager.getNotificationChannel(channelId);

            if (existingChannel == null) {
                NotificationChannel channel = new NotificationChannel(channelId, "Events", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            builder.setChannelId(channelId);
        }

        // Отправляем уведомление
        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false; // Не требуется повторять задачу
    }
}
