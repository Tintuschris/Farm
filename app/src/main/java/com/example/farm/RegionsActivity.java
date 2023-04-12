package com.example.farm;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RegionsActivity extends AppCompatActivity {
    private MyDatabaseHelper dbHelper;
    private TextView soil_properties_view;
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acivity_regions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new MyDatabaseHelper(this);
        soil_properties_view = findViewById(R.id.soil_properties_view);


        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedRegion = adapterView.getItemAtPosition(i).toString();
                // Use the selectedRegion value to query the database and update the UI
                Cursor cursor = db.rawQuery("SELECT * FROM regions WHERE name LIKE ?", new String[]{"%" + selectedRegion + "%"});
                if (cursor.moveToFirst()) {
                    StringBuilder builder = new StringBuilder();
                    int idColumnIndex = cursor.getColumnIndex("id");
                    int cropsColumnIndex = cursor.getColumnIndex("crops");
                    int soilPropertiesColumnIndex = cursor.getColumnIndex("soil_properties");
                    int soilTypeColumnIndex = cursor.getColumnIndex("soil_type");

                    do {
                        int id = cursor.getInt(idColumnIndex);
                        String crops = cursor.getString(cropsColumnIndex);
                        String soilProperties = cursor.getString(soilPropertiesColumnIndex);
                        String soilType = cursor.getString(soilTypeColumnIndex);

                        builder.append("Region: ").append(selectedRegion).append("\n")
                                .append("\n")
                                .append("Crops: ").append(crops).append("\n")
                                .append("\n")
                                .append("Soil Properties: ").append(soilProperties).append("\n")
                                .append("\n")
                                .append("Soil Type: ").append(soilType).append("\n")
                                .append("\n");
                    } while (cursor.moveToNext());

                    soil_properties_view.setText(builder.toString());
                } else {
                    Toast.makeText(RegionsActivity.this, "No records retrieved", Toast.LENGTH_SHORT).show();
                }

                cursor.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case when no item is selected
                soil_properties_view.setText("Select a Region to view soil properties");
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sec, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.back_button) {
            // Open HomepageActivity
            Intent intent = new Intent(this, HomePageActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
