package com.example.kurs_06_12_2024;

public class CarEvent {
    private String calendarName;
    private String carBrand;
    private String carModel;
    private String eventDate; // Изменяем тип на String
    private String eventDescription;

    // Конструктор с параметрами
    public CarEvent(String calendarName, String carBrand, String carModel, String eventDate, String eventDescription) {
        this.calendarName = calendarName;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.eventDate = eventDate;
        this.eventDescription = eventDescription;
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

    @Override
    public String toString() {
        return "CarEvent{" +
                "calendarName='" + calendarName + '\'' +
                ", carBrand='" + carBrand + '\'' +
                ", carModel='" + carModel + '\'' +
                ", eventDate='" + eventDate + '\'' +
                ", eventDescription='" + eventDescription + '\'' +
                '}';
    }
}
