package com.example.kurs_06_12_2024;

public class Car {
    private String id;
    private String brand;
    private String model;
    private int mileage;  // Текущий пробег автомобиля (в целочисленном формате)
    private String year;
    private String fuelType;
    private String color;
    private int logoResId;
    private int estimatedMileage;  // Новое поле для хранения предполагаемого пробега

    // Конструктор, который принимает все необходимые параметры
    public Car(String brand, String model, int mileage, String year, String fuelType, String color, int logoResId, int estimatedMileage) {
        this.brand = brand;
        this.model = model;
        this.mileage = mileage;
        this.year = year;
        this.fuelType = fuelType;
        this.color = color;
        this.logoResId = logoResId;
        this.estimatedMileage = mileage;  // Устанавливаем начальное значение estimatedMileage равным текущему пробегу
    }

    // Конструктор с ID, который может использоваться для получения данных из Firestore
    public Car(String id, String brand, String model, int mileage, String year, String fuelType, String color, int logoResId, int estimatedMileage) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.mileage = mileage;
        this.year = year;
        this.fuelType = fuelType;
        this.color = color;
        this.logoResId = logoResId;
        this.estimatedMileage = estimatedMileage;  // Инициализируем предполагаемый пробег
    }

    // Геттеры и сеттеры для всех полей
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getLogoResId() {
        return logoResId;
    }

    public void setLogoResId(int logoResId) {
        this.logoResId = logoResId;
    }

    // Новый геттер и сеттер для estimatedMileage
    public int getEstimatedMileage() {
        return estimatedMileage;
    }

    public void setEstimatedMileage(int estimatedMileage) {
        this.estimatedMileage = estimatedMileage;
    }
}
