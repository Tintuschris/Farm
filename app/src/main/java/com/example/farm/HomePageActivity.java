package com.example.farm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
    private TextView weatherTextView;
    private PopupWindow popupWindow;
    private Button soil_button;
    private MyDatabaseHelper dbHelper;
    private View popupView;
    private double currentLatitude;
    private double currentLongitude;
    private static final String API_KEY = "c97a0f9ebbf89a9bdb7c569fc5f2fefd";  // OpenWeatherMap API key

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
        weatherTextView = findViewById(R.id.forecast_text_view);

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
    }

    // Inflate the toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handle toolbar menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_weather_forecast) {
            // Navigate to the WeatherForecastActivity, passing the latitude and longitude
            Intent intent = new Intent(this, WeatherForecastActivity.class);
            intent.putExtra("latitude", getCurrentLatitude());
            intent.putExtra("longitude", getCurrentLongitude());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                            getWeatherData(currentLatitude, currentLongitude); // Fetch weather data
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

    // Method to get weather data using OpenWeatherMap API
    private void getWeatherData(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main = response.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    int humidity = main.getInt("humidity");

                    JSONArray weatherArray = response.getJSONArray("weather");
                    JSONObject weather = weatherArray.getJSONObject(0);
                    String weatherDescription = weather.getString("description");

                    String weatherInfo = "Temperature: " + temperature + "°C\n"
                            + "Humidity: " + humidity + "%\n"
                            + "Description: " + weatherDescription;
                    weatherTextView.setText(weatherInfo);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(HomePageActivity.this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(HomePageActivity.this, "Error retrieving weather data", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    // Method to get nearest town using coordinates
    private void getNearestTown(double latitude, double longitude) {
        String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject address = response.getJSONObject("address");
                    String location = address.has("town") ? address.getString("town") : "Unknown location";
                    nearestTownTextView.setText("Nearest Town: " + location);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(HomePageActivity.this, "Unable to retrieve nearest location", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(HomePageActivity.this, "Error retrieving nearest location", Toast.LENGTH_SHORT).show();
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
