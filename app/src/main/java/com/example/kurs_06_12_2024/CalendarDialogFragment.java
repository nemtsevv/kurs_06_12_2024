package com.example.kurs_06_12_2024;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarDialogFragment extends DialogFragment {

    private MyCalendar calendar;
    private MaterialCalendarView materialCalendarView;

    public CalendarDialogFragment(MyCalendar calendar) {
        this.calendar = calendar;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_calendar, container, false);
        materialCalendarView = view.findViewById(R.id.calendarView);

        loadEventsForCalendar(calendar);

        return view;
    }

    private void loadEventsForCalendar(MyCalendar calendar) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("shared_calendars").document(userId).collection("calendars")
                .whereEqualTo("calendarName", calendar.getCalendarName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot calendarDoc = task.getResult().getDocuments().get(0);
                        Log.d("CalendarDialogFragment", "Calendar found: " + calendarDoc.getId());
                        FirebaseFirestore.getInstance().collection("shared_calendars").document(userId).collection("calendars")
                                .document(calendarDoc.getId())
                                .collection("events")
                                .get()
                                .addOnCompleteListener(eventsTask -> {
                                    if (eventsTask.isSuccessful()) {
                                        List<DocumentSnapshot> events = eventsTask.getResult().getDocuments();
                                        Log.d("CalendarDialogFragment", "Events loaded: " + events.size());
                                        for (DocumentSnapshot event : events) {
                                            String eventDate = event.getString("eventDate");
                                            Log.d("CalendarDialogFragment", "Event date: " + eventDate);

                                            try {
                                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                                LocalDate localDate = LocalDate.parse(eventDate, formatter);

                                                CalendarDay calendarDay = CalendarDay.from(localDate);
                                                materialCalendarView.setDateSelected(calendarDay, true);

                                                Drawable redCircle = getResources().getDrawable(R.drawable.event_circle);
                                                EventDecorator decorator = new EventDecorator(calendarDay, redCircle);
                                                materialCalendarView.addDecorator(decorator);

                                            } catch (Exception e) {
                                                Log.e("CalendarDialogFragment", "Error parsing event date", e);
                                            }
                                        }

                                        materialCalendarView.setOnDateChangedListener((widget, date, selected) -> {
                                            showEventDetailsForDate(calendar, date, materialCalendarView);
                                        });
                                    } else {
                                        Log.d("CalendarDialogFragment", "Failed to load events: " + eventsTask.getException().getMessage());
                                        Toast.makeText(getContext(), "Не удалось загрузить события", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.d("CalendarDialogFragment", "No calendar found");
                        Toast.makeText(getContext(), "Не удалось найти календарь", Toast.LENGTH_SHORT).show();
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

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("shared_calendars").document(userId)
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

                            new AlertDialog.Builder(getContext())
                                    .setTitle("События на " + selectedDateString)
                                    .setMessage(eventDetails.toString())
                                    .setPositiveButton("ОК", null)
                                    .show();
                        } else {
                            Toast.makeText(getContext(), "Нет событий на эту дату", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("CalendarDialogFragment", "Failed to load event details: " + task.getException().getMessage());
                    }
                });
    }
}


