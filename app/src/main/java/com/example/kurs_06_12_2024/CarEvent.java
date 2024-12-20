package com.example.kurs_06_12_2024;


public class CarEvent {
    private String calendarName;
    private String carBrand;
    private String carModel;
    private String eventDate; // Дата события
    private String eventDescription;
    private double eventCost; // Стоимость события
    private double eventMileage; // Пробег в километрах

    // Конструктор с параметрами, добавлен параметр для пробега
    public CarEvent(String calendarName, String carBrand, String carModel, String eventDate, String eventDescription, double eventCost, double eventMileage) {
        this.calendarName = calendarName;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.eventDate = eventDate;
        this.eventDescription = eventDescription;
        this.eventCost = eventCost; // Инициализация стоимости
        this.eventMileage = eventMileage; // Инициализация пробега
    }
    public CarEvent() {
        // Firebase требует наличие конструктора без параметров
    }
    // Геттеры и сеттеры
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

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public double getEventCost() {
        return eventCost;
    }

    public void setEventCost(double eventCost) {
        this.eventCost = eventCost;
    }

    public double getEventMileage() {
        return eventMileage;
    }

    public void setEventMileage(double eventMileage) {
        this.eventMileage = eventMileage;
    }

    @Override
    public String toString() {
        return "CarEvent{" +
                "calendarName='" + calendarName + '\'' +
                ", carBrand='" + carBrand + '\'' +
                ", carModel='" + carModel + '\'' +
                ", eventDate='" + eventDate + '\'' +
                ", eventDescription='" + eventDescription + '\'' +
                ", eventCost=" + eventCost +
                ", eventMileage=" + eventMileage + // Добавили пробег в вывод
                '}';
    }
}
