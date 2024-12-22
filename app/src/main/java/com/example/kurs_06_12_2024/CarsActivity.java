package com.example.kurs_06_12_2024;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CarsActivity extends AppCompatActivity implements CarsAdapter.OnCarClickListener {

    private ImageButton addCarButton;
    private Spinner brandSpinner, modelSpinner, yearSpinner, fuelTypeSpinner, colorSpinner;
    private EditText mileageEditText;
    private RecyclerView carsRecyclerView;
    private CarsAdapter carsAdapter;
    private List<Car> carList = new ArrayList<>();

    private HashMap<String, String[]> carModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);

        addCarButton = findViewById(R.id.addCarButton);
        carsRecyclerView = findViewById(R.id.carsRecyclerView);

        // Инициализация данных марок и моделей
        initCarData();

        // Настроим RecyclerView
        carsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        carsAdapter = new CarsAdapter(carList, this);  // Передаем обработчик кликов
        carsRecyclerView.setAdapter(carsAdapter);

        // Загрузка автомобилей из Firestore
        loadCarsFromFirestore();

        addCarButton.setOnClickListener(v -> showAddCarDialog());
    }

    private void initCarData() {
        carModels = new HashMap<>();

        String[] brands = getResources().getStringArray(R.array.car_brands);
        carModels.put("Alfa Romeo", getResources().getStringArray(R.array.alfaromeo_models));
        carModels.put("Audi", getResources().getStringArray(R.array.audi_models));
        carModels.put("BMW", getResources().getStringArray(R.array.bmw_models));
        carModels.put("Chevrolet", getResources().getStringArray(R.array.chevrolet_models));
        carModels.put("Chrysler", getResources().getStringArray(R.array.chrysler_models));
        carModels.put("Citroen", getResources().getStringArray(R.array.citroen_models));
        carModels.put("Dodge", getResources().getStringArray(R.array.dodge_models));
        carModels.put("Fiat", getResources().getStringArray(R.array.fiat_models));
        carModels.put("Ford", getResources().getStringArray(R.array.ford_models));
        carModels.put("Geely", getResources().getStringArray(R.array.geely_models));
        carModels.put("Honda", getResources().getStringArray(R.array.honda_models));
        carModels.put("Hyundai", getResources().getStringArray(R.array.hyundai_models));
        carModels.put("Infiniti", getResources().getStringArray(R.array.infiniti_models));
        carModels.put("Kia", getResources().getStringArray(R.array.kia_models));
        carModels.put("LADA", getResources().getStringArray(R.array.lada_models));
        carModels.put("Land Rover", getResources().getStringArray(R.array.landover_models));
        carModels.put("Lexus", getResources().getStringArray(R.array.lexus_models));
        carModels.put("Mazda", getResources().getStringArray(R.array.mazda_models));
        carModels.put("Mercedes-Benz", getResources().getStringArray(R.array.mercedes_models));
        carModels.put("Mitsubishi", getResources().getStringArray(R.array.mitsubishi_models));
        carModels.put("Nissan", getResources().getStringArray(R.array.nissan_models));
        carModels.put("Opel", getResources().getStringArray(R.array.opel_models));
        carModels.put("Peugeot", getResources().getStringArray(R.array.peugeot_models));
        carModels.put("Renault", getResources().getStringArray(R.array.renault_models));
        carModels.put("Skoda", getResources().getStringArray(R.array.skoda_models));
        carModels.put("Tesla", getResources().getStringArray(R.array.tesla_models));
        carModels.put("Toyota", getResources().getStringArray(R.array.toyota_models));
        carModels.put("Volkswagen", getResources().getStringArray(R.array.volkswagen_models));
        carModels.put("Volvo", getResources().getStringArray(R.array.volvo_models));
    }

    private int getLogoResId(String brand) {
        switch (brand) {
            case "Alfa Romeo": return R.drawable.alfaromeo_logo;
            case "Audi": return R.drawable.audi_logo;
            case "BMW": return R.drawable.bmw_logo;
            case "Chevrolet": return R.drawable.chevrolet_logo;
            case "Chrysler": return R.drawable.chrysler_logo;
            case "Citroen": return R.drawable.citroen_logo;
            case "Dodge": return R.drawable.dodge_logo;
            case "Fiat": return R.drawable.fiat_logo;
            case "Ford": return R.drawable.ford_logo;
            case "Geely": return R.drawable.geely_logo;
            case "Honda": return R.drawable.honda_logo;
            case "Hyundai": return R.drawable.hyundai_logo;
            case "Infiniti": return R.drawable.infiniti_logo;
            case "Kia": return R.drawable.kia_logo;
            case "Lada": return R.drawable.lada_logo;
            case "Land Rover": return R.drawable.landrover_logo;
            case "Lexus": return R.drawable.lexus_logo;
            case "Mazda": return R.drawable.mazda_logo;
            case "Mercedes-Benz": return R.drawable.mercedes_logo;
            case "Mitsubishi": return R.drawable.mitsubishi_logo;
            case "Nissan": return R.drawable.nissan_logo;
            case "Opel": return R.drawable.opel_logo;
            case "Peugeot": return R.drawable.peugeot_logo;
            case "Renault": return R.drawable.renault_logo;
            case "Skoda": return R.drawable.skoda_logo;
            case "Tesla": return R.drawable.tesla_logo;
            case "Toyota": return R.drawable.toyota_logo;
            case "Volkswagen": return R.drawable.volkswagen_logo;
            case "Volvo": return R.drawable.volvo_logo;
            default: return R.drawable.default_logo;
        }
    }

    private void showAddCarDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_car, null);

        brandSpinner = dialogView.findViewById(R.id.spinnerCarBrand);
        modelSpinner = dialogView.findViewById(R.id.spinnerCarModel);
        mileageEditText = dialogView.findViewById(R.id.editTextMileage);
        yearSpinner = dialogView.findViewById(R.id.spinnerYear);
        fuelTypeSpinner = dialogView.findViewById(R.id.spinnerFuelType);
        colorSpinner = dialogView.findViewById(R.id.spinnerCarColor);

        // Настройка адаптеров для марок, моделей, годов, типов топлива и цветов
        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.car_brands));
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brandSpinner.setAdapter(brandAdapter);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.car_years));
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        ArrayAdapter<String> fuelTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.fuel_types));
        fuelTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fuelTypeSpinner.setAdapter(fuelTypeAdapter);

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.car_colors));
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);

        brandSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parentView, View view, int position, long id) {
                updateModelsForSelectedBrand();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parentView) {
                // Пустая реализация
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Car")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String selectedBrand = (String) brandSpinner.getSelectedItem();
                    String selectedModel = (String) modelSpinner.getSelectedItem();
                    String mileage = mileageEditText.getText().toString();
                    String selectedYear = (String) yearSpinner.getSelectedItem();
                    String selectedFuelType = (String) fuelTypeSpinner.getSelectedItem();
                    String selectedColor = (String) colorSpinner.getSelectedItem();

                    if (selectedBrand == null || selectedModel == null || mileage.isEmpty() || selectedYear == null) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    saveCarToFirestore(selectedBrand, selectedModel, mileage, selectedYear, selectedFuelType, selectedColor);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        updateModelsForSelectedBrand();
    }

    private void updateModelsForSelectedBrand() {
        String selectedBrand = (String) brandSpinner.getSelectedItem();
        String[] models = carModels.get(selectedBrand);

        if (models != null) {
            ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, models);
            modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modelSpinner.setAdapter(modelAdapter);
        } else {
            modelSpinner.setAdapter(null);
        }
    }

    private void saveCarToFirestore(String brand, String model, String mileage, String year, String fuelType, String color) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference carsCollection = db.collection("users").document(userId).collection("cars");

        int logoResId = getLogoResId(brand);
        // Преобразуем пробег из строки в число
        int mileageValue = Integer.parseInt(mileage);

        // Инициализируем объект Car
        Car newCar = new Car(brand, model, mileageValue, year, fuelType, color, logoResId, mileageValue);

        // Добавляем новый автомобиль в коллекцию
        carsCollection.add(newCar)
                .addOnSuccessListener(documentReference -> {
                    String carId = documentReference.getId();
                    newCar.setId(carId);

                    // Теперь можно сохранить объект Car с полученным ID
                    carsCollection.document(carId).set(newCar)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Car added successfully!", Toast.LENGTH_SHORT).show();
                                loadCarsFromFirestore();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error adding car: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error adding car: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void loadCarsFromFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference carsCollection = db.collection("users").document(userId).collection("cars");

        carsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    carList.clear(); // Очищаем старые данные
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getString("id");
                        String brand = document.getString("brand");
                        String model = document.getString("model");

                        // Извлечение пробега с проверкой типа данных
                        Object mileageObject = document.get("mileage");  // Получаем значение как Object
                        int mileage = 0;  // Значение по умолчанию, если не удается получить корректное число

                        // Если пробег это число (Long или Double)
                        if (mileageObject instanceof Long) {
                            mileage = ((Long) mileageObject).intValue();
                        } else if (mileageObject instanceof Double) {
                            mileage = ((Double) mileageObject).intValue();
                        } else if (mileageObject instanceof String) {
                            // Если пробег хранится как строка, пытаемся преобразовать
                            try {
                                mileage = Integer.parseInt((String) mileageObject);
                            } catch (NumberFormatException e) {
                                // Логируем ошибку, если пробег не может быть преобразован
                                mileage = 0;
                            }
                        }

                        String year = document.getString("year");
                        String fuelType = document.getString("fuelType");
                        String color = document.getString("color");

                        int logoResId = getLogoResId(brand);  // Получаем логотип по марке
                        Car car = new Car(id, brand, model, mileage, year, fuelType, color, logoResId, mileage);

                        carList.add(car); // Добавляем в список
                    }
                    carsAdapter.notifyDataSetChanged();  // Обновляем адаптер, чтобы отобразить изменения
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading cars: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }





    @Override
    public void onCarClick(Car car) {
        // Инфлейтим разметку для диалога
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_car_info, null);

        // Находим все элементы внутри диалога
        ImageView logoImageView = dialogView.findViewById(R.id.carLogoInDialog);  // Логотип
        EditText mileageEditText = dialogView.findViewById(R.id.carMileage);
        TextView brandTextView = dialogView.findViewById(R.id.carBrand);
        TextView modelTextView = dialogView.findViewById(R.id.carModel);
        TextView yearTextView = dialogView.findViewById(R.id.carYear);
        TextView fuelTypeTextView = dialogView.findViewById(R.id.carFuelType);
        TextView colorTextView = dialogView.findViewById(R.id.carColor);
        Button saveMileageButton = dialogView.findViewById(R.id.saveMileageButton);

        // Добавление кнопки для действий с автомобилем
        ImageButton customButton = dialogView.findViewById(R.id.customButton);
        customButton.setOnClickListener(v -> showActionDialog(car)); // При клике на customButton показываем диалог действий

        // Устанавливаем данные для автомобиля
        brandTextView.setText(car.getBrand());
        modelTextView.setText(car.getModel());
        mileageEditText.setText(String.valueOf(car.getMileage()));
        yearTextView.setText(car.getYear());
        fuelTypeTextView.setText(car.getFuelType());
        colorTextView.setText(car.getColor());

        // Устанавливаем логотип автомобиля
        logoImageView.setImageResource(car.getLogoResId());

        // Разрешаем пользователю редактировать пробег сразу
        mileageEditText.setFocusable(true);  // Даем возможность редактировать
        mileageEditText.setFocusableInTouchMode(true);  // Обеспечиваем фокусировку на поле

        // Обработчик для изменения пробега
        mileageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Не требуется
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Если пробег изменился, включаем кнопку "Сохранить пробег"
                if (!charSequence.toString().equals(String.valueOf(car.getMileage()))) {
                    saveMileageButton.setEnabled(true);
                } else {
                    saveMileageButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Не требуется
            }
        });

        // Обработчик нажатия на кнопку "Сохранить пробег"
        saveMileageButton.setOnClickListener(v -> {
            String updatedMileage = mileageEditText.getText().toString();
            if (!updatedMileage.isEmpty() && !updatedMileage.equals(String.valueOf(car.getMileage()))) {
                car.setMileage(Integer.parseInt(updatedMileage));  // Обновляем пробег в объекте

                // Сохраняем обновленный пробег в Firestore
                saveUpdatedMileageToFirestore(car);

                // Отключаем кнопку после сохранения
                saveMileageButton.setEnabled(false);
            }
        });

        // Создаем и показываем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Car Information")
                .setView(dialogView) // Устанавливаем инфлейтую разметку
                .setPositiveButton("Close", null)
                .create()
                .show();
    }

    private void showActionDialog(Car car) {
        // Диалог для выбора действия
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Массив с действиями
        CharSequence[] actions = new CharSequence[]{
                "Дневной пробег",  // Добавляем новое действие
                "Удалить автомобиль"
        };

        builder.setTitle("Выберите действие")
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Дневной пробег
                            // При нажатии на "Дневной пробег", вызываем метод для изменения дневного пробега
                            showDailyMileageDialog(car);
                            break;
                        case 1: // Удалить автомобиль
                            showDeleteConfirmationDialog(car, dialog);  // Открываем диалог подтверждения удаления
                            break;
                    }
                })
                .create()
                .show();
    }


    private void showDeleteConfirmationDialog(Car car, DialogInterface actionDialog) {  // Параметр теперь DialogInterface
        // Подтверждение на удаление автомобиля
        new AlertDialog.Builder(this)
                .setTitle("Удалить автомобиль?")
                .setMessage("Вы уверены, что хотите удалить этот автомобиль?")
                .setPositiveButton("Да", (dialog, which) -> {
                    // Удаляем автомобиль
                    deleteCar(car);

                    // Закрываем диалог "Удалить автомобиль"
                    actionDialog.dismiss();  // Закрываем диалог с выбором действия
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void deleteCar(Car car) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference carsCollection = db.collection("users").document(userId).collection("cars");

        String carId = car.getId();
        if (carId != null) {
            // Удаляем автомобиль из базы данных
            carsCollection.document(carId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Находим позицию автомобиля в списке
                        int position = carList.indexOf(car);

                        // Удаляем автомобиль из списка
                        carList.remove(car);

                        // Уведомляем адаптер, что элемент был удален
                        carsAdapter.notifyItemRemoved(position); // Уведомляем адаптер об удалении конкретного элемента

                        Toast.makeText(CarsActivity.this, "Автомобиль удален успешно!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CarsActivity.this, "Ошибка при удалении автомобиля: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(CarsActivity.this, "Ошибка: ID автомобиля не найден.", Toast.LENGTH_SHORT).show();
        }
    }



    private void saveUpdatedMileageToFirestore(Car car) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference carsCollection = db.collection("users").document(userId).collection("cars");

        // Получаем ID машины и обновляем пробег
        String carId = car.getId();
        if (carId != null) {
            carsCollection.document(carId) // Используем правильный ID
                    .update("mileage", car.getMileage()) // Обновляем поле пробега
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CarsActivity.this, "Пробег обновлен успешно!", Toast.LENGTH_SHORT).show();
                        loadCarsFromFirestore(); // Перезагружаем данные после обновления
                    })
                    .addOnFailureListener(e -> Toast.makeText(CarsActivity.this, "Ошибка при обновлении пробега: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Обработайте случай, если ID машины отсутствует
            Toast.makeText(CarsActivity.this, "Ошибка: ID автомобиля не найден.", Toast.LENGTH_SHORT).show();
        }
    }


    // Дневной пробег и метод обновления
    private void showDailyMileageDialog(Car car) {
        // Инфлейт диалоговое окно для ввода дневного пробега
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_daily_mileage, null);
        EditText dailyMileageEditText = dialogView.findViewById(R.id.dailyMileageEditText);  // Поле для ввода километров

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите дневной пробег")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String dailyMileageStr = dailyMileageEditText.getText().toString();
                    if (!dailyMileageStr.isEmpty()) {
                        int dailyMileage = Integer.parseInt(dailyMileageStr);
                        updateCarDailyMileage(car, dailyMileage);  // Сохранить дневной пробег в Firestore
                    } else {
                        Toast.makeText(this, "Пожалуйста, введите значение", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null);

        builder.create().show();
    }

    private void updateCarDailyMileage(Car car, int newDailyMileage) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference carsCollection = db.collection("users").document(userId).collection("cars");

        String carId = car.getId();
        if (carId != null) {
            // Получаем текущие данные о пробеге
            carsCollection.document(carId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Получаем текущие значения
                            Long currentEstimatedMileage = documentSnapshot.getLong("estimatedMileage");
                            Long currentDailyMileage = documentSnapshot.getLong("dailyMileage");

                            if (currentEstimatedMileage == null) {
                                currentEstimatedMileage = 0L; // Если предполагаемый пробег не задан
                            }
                            if (currentDailyMileage == null) {
                                currentDailyMileage = 0L; // Если дневной пробег не задан
                            }

                            // Если дневной пробег изменился, то вычисляем разницу и обновляем данные
                            long difference = newDailyMileage - currentDailyMileage;

                            // Если разница положительная, то добавляем её к предполагаемому пробегу
                            if (difference != 0) {
                                carsCollection.document(carId)
                                        .update("dailyMileage", newDailyMileage, // Обновляем дневной пробег
                                                "estimatedMileage", FieldValue.increment(difference)) // Добавляем разницу к предполагаемому пробегу
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(CarsActivity.this, "Дневной пробег обновлен!", Toast.LENGTH_SHORT).show();
                                            loadCarsFromFirestore();  // Перезагружаем данные с учетом изменений
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(CarsActivity.this, "Ошибка при обновлении данных: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            } else {
                                // Если разница равна 0, то просто обновляем предполагаемый пробег на новый дневной пробег
                                carsCollection.document(carId)
                                        .update("dailyMileage", newDailyMileage, // Обновляем дневной пробег
                                                "estimatedMileage", FieldValue.increment(newDailyMileage)) // Добавляем новый дневной пробег к предполагаемому
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(CarsActivity.this, "Дневной пробег обновлен!", Toast.LENGTH_SHORT).show();
                                            loadCarsFromFirestore();  // Перезагружаем данные с учетом изменений
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(CarsActivity.this, "Ошибка при обновлении данных: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(CarsActivity.this, "Ошибка при получении данных: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(CarsActivity.this, "Ошибка: ID автомобиля не найден.", Toast.LENGTH_SHORT).show();
        }
    }


}