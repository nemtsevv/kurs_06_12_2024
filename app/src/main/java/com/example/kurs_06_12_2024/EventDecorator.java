package com.example.kurs_06_12_2024;

import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import android.graphics.drawable.Drawable;

public class EventDecorator implements DayViewDecorator {

    private final CalendarDay day;
    private final Drawable drawable;

    public EventDecorator(CalendarDay day, Drawable drawable) {
        this.day = day;
        this.drawable = drawable;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return this.day.equals(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(drawable);  // Устанавливаем фоновое изображение для даты
    }
}