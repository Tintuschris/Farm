package com.example.farm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

public class HomePageActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private LocationManager locationManager;
    private TextView currentLocationTextView;
    private TextView nearestTownTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        // Enable the app bar
        ActionBar actionBar = getSupportActionBar();
       // if (actionBar != null) {

            //actionBar.setHomeAsUpIndicator(R.drawable.ic_settings);
          //  Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_settings);
           // drawable.setBounds(4, 4, 4, 4);
        //    actionBar.setHomeAsUpIndicator(drawable);
        //    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO);
        //    actionBar.setDisplayHomeAsUpEnabled(true);
        //    actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.main_color)));
       // }
        currentLocationTextView = findViewById(R.id.current_location_text_view);
        nearestTownTextView = findViewById(R.id.nearest_town_text_view);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                           // currentLocationTextView.setTextColor(Color.BLACK);
                            currentLocationTextView.setText("Lat: " + latitude + ", Long: " + longitude);

                            getNearestTown(latitude, longitude);
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

    private void getNearestTown(double latitude, double longitude) {
        String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + latitude + "&lon=" + longitude;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject address = response.getJSONObject("address");
                            String location = "";

                            // Check for available address properties in order of preference
                            if (address.has("town")) {
                                location = address.getString("town");
                            } else if (address.has("center")) {
                                location = address.getString("center");
                            } else if (address.has("city")) {
                                location = address.getString("city");
                            } else if (address.has("state")) {
                                location = address.getString("state");
                            }

                            Log.d("NearestLocation", "Nearest location: " + location);
                            //nearestTownTextView.setTextColor(Color.WHITE);
                            nearestTownTextView.setText("Location: " + location);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(HomePageActivity.this, "Unable to retrieve nearest location", Toast.LENGTH_SHORT).show();
                            Log.e("NearestLocation", "Error retrieving nearest location", e);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(HomePageActivity.this, "Unable to retrieve nearest location", Toast.LENGTH_SHORT).show();
                        Log.e("NearestLocation", "Error retrieving nearest location", error);
                    }
                });

        // Set the retry policy
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                3000, // initial timeout of 3 seconds
                3, // maximum number of retries
                2 // backoff multiplier
        ));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

}


