package com.example.kurs_06_12_2024;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
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


public class CalendarsAdapter extends RecyclerView.Adapter<CalendarsAdapter.CalendarViewHolder> {

    private List<MyCalendar> calendarList;
    private Context context;

    public CalendarsAdapter(Context context, List<MyCalendar> calendarList) {
        this.context = context;
        this.calendarList = calendarList;
    }

    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CalendarViewHolder holder, int position) {
        MyCalendar calendar = calendarList.get(position);
        holder.calendarNameTextView.setText(calendar.getCalendarName());
        holder.carNameTextView.setText(calendar.getCarBrand() + " " + calendar.getCarModel());

        holder.itemView.setOnClickListener(v -> showDialog(calendar, position));
    }

    @Override
    public int getItemCount() {
        return calendarList.size();
    }

    private void showDialog(MyCalendar calendar, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Выберите действие")
                .setItems(new String[]{"Добавить событие", "Показать календарь", "Удалить календарь"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    showMaterialDatePicker(calendar); // Используем MaterialDatePicker
                                    break;
                                case 1:
                                    showCalendarDialog(calendar); // Показать календарь с выделением дат
                                    break;
                                case 2:
                                    deleteCalendar(calendar, position); // Удалить календарь
                                    break;
                            }
                        })
                .setCancelable(true)
                .show();
    }

    private void showMaterialDatePicker(MyCalendar calendar) {
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Выберите дату")
                .build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String selectedDate = dateFormat.format(selection);

            showEventInputDialog(calendar, selectedDate);
        });

        // Показываем MaterialDatePicker
        if (context instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) context;
            materialDatePicker.show(activity.getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        } else {
            Toast.makeText(context, "Ошибка: контекст не является экземпляром AppCompatActivity", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEventInputDialog(MyCalendar calendar, String selectedDate) {
        final EditText eventEditText = new EditText(context);
        eventEditText.setHint("Введите описание события, например, замена масла");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Введите событие")
                .setView(eventEditText)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String eventDescription = eventEditText.getText().toString().trim();
                    if (eventDescription.isEmpty()) {
                        Toast.makeText(context, "Описание события не может быть пустым", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    saveEventToFirestore(calendar, selectedDate, eventDescription);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void saveEventToFirestore(MyCalendar calendar, String selectedDate, String eventDescription) {
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
                selectedDate, eventDescription);

        Log.d("Firestore", "Событие, которое добавляем: " + event.toString());

        db.collection("users").document(userId).collection("calendars")
                .whereEqualTo("calendarName", calendar.getCalendarName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot calendarDoc = task.getResult().getDocuments().get(0);

                        db.collection("users").document(userId).collection("calendars")
                                .document(calendarDoc.getId())
                                .collection("events")
                                .add(event)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("Firestore", "Событие успешно добавлено с ID: " + documentReference.getId());
                                    Toast.makeText(context, "Событие добавлено", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Ошибка при добавлении события", e);
                                    Toast.makeText(context, "Ошибка при добавлении события", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "Не найден календарь для этого автомобиля", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteCalendar(MyCalendar calendar, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).collection("calendars")
                .whereEqualTo("calendarName", calendar.getCalendarName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        calendarList.remove(position);
                                        notifyItemRemoved(position);
                                        Toast.makeText(context, "Календарь удалён", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(context, "Ошибка при поиске календаря", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCalendarDialog(MyCalendar calendar) {
        // Создаем диалог для отображения календаря
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Внешний вид календаря
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_calendar, null);
        MaterialCalendarView materialCalendarView = view.findViewById(R.id.calendarView);

        // Загружаем события для этого календаря
        loadEventsForCalendar(calendar, materialCalendarView);

        builder.setTitle("Календарь: " + calendar.getCalendarName())
                .setView(view)
                .setPositiveButton("Закрыть", null)
                .create()
                .show();
    }

    private void loadEventsForCalendar(MyCalendar calendar, MaterialCalendarView materialCalendarView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).collection("calendars")
                .whereEqualTo("calendarName", calendar.getCalendarName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot calendarDoc = task.getResult().getDocuments().get(0);
                        db.collection("users").document(userId).collection("calendars")
                                .document(calendarDoc.getId())
                                .collection("events")
                                .get()
                                .addOnCompleteListener(eventsTask -> {
                                    if (eventsTask.isSuccessful()) {
                                        List<DocumentSnapshot> events = eventsTask.getResult().getDocuments();
                                        for (DocumentSnapshot event : events) {
                                            String eventDate = event.getString("eventDate");

                                            try {
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                Date date = sdf.parse(eventDate);
                                                Calendar calendarDate = Calendar.getInstance();
                                                calendarDate.setTime(date);

                                                // Преобразуем в LocalDate
                                                LocalDate localDate = LocalDate.of(calendarDate.get(Calendar.YEAR),
                                                        calendarDate.get(Calendar.MONTH) + 1,
                                                        calendarDate.get(Calendar.DAY_OF_MONTH));

                                                // Добавляем дату в календарь
                                                CalendarDay calendarDay = CalendarDay.from(localDate);
                                                materialCalendarView.setDateSelected(calendarDay, true);

                                                // Добавляем красный кружок
                                                Drawable redCircle = context.getResources().getDrawable(R.drawable.event_circle);
                                                EventDecorator decorator = new EventDecorator(calendarDay, redCircle);
                                                materialCalendarView.addDecorator(decorator);

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        materialCalendarView.setOnDateChangedListener((widget, date, selected) -> {
                                            showEventDetailsForDate(calendar, date);
                                        });
                                    }
                                });
                    }
                });
    }

    private void showEventDetailsForDate(MyCalendar calendar, CalendarDay selectedDate) {
        int year = selectedDate.getYear();
        int month = selectedDate.getMonth() - 1;
        int day = selectedDate.getDay();

        Calendar calendarInstance = Calendar.getInstance();
        calendarInstance.set(year, month, day, 0, 0, 0);
        Date date = calendarInstance.getTime();

        String selectedDateString = new SimpleDateFormat("yyyy-MM-dd").format(date);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).collection("calendars")
                .whereEqualTo("calendarName", calendar.getCalendarName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot calendarDoc = task.getResult().getDocuments().get(0);
                        db.collection("users").document(userId).collection("calendars")
                                .document(calendarDoc.getId())
                                .collection("events")
                                .whereEqualTo("eventDate", selectedDateString)
                                .get()
                                .addOnCompleteListener(eventsTask -> {
                                    if (eventsTask.isSuccessful()) {
                                        List<DocumentSnapshot> events = eventsTask.getResult().getDocuments();
                                        if (!events.isEmpty()) {
                                            StringBuilder eventDetails = new StringBuilder();
                                            for (DocumentSnapshot event : events) {
                                                eventDetails.append(event.getString("eventDescription")).append("\n");
                                            }

                                            new AlertDialog.Builder(context)
                                                    .setTitle("События на " + selectedDateString)
                                                    .setMessage(eventDetails.toString())
                                                    .setPositiveButton("ОК", null)
                                                    .show();
                                        } else {
                                            Toast.makeText(context, "Нет событий на эту дату", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView calendarNameTextView;
        TextView carNameTextView;

        public CalendarViewHolder(View itemView) {
            super(itemView);
            calendarNameTextView = itemView.findViewById(R.id.calendarNameTextView);
            carNameTextView = itemView.findViewById(R.id.carNameTextView);
        }
    }
}
