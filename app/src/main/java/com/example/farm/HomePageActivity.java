package com.example.farm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomePageActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private LocationManager locationManager;
    private TextView currentLocationTextView;
    private TextView nearestTownTextView;
    private TextView bulkDensityTextView;
    private MyDatabaseHelper dbHelper;
    private TextView soil_properties_view;
    private PopupWindow popupWindow;
    private Button soil_button;
    private Button trees;
    private TextView cropsview;

    private SharedPreferences sharedPreferences;
    private Activity activity;
    private  View popupView ;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        currentLocationTextView = findViewById(R.id.current_location_text_view);
        nearestTownTextView = findViewById(R.id.nearest_town_text_view);
        bulkDensityTextView = findViewById(R.id.bulkDensityTextView);
        soil_properties_view = findViewById(R.id.soil_properties_view);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.popup_layout, null);

        // Get the screen dimensions
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );


        // Set the elevation and background color to create the overlay effect
        popupWindow.setElevation(8.0f);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set an onClickListener on the TextView to dismiss the PopupWindow
        ImageView linkTextView = popupView.findViewById(R.id.link_textview);
        linkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

       Button soil_button = findViewById(R.id.soil_button);
        soil_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
        });



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            getLocation();
        }

        dbHelper = new MyDatabaseHelper(this);

        // Inside onCreate method of HomePageActivity class after you open the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Show popup with "Logout" as its text content
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Logout")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Perform logout action
                            Log.d("MainActivity", "Logout item clicked");

                            LoginActivity.logout(sharedPreferences, HomePageActivity.this);


                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            builder.create().show();
            return true;
        }

        if (id == R.id.action_trees) {
            // Open TreesActivity
            Intent intent = new Intent(this, TreesActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_regions) {
            // Open RegionsActivity
            Intent intent = new Intent(this, RegionsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                            getSoilProperties(latitude, longitude);

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
                            }else if (address.has("street")) {
                                location = address.getString("street");
                            }else if (address.has("neighbourhood")) {
                                location = address.getString("neighbourhood");
                            } else if (address.has("center")) {
                                location = address.getString("center");
                            } else if (address.has("city")) {
                                location = address.getString("city");
                            } else if (address.has("state")) {
                                location = address.getString("state");
                            }else if (address.has("region")) {
                                location = address.getString("region");
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
    private void getSoilProperties(double latitude, double longitude) {
        String url = "https://rest.isric.org/soilgrids/v2.0/classification/query?lon=" + longitude + "&lat=" + latitude + "&format=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Extract the properties from the JSONObject
                            String type = response.getString("type");
                            JSONArray coordinates = response.getJSONArray("coordinates");
                            double longitude = coordinates.getDouble(0);
                            double latitude = coordinates.getDouble(1);
                            String wrbClassName = response.getString("wrb_class_name");

                            // Query the database for soil properties based on the retrieved soil name
                            // Create an instance of the database helper class
                            MyDatabaseHelper dbHelper = new MyDatabaseHelper(getApplicationContext());

                            // Get a readable database
                            SQLiteDatabase db = dbHelper.getReadableDatabase();

                            // Execute a select query on the "regions" table to get the soil_properties for the retrieved soil name
                            Cursor cursor = db.query("agroforestry",
                                    new String[]{"soil_properties","crops","trees"},
                                    "soil_name = ?",
                                    new String[]{wrbClassName},
                                    null,
                                    null,
                                    null);

                            // Iterate over the query results and extract the soil_properties value

                            final String soilProperties;
                            int columnIndex = cursor.getColumnIndexOrThrow("soil_properties");
                            if (cursor.moveToFirst()) {
                                soilProperties = cursor.getString(columnIndex);
                            } else {
                                soilProperties= "";
                            }
                            String crops = "";
                            int columnIndexcrops = cursor.getColumnIndexOrThrow("crops");
                            if (cursor.moveToFirst()) {
                                crops = cursor.getString(columnIndexcrops);
                            }
                            final String treesresults;
                            int treesColumn = cursor.getColumnIndexOrThrow("trees");
                            if (cursor.moveToFirst()) {
                                treesresults = cursor.getString(treesColumn);
                            } else {
                                treesresults = "";
                            }
                            cursor.close();

                            String result = "Type: " + type +
                                    "\nLongitude: " + longitude +
                                    "\nLatitude: " + latitude +
                                    "\nSoil Type: " + wrbClassName;
                            // Set the text of a TextView to the result string
                            bulkDensityTextView.setText(result);
                            cropsview=findViewById(R.id.crops);


                            cropsview.setText(crops);
                            //display the trees
                                    Button treesButton = findViewById(R.id.trees);

                                        treesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        TextView popupContentTextView = popupView.findViewById(R.id.soil_properties_popup);
                                        popupContentTextView.setText("These are the trees that can be grown in your soil type:" + "\n"+ treesresults);
                                        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                                    }
                            });
                            soil_button = findViewById(R.id.soil_button);

                            soil_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    TextView popupContentTextView = popupView.findViewById(R.id.soil_properties_popup);
                                    popupContentTextView.setText(soilProperties);
                                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Toast.makeText(HomePageActivity.this, "Unable get soil classification", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }




    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}



