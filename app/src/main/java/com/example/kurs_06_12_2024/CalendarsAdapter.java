package com.example.kurs_06_12_2024;
//Добавлены уведомления, надо доделать, последняя версия на гитхабе
//без уведомлений
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.PersistableBundle;
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

import java.text.ParseException;
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
        EditText costEditText = dialogView.findViewById(R.id.eventCostEditText); // Поле для ввода стоимости
        EditText mileageEditText = dialogView.findViewById(R.id.eventMileageEditText); // Поле для ввода пробега

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
                    String eventCostStr = costEditText.getText().toString();
                    String eventMileageStr = mileageEditText.getText().toString();

                    // Проверяем, выбраны ли значения
                    if (selectedCategory.isEmpty() || selectedAction.isEmpty() || eventCostStr.isEmpty() || eventMileageStr.isEmpty()) {
                        Toast.makeText(context, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double eventCost = Double.parseDouble(eventCostStr);
                    double eventMileage = Double.parseDouble(eventMileageStr);

                    // Сохраняем событие в Firestore
                    saveEventToFirestore(calendar, selectedDate, selectedCategory, selectedAction, eventCost, eventMileage);
                    //scheduleNotificationsForUpcomingEvents(calendar);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }



    private void saveEventToFirestore(MyCalendar calendar, String selectedDate, String selectedCategory, String selectedAction, double eventCost, double eventMileage) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (userId == null) {
            Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаем объект события с добавлением категории, действия, стоимости и пробега
        CarEvent event = new CarEvent(
                calendar.getCalendarName(),
                calendar.getCarBrand(),
                calendar.getCarModel(),
                selectedDate,
                selectedCategory + ": " + selectedAction, // Сохраняем категорию и действие
                eventCost,
                eventMileage
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
                                    Toast.makeText(context, "Событие сохранено", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Ошибка сохранения события", Toast.LENGTH_SHORT).show();
                                });
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
                        // Получаем документ календаря
                        for (DocumentSnapshot document : task.getResult()) {
                            // Удаляем все события, связанные с этим календарем
                            db.collection("users").document(userId).collection("calendars")
                                    .document(document.getId())
                                    .collection("events")
                                    .get()
                                    .addOnCompleteListener(eventsTask -> {
                                        if (eventsTask.isSuccessful()) {
                                            for (DocumentSnapshot eventDoc : eventsTask.getResult()) {
                                                // Удаляем каждое событие
                                                eventDoc.getReference().delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Событие удалено
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(context, "Ошибка при удалении события", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        }
                                    });

                            // Удаляем сам календарь
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Удаляем календарь из списка и обновляем UI
                                        calendarList.remove(position);
                                        notifyItemRemoved(position);
                                        Toast.makeText(context, "Календарь и все события удалены", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Ошибка при удалении календаря", Toast.LENGTH_SHORT).show();
                                    });
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

                                        // Теперь, при изменении даты, будем передавать MaterialCalendarView
                                        materialCalendarView.setOnDateChangedListener((widget, date, selected) -> {
                                            showEventDetailsForDate(calendar, date, materialCalendarView);
                                        });
                                    }
                                });
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

                                            // Диалог с возможностью удалить событие
                                            new AlertDialog.Builder(context)
                                                    .setTitle("События на " + selectedDateString)
                                                    .setMessage(eventDetails.toString())
                                                    .setPositiveButton("ОК", null)
                                                    .setNegativeButton("Удалить событие", (dialog, which) -> {
                                                        // Удаляем событие при нажатии на кнопку "Удалить"
                                                        deleteEventFromFirestore(events.get(0), calendar, materialCalendarView, selectedDate);
                                                    })
                                                    .show();
                                        } else {
                                            Toast.makeText(context, "Нет событий на эту дату", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
    }





    private void deleteEventFromFirestore(DocumentSnapshot eventDoc, MyCalendar calendar, MaterialCalendarView materialCalendarView, CalendarDay selectedDate) {
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
                                .document(eventDoc.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Очистить все декораторы
                                    materialCalendarView.removeDecorators();

                                    // Перезагрузить события для данного календаря и даты
                                    loadEventsForCalendar(calendar, materialCalendarView);

                                    Toast.makeText(context, "Событие удалено", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Ошибка при удалении события", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }


//    private void scheduleNotificationsForUpcomingEvents(MyCalendar calendar) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        if (userId == null) {
//            Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        db.collection("users").document(userId).collection("calendars")
//                .whereEqualTo("calendarName", calendar.getCalendarName())
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
//                        DocumentSnapshot calendarDoc = task.getResult().getDocuments().get(0);
//
//                        db.collection("users").document(userId).collection("calendars")
//                                .document(calendarDoc.getId())
//                                .collection("events")
//                                .get()
//                                .addOnCompleteListener(eventsTask -> {
//                                    if (eventsTask.isSuccessful()) {
//                                        for (DocumentSnapshot eventDoc : eventsTask.getResult()) {
//                                            // Извлекаем данные о событии
//                                            String eventDate = eventDoc.getString("eventDate");
//                                            String eventMessage = eventDoc.getString("eventDescription");
//                                            String calendarName = calendar.getCalendarName();
//
//                                            // Преобразуем строку в дату
//                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                                            try {
//                                                Date eventDateObj = sdf.parse(eventDate);
//                                                if (eventDateObj != null) {
//                                                    long eventTimeMillis = eventDateObj.getTime();
//
//                                                    // Планируем уведомление за день до события (24 часа)
//                                                    long oneDayBeforeMillis = eventTimeMillis - 24 * 60 * 60 * 1000;  // 24 часа до события
//                                                    long notificationTimeMillis = eventTimeMillis;  // В день события
//
//                                                    // Форматируем сообщение для уведомления
//                                                    String notificationMessage = "Дата события: " + eventDate + "\n" +
//                                                            "Календарь: " + calendarName + "\n" +
//                                                            "Событие: " + eventMessage;
//
//                                                    // Планируем два уведомления
//                                                    scheduleNotification(notificationMessage, oneDayBeforeMillis, 1);  // Уведомление за день
//                                                    scheduleNotification(notificationMessage, notificationTimeMillis, 2);  // Уведомление в день события
//                                                }
//                                            } catch (ParseException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    }
//                                });
//                    }
//                });
//    }
//
//
//
//    private void scheduleNotification(String eventMessage, long triggerTimeMillis, int notificationId) {
//        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//
//        // Компонент для NotificationJobService
//        ComponentName componentName = new ComponentName(context, NotificationJobService.class);
//
//        // Передаем дополнительные данные
//        PersistableBundle bundle = new PersistableBundle();
//        bundle.putString("eventMessage", eventMessage);
//        bundle.putInt("notificationId", notificationId);  // Уникальный ID для уведомлений
//
//        // Планируем задачу
//        JobInfo jobInfo = new JobInfo.Builder(notificationId, componentName)
//                .setMinimumLatency(triggerTimeMillis - System.currentTimeMillis())  // Задержка до выполнения задачи
//                .setOverrideDeadline(triggerTimeMillis - System.currentTimeMillis())  // Максимальная задержка
//                .setExtras(bundle)  // Передаем данные (например, сообщение)
//                .build();
//
//        // Запускаем задачу
//        if (jobScheduler != null) {
//            jobScheduler.schedule(jobInfo);
//        } else {
//            Toast.makeText(context, "Ошибка при планировании уведомления", Toast.LENGTH_SHORT).show();
//        }
//    }




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