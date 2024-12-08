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

    public CarsAdapter(List<Car> carList) {
        this.carList = carList;
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
        holder.mileageTextView.setText(car.getMileage());

        // Устанавливаем изображение логотипа в зависимости от марки
        holder.logoImageView.setImageResource(car.getLogoResId());

        // Дополнительные стили для выделения элемента (например, изменение шрифта, цвета и т.д.)
        holder.itemView.setBackgroundResource(R.drawable.item_car_background); // Фон с закругленными углами
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
}
