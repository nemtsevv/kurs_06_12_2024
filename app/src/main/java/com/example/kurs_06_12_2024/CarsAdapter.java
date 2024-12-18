package com.example.kurs_06_12_2024;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarsAdapter extends RecyclerView.Adapter<CarsAdapter.CarViewHolder> {

    private List<Car> carList;
    private OnCarClickListener onCarClickListener;

    public CarsAdapter(List<Car> carList, OnCarClickListener onCarClickListener) {
        this.carList = carList;
        this.onCarClickListener = onCarClickListener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Инфлейтим новый элемент списка с фоном и тенью
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);

        // Устанавливаем данные в элементы
        holder.brandTextView.setText(car.getBrand());
        holder.modelTextView.setText(car.getModel());

        // Преобразуем пробег в строку, если это нужно (например, если mileage — это int или double)
        holder.mileageTextView.setText(String.valueOf(car.getMileage()));  // Преобразуем в строку

        // Устанавливаем изображение логотипа в зависимости от марки
        holder.logoImageView.setImageResource(car.getLogoResId());

        // Дополнительные стили для выделения элемента (например, изменение фона)
        holder.itemView.setBackgroundResource(R.drawable.item_car_background); // Фон с закругленными углами

        // Обработка клика по элементу
        holder.itemView.setOnClickListener(v -> onCarClickListener.onCarClick(car));
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView brandTextView;
        TextView modelTextView;
        TextView mileageTextView;
        ImageView logoImageView;

        public CarViewHolder(View itemView) {
            super(itemView);
            brandTextView = itemView.findViewById(R.id.carBrand);
            modelTextView = itemView.findViewById(R.id.carModel);
            mileageTextView = itemView.findViewById(R.id.carMileage);
            logoImageView = itemView.findViewById(R.id.carLogo);
        }
    }

    // Интерфейс для обработки клика на автомобиле
    public interface OnCarClickListener {
        void onCarClick(Car car);
    }
}
