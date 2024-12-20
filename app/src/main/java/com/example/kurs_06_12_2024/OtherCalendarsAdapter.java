package com.example.kurs_06_12_2024;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.List;

public class OtherCalendarsAdapter extends RecyclerView.Adapter<OtherCalendarsAdapter.CalendarViewHolder> {

    private List<MyCalendar> calendarList;
    private Context context;

    public OtherCalendarsAdapter(Context context, List<MyCalendar> calendarList) {
        this.context = context;
        this.calendarList = calendarList;
    }

    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CalendarViewHolder holder, int position) {
        MyCalendar calendar = calendarList.get(position);
        holder.calendarNameTextView.setText(calendar.getCalendarName());
        holder.carNameTextView.setText(calendar.getCarBrand() + " " + calendar.getCarModel());

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;

                // Создаем MaterialCalendarView и передаем его в метод loadEventsForCalendar
                MaterialCalendarView materialCalendarView = new MaterialCalendarView(context);

                // Вызываем метод loadEventsForCalendar
                ((OtherCalendarsActivity) activity).loadEventsForCalendar(calendar, materialCalendarView);

                // Показываем календарный диалог или другое представление
                CalendarDialogFragment dialogFragment = new CalendarDialogFragment(calendar);
                dialogFragment.show(activity.getSupportFragmentManager(), "CalendarDialog");
            }
        });
    }

    @Override
    public int getItemCount() {
        return calendarList.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView calendarNameTextView;
        TextView carNameTextView;

        public CalendarViewHolder(View itemView) {
            super(itemView);
            calendarNameTextView = itemView.findViewById(R.id.calendarNameTextView);
            carNameTextView = itemView.findViewById(R.id.carNameTextView);
        }
    }
}
