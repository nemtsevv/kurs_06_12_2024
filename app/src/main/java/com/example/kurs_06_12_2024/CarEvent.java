package com.example.kurs_06_12_2024;


    // Конструктор
    public class CarEvent {
        private String calendarName;
        private String carBrand;
        private String carModel;
        private int year;
        private int month;
        private int dayOfMonth;
        private String eventDescription;

        // Конструктор
        public CarEvent(String calendarName, String carBrand, String carModel,
                        int year, int month, int dayOfMonth, String eventDescription) {
            this.calendarName = calendarName;
            this.carBrand = carBrand;
            this.carModel = carModel;
            this.year = year;
            this.month = month;
            this.dayOfMonth = dayOfMonth;
            this.eventDescription = eventDescription;
        }

        // Геттеры и сеттеры для всех полей
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

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getDayOfMonth() {
            return dayOfMonth;
        }

        public void setDayOfMonth(int dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
        }

        public String getEventDescription() {
            return eventDescription;
        }

        public void setEventDescription(String eventDescription) {
            this.eventDescription = eventDescription;
        }
    }
