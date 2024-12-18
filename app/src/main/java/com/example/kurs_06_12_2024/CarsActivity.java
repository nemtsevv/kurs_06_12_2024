package com.example.kurs_06_12_2024;

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

        carModels.put("Ford", getResources().getStringArray(R.array.ford_models));
        carModels.put("Toyota", getResources().getStringArray(R.array.toyota_models));
        carModels.put("BMW", getResources().getStringArray(R.array.bmw_models));
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
        Car newCar = new Car(brand, model, mileage, year, fuelType, color, logoResId);

        // Добавляем автомобиль в коллекцию и получаем ID документа
        carsCollection.add(newCar)
                .addOnSuccessListener(documentReference -> {
                    // Получаем ID сгенерированного документа
                    String carId = documentReference.getId();

                    // Обновляем объект Car с полученным ID
                    newCar.setId(carId);

                    // Теперь можно сохранить обновленный объект с ID, если нужно обновить данные
                    // Например, обновить информацию о автомобиле в Firestore
                    carsCollection.document(carId).set(newCar) // Можно использовать set вместо add, если нужно гарантировать ID
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
                        String mileage = document.getString("mileage");
                        String year = document.getString("year");
                        String fuelType = document.getString("fuelType");
                        String color = document.getString("color");

                        int logoResId = getLogoResId(brand);  // Получаем логотип по марке
                        Car car = new Car(id, brand, model, mileage, year, fuelType, color, logoResId);
                        carList.add(car); // Добавляем в список
                    }
                    carsAdapter.notifyDataSetChanged();  // Обновляем адаптер, чтобы отобразить изменения
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading cars: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private int getLogoResId(String brand) {
        switch (brand) {
            case "Ford": return R.drawable.ford_logo;
            case "Toyota": return R.drawable.toyota_logo;
            case "BMW": return R.drawable.bmw_logo;
            default: return R.drawable.default_logo;
        }
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
        mileageEditText.setText(car.getMileage());
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
                if (!charSequence.toString().equals(car.getMileage())) {
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
            if (!updatedMileage.isEmpty() && !updatedMileage.equals(car.getMileage())) {
                car.setMileage(updatedMileage);  // Обновляем пробег в объекте

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
        builder.setTitle("Select Action")
                .setItems(new CharSequence[]{"Action 1", "Action 2", "Action 3"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(this, "Action 1 for " + car.getModel(), Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(this, "Action 2 for " + car.getModel(), Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(this, "Action 3 for " + car.getModel(), Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .create()
                .show();
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
}
