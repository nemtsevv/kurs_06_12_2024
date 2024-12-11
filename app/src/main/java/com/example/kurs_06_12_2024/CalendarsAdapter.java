package com.example.kurs_06_12_2024;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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
        // Загружаем массивы из arrays.xml
        String[] eventCategories = context.getResources().getStringArray(R.array.event_categories);
        String[] regularMaintenanceActions = context.getResources().getStringArray(R.array.regular_maintenance_actions);
        String[] repairActions = context.getResources().getStringArray(R.array.repair_actions);

        // Создаём контейнер для Spinner'ов
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_event_input, null);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        Spinner actionSpinner = dialogView.findViewById(R.id.actionSpinner);

        // Добавляем поле для ввода стоимости
        EditText costEditText = dialogView.findViewById(R.id.costEditText); // новое поле для стоимости

        // Устанавливаем адаптер для категории
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, eventCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Локальная переменная для actionAdapter
        final ArrayAdapter<String> actionAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, new String[]{});  // Изначально пустой список
        actionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(actionAdapter);

        // Меняем действия в зависимости от выбранной категории
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // В зависимости от выбранной категории, обновляем действия
                String[] newActions;
                if (eventCategories[position].equals("Регулярное обслуживание")) {
                    newActions = regularMaintenanceActions;
                } else if (eventCategories[position].equals("Необходимость ремонта")) {
                    newActions = repairActions;
                } else {
                    newActions = new String[] {};  // Пустой список для "Прочее"
                }

                // Создаем новый адаптер с обновленным списком действий
                ArrayAdapter<String> newActionAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, newActions);
                newActionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                actionSpinner.setAdapter(newActionAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Обработчик для случая, если ничего не выбрано
            }
        });

        // Создаем и показываем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Введите событие для " + calendar.getCarBrand() + " " + calendar.getCarModel())
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    // Получаем выбранную категорию и действие
                    String selectedCategory = categorySpinner.getSelectedItem().toString();
                    String selectedAction = actionSpinner.getSelectedItem().toString();

                    // Получаем стоимость события
                    String costString = costEditText.getText().toString();
                    double eventCost = 0.0;
                    if (!costString.isEmpty()) {
                        try {
                            eventCost = Double.parseDouble(costString);
                        } catch (NumberFormatException e) {
                            Toast.makeText(context, "Введите правильную стоимость", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Проверяем, выбраны ли значения
                    if (selectedCategory.isEmpty() || selectedAction.isEmpty()) {
                        Toast.makeText(context, "Пожалуйста, выберите категорию и действие", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Сохраняем событие в Firestore
                    saveEventToFirestore(calendar, selectedDate, selectedCategory, selectedAction, eventCost);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }


    private void saveEventToFirestore(MyCalendar calendar, String selectedDate, String selectedCategory, String selectedAction, double eventCost) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (userId == null) {
            Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаем объект события с добавлением категории, действия и стоимости
        CarEvent event = new CarEvent(
                calendar.getCalendarName(),
                calendar.getCarBrand(),
                calendar.getCarModel(),
                selectedDate,
                selectedCategory + ": " + selectedAction, // Сохраняем категорию и действие
                eventCost
        );

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
                                    Toast.makeText(context, "Событие добавлено", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
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
