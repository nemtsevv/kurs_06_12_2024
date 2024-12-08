package com.example.kurs_06_12_2024;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

// Добавляем правильный импорт
import android.app.DatePickerDialog;

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
                                    showDatePickerDialog(calendar); // Показать диалог выбора даты
                                    break;
                                case 1:
                                    // Показать календарь
                                    Toast.makeText(context, "Показать календарь: " + calendar.getCalendarName(), Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    deleteCalendar(calendar, position); // Удалить календарь
                                    break;
                            }
                        })
                .setCancelable(true)
                .show();
    }

    private void showDatePickerDialog(MyCalendar calendar) {
        // Получаем текущую дату
        Calendar calendarInstance = Calendar.getInstance();
        int year = calendarInstance.get(Calendar.YEAR);
        int month = calendarInstance.get(Calendar.MONTH);
        int dayOfMonth = calendarInstance.get(Calendar.DAY_OF_MONTH);

        // Создаем DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(android.widget.DatePicker view, int year1, int month1, int dayOfMonth1) {
                // Выбрали дату, теперь запрашиваем описание события
                showEventInputDialog(calendar, year1, month1, dayOfMonth1);
            }
        }, year, month, dayOfMonth);

        datePickerDialog.show(); // Показываем диалог выбора даты
    }

    private void showEventInputDialog(MyCalendar calendar, int year, int month, int dayOfMonth) {
        // Диалог для ввода описания события
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

                    // Сохраняем событие в Firestore
                    saveEventToFirestore(calendar, year, month, dayOfMonth, eventDescription);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void saveEventToFirestore(MyCalendar calendar, int year, int month, int dayOfMonth, String eventDescription) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Проверка, что текущий пользователь авторизован
        if (userId == null) {
            Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаем объект события
        CarEvent event = new CarEvent(
                calendar.getCalendarName(),
                calendar.getCarBrand(),
                calendar.getCarModel(),
                year, month, dayOfMonth, eventDescription);

        // Логируем объект события
        Log.d("Firestore", "Событие, которое добавляем: " + event.toString());

        // Находим календарь в Firestore по calendarName
        db.collection("users").document(userId).collection("calendars")
                .whereEqualTo("calendarName", calendar.getCalendarName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Получаем первый документ, так как calendarName уникален
                        DocumentSnapshot calendarDoc = task.getResult().getDocuments().get(0);

                        // Добавляем событие в подколлекцию "events" этого календаря
                        db.collection("users").document(userId).collection("calendars")
                                .document(calendarDoc.getId()) // Идентификатор документа календаря
                                .collection("events") // Коллекция событий
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
