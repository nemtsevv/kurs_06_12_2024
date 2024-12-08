package com.example.kurs_06_12_2024;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CarsActivity extends AppCompatActivity {

    private ImageButton addCarButton;
    private Spinner brandSpinner, modelSpinner;
    private EditText mileageEditText;
    private RecyclerView carsRecyclerView;
    private CarsAdapter carsAdapter;
    private List<Car> carList = new ArrayList<>();

    // Список марок и моделей авто
    private HashMap<String, String[]> carModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);

        addCarButton = findViewById(R.id.addCarButton);
        carsRecyclerView = findViewById(R.id.carsRecyclerView);

        // Инициализируем данные для марок и моделей авто
        initCarData();

        // Настроить RecyclerView
        carsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        carsAdapter = new CarsAdapter(carList);
        carsRecyclerView.setAdapter(carsAdapter);

        // Загрузка автомобилей из Firestore
        loadCarsFromFirestore();

        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываем диалог для добавления авто
                showAddCarDialog();
            }
        });
    }

    private void initCarData() {
        // Инициализация списка марок и моделей из arrays.xml
        carModels = new HashMap<>();

        // Получаем массивы марок и моделей из ресурсов
        String[] brands = getResources().getStringArray(R.array.car_brands);

        // Для каждой марки добавляем соответствующие модели
        carModels.put("Ford", getResources().getStringArray(R.array.ford_models));
        carModels.put("Toyota", getResources().getStringArray(R.array.toyota_models));
        carModels.put("BMW", getResources().getStringArray(R.array.bmw_models));
    }

    private void showAddCarDialog() {
        // Создаем диалог с кастомным layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_car, null);

        // Привязываем элементы из диалога
        brandSpinner = dialogView.findViewById(R.id.spinnerCarBrand);
        modelSpinner = dialogView.findViewById(R.id.spinnerCarModel);
        mileageEditText = dialogView.findViewById(R.id.editTextMileage);

        // Логируем элементы для проверки
        Log.d("CarsActivity", "brandSpinner: " + brandSpinner);
        Log.d("CarsActivity", "modelSpinner: " + modelSpinner);
        Log.d("CarsActivity", "mileageEditText: " + mileageEditText);

        // Если спиннеры или другие элементы равны null, это значит, что они не были правильно найдены
        if (brandSpinner == null || modelSpinner == null || mileageEditText == null) {
            Log.e("CarsActivity", "One or more dialog elements are not properly initialized!");
            return; // Возвращаемся, если элементы не инициализированы
        }

        // Адаптер для марок автомобилей
        String[] brands = getResources().getStringArray(R.array.car_brands);
        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, brands);
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brandSpinner.setAdapter(brandAdapter);

        // Устанавливаем обработчик для изменения модели при выборе марки
        brandSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parentView, View view, int position, long id) {
                updateModelsForSelectedBrand();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parentView) {
                // Логируем, если ничего не выбрано
                Log.d("CarsActivity", "No brand selected");
            }
        });

        // Создаем и показываем диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(CarsActivity.this);
        builder.setTitle("Add Car")
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Логируем состояние перед попыткой получить данные
                        Log.d("CarsActivity", "brandSpinner.getSelectedItem(): " + brandSpinner.getSelectedItem());
                        Log.d("CarsActivity", "modelSpinner.getSelectedItem(): " + modelSpinner.getSelectedItem());

                        String selectedBrand = (String) brandSpinner.getSelectedItem();
                        String selectedModel = (String) modelSpinner.getSelectedItem();
                        String mileage = mileageEditText.getText().toString();

                        // Проверка на null или пустое значение
                        if (selectedBrand == null || selectedModel == null || mileage.isEmpty()) {
                            Toast.makeText(CarsActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Сохранение данных автомобиля в Firestore
                        saveCarToFirestore(selectedBrand, selectedModel, mileage);
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Обновляем модели после создания диалога
        updateModelsForSelectedBrand();
    }

    private void updateModelsForSelectedBrand() {
        // Получаем выбранную марку
        String selectedBrand = (String) brandSpinner.getSelectedItem();

        // Проверяем, есть ли модели для выбранной марки
        String[] models = carModels.get(selectedBrand);

        if (models != null) {
            // Создаем и устанавливаем адаптер для моделей
            ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, models);
            modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modelSpinner.setAdapter(modelAdapter);
        } else {
            // Если моделей нет для выбранной марки, показываем пустой адаптер
            modelSpinner.setAdapter(null);
        }
    }

    // Метод для сохранения автомобиля в Firestore
    private void saveCarToFirestore(String brand, String model, String mileage) {
        // Получаем UID текущего пользователя
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Ссылка на коллекцию автомобилей пользователя
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference carsCollection = db.collection("users").document(userId).collection("cars");

        // Определяем логотип автомобиля
        int logoResId = getLogoResId(brand);

        // Создаем объект автомобиля
        Car newCar = new Car(brand, model, mileage, logoResId);

        // Сохраняем данные в коллекцию
        carsCollection.add(newCar)
                .addOnSuccessListener(documentReference -> {
                    // Успешно добавлено
                    Toast.makeText(CarsActivity.this, "Car added successfully!", Toast.LENGTH_SHORT).show();
                    loadCarsFromFirestore();  // Перезагружаем список автомобилей
                })
                .addOnFailureListener(e -> {
                    // Ошибка при добавлении
                    Toast.makeText(CarsActivity.this, "Error adding car: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Загрузка автомобилей из Firestore
    private void loadCarsFromFirestore() {
        // Получаем UID текущего пользователя
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference carsCollection = db.collection("users").document(userId).collection("cars");

        carsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    carList.clear(); // Очистить текущий список
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String brand = document.getString("brand");
                        String model = document.getString("model");
                        String mileage = document.getString("mileage");

                        // Определяем логотип в зависимости от марки автомобиля
                        int logoResId = getLogoResId(brand);

                        // Создаем объект автомобиля
                        Car car = new Car(brand, model, mileage, logoResId);
                        carList.add(car);
                    }
                    carsAdapter.notifyDataSetChanged(); // Обновить адаптер
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CarsActivity.this, "Error loading cars: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Метод для получения ресурса логотипа по марке
    private int getLogoResId(String brand) {
        switch (brand) {
            case "Ford":
                return R.drawable.ford_logo;
            case "Toyota":
                return R.drawable.toyota_logo;
            case "BMW":
                return R.drawable.bmw_logo;
            default:
                return R.drawable.default_logo;
        }
    }
}
