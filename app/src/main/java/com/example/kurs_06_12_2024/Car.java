package com.example.kurs_06_12_2024;

public class Car {
    private String brand;
    private String model;
    private String mileage;
    private int logoResId;  // Ресурсное ID для логотипа

    // Конструктор без аргументов, необходим для Firestore
    public Car() {
        // Пустой конструктор
    }

    // Конструктор с аргументами
    public Car(String brand, String model, String mileage, int logoResId) {
        this.brand = brand;
        this.model = model;
        this.mileage = mileage;
        this.logoResId = logoResId;
    }

    // Геттеры и сеттеры
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

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public int getLogoResId() {
        return logoResId;
    }

    public void setLogoResId(int logoResId) {
        this.logoResId = logoResId;
    }
}
