package com.myapp.placevisitedapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {
    private List<Place> placesList;

    public PlacesAdapter(List<Place> placesList) {
        this.placesList = placesList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placesList.get(position);
        holder.text1.setText(place.getTitle());
        holder.text2.setText(String.format(Locale.getDefault(),
                "Lat: %.6f, Long: %.6f\nDate: %s",
                place.getLatitude(), place.getLongitude(), place.getDate()));
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView text1;
        TextView text2;

        PlaceViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}