package com.myapp.placevisitedapp;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class PlacesListActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView listView;
    private ArrayAdapter<Place> adapter;
    private List<Place> placesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);

        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.listView);

        // Add Reset Database button
        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> showResetDatabaseDialog());

        loadPlaces();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Place selectedPlace = placesList.get(position);
            showPlaceOptionsDialog(selectedPlace);
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId()==R.id.mn_add) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.mn_history) {
            Intent intent = new Intent(this, PlacesListActivity.class);
            startActivity(intent);
        }
        return true;
    }
    private void loadPlaces() {
        placesList = dbHelper.getAllPlaces();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, placesList);
        listView.setAdapter(adapter);
    }

    private void showPlaceOptionsDialog(Place place) {
        String[] options = {"Edit Title", "Share Location", "Delete", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(place.getTitle())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit Title
                            showEditTitleDialog(place);
                            break;
                        case 1: // Share Location
                            shareLocation(place);
                            break;
                        case 2: // Delete
                            deletePlace(place);
                            break;
                        case 3: // Cancel
                            dialog.dismiss();
                            break;
                    }
                });
        builder.show();
    }

    private void showEditTitleDialog(Place place) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Place Name");

        final EditText input = new EditText(this);
        input.setText(place.getTitle());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                dbHelper.updatePlaceTitle(place.getId(), newTitle);
                loadPlaces();
                Toast.makeText(this, "Title updated", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void shareLocation(Place place) {
        String googleMapsUrl = String.format(Locale.US,
                "https://www.google.com/maps/search/?api=1&query=%f,%f",
                place.getLatitude(), place.getLongitude());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, place.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Check out " + place.getTitle() + " visited on :"+place.getDate() +" at: " + googleMapsUrl);

        startActivity(Intent.createChooser(shareIntent, "Share location via"));
    }

    private void deletePlace(Place place) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Place")
                .setMessage("Are you sure you want to delete " + place.getTitle() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dbHelper.deletePlace(place.getId());
                    loadPlaces();
                    Toast.makeText(this, "Place deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showResetDatabaseDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Database")
                .setMessage("Are you sure you want to delete all places? This cannot be undone.")
                .setPositiveButton("Reset", (dialog, which) -> {
                    dbHelper.resetDatabase();
                    loadPlaces();
                    Toast.makeText(this, "Database reset", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}