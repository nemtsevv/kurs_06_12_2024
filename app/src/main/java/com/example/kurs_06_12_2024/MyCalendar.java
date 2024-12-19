package com.example.kurs_06_12_2024;

public class MyCalendar {
    private String calendarId;  // Уникальный идентификатор календаря
    private String calendarName;
    private String carBrand;
    private String carModel;

    // Пустой конструктор для Firestore
    public MyCalendar() {
    }

    // Конструктор с тремя параметрами: название календаря, марка и модель авто
    public MyCalendar(String calendarId, String calendarName, String carBrand, String carModel) {
        this.calendarId = calendarId;
        this.calendarName = calendarName;
        this.carBrand = carBrand;
        this.carModel = carModel;
    }

    // Геттер и сеттер для calendarId
    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }
}
