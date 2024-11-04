package com.example.farm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomePageActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private LocationManager locationManager;
    private TextView currentLocationTextView;
    private TextView nearestTownTextView;
    private TextView bulkDensityTextView;
    private TextView cropsview;
    private PopupWindow popupWindow;
    private Button soil_button;
    private MyDatabaseHelper dbHelper;
    private View popupView;
    private double currentLatitude;
    private double currentLongitude;
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentLocationTextView = findViewById(R.id.current_location_text_view);
        nearestTownTextView = findViewById(R.id.nearest_town_text_view);
        bulkDensityTextView = findViewById(R.id.bulkDensityTextView);
        cropsview = findViewById(R.id.crops);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_layout, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setElevation(8.0f);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            getLocation();  // Get location and weather data
        }

        dbHelper = new MyDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();  // Opening database

        soil_button = findViewById(R.id.soil_button);
        soil_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
        });

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
    }

    // Inflate the toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);  // Ensure menu_main includes all items
        return true;
    }

    // Handle toolbar menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Open Weather Forecast Activity
        if (id == R.id.action_weather_forecast) {
            Intent intent = new Intent(this, WeatherForecastActivity.class);
            intent.putExtra("latitude", getCurrentLatitude());
            intent.putExtra("longitude", getCurrentLongitude());
            startActivity(intent);
            return true;
        }

        // Logout Action
        if (id == R.id.action_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to logout?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Call logout() from LoginActivity
                            LoginActivity.logout(sharedPreferences, HomePageActivity.this);  // Logout and clear session
                        }
                    })
                    .setNegativeButton("Cancel", null);
            builder.create().show();
            return true;
        }


        // Open Trees Activity
        if (id == R.id.action_trees) {
            Intent intent = new Intent(this, TreesActivity.class);
            startActivity(intent);
            return true;
        }

        // Open Regions Activity
        if (id == R.id.action_regions) {
            Intent intent = new Intent(this, RegionsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Method to logout the user and clear session
    private void logoutUser() {
        // Clear session data in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Redirect to LoginActivity
        Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();  // Close the current activity to prevent the user from going back
    }

    // Method to get device location
    private void getLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            currentLocationTextView.setText("Lat: " + currentLatitude + " Long: " + currentLongitude);
                            getNearestTown(currentLatitude, currentLongitude); // Fetch nearest town
                            getSoilProperties(currentLatitude, currentLongitude); // Fetch soil properties
                        } else {
                            Toast.makeText(HomePageActivity.this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private double getCurrentLatitude() {
        return currentLatitude;
    }

    private double getCurrentLongitude() {
        return currentLongitude;
    }
    private void getNearestTown(double latitude, double longitude) {
        String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject address = response.getJSONObject("address");
                    String location = "Unknown location";

                    // Check for various location types in order of specificity
                    String[] locationTypes = {"city", "town", "village", "hamlet", "suburb", "neighbourhood", "county", "state", "country"};

                    for (String type : locationTypes) {
                        if (address.has(type)) {
                            location = address.getString(type);
                            break;
                        }
                    }

                    // If no match found, use display_name as a fallback
                    if (location.equals("Unknown location") && response.has("display_name")) {
                        location = response.getString("display_name");
                    }

                    nearestTownTextView.setText("Current Location: " + location);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(HomePageActivity.this, "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(HomePageActivity.this, "Error retrieving current location", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    // Method to get soil properties using coordinates
    private void getSoilProperties(double latitude, double longitude) {
        String url = "https://rest.isric.org/soilgrids/v2.0/classification/query?lon=" + longitude + "&lat=" + latitude + "&format=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String wrbClassName = response.getString("wrb_class_name");
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    Cursor cursor = db.query("agroforestry", new String[]{"soil_properties", "crops", "trees"}, "soil_name = ?", new String[]{wrbClassName}, null, null, null);
                    if (cursor.moveToFirst()) {
                        String soilProperties = cursor.getString(cursor.getColumnIndexOrThrow("soil_properties"));
                        String crops = cursor.getString(cursor.getColumnIndexOrThrow("crops"));
                        bulkDensityTextView.setText(soilProperties);
                        cropsview.setText(crops);
                    }
                    cursor.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(HomePageActivity.this, "Error fetching soil properties", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(HomePageActivity.this, "Unable to retrieve soil properties", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}
