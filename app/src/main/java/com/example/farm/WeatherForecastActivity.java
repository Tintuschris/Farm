package com.example.farm;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherForecastActivity extends AppCompatActivity {
    private TextView forecastTextView;
    private static final String API_KEY = "c97a0f9ebbf89a9bdb7c569fc5f2fefd";  // OpenWeatherMap API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        forecastTextView = findViewById(R.id.forecast_text_view);

        // Get the latitude and longitude passed from HomePageActivity
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);

        // Fetch the weather forecast
        getWeatherForecast(latitude, longitude);
    }

    // Method to get 5-Day / 3-Hour forecast using OpenWeatherMap API
    private void getWeatherForecast(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=" + latitude + "&lon=" + longitude + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray forecastList = response.getJSONArray("list");
                    StringBuilder forecastInfo = new StringBuilder();

                    for (int i = 0; i < forecastList.length(); i++) {
                        JSONObject forecast = forecastList.getJSONObject(i);
                        String dateTime = forecast.getString("dt_txt");
                        JSONObject main = forecast.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        JSONArray weatherArray = forecast.getJSONArray("weather");
                        JSONObject weather = weatherArray.getJSONObject(0);
                        String description = weather.getString("description");

                        String rainInfo = "No rain forecasted";
                        if (forecast.has("rain")) {
                            JSONObject rain = forecast.getJSONObject("rain");
                            if (rain.has("3h")) {
                                double rainVolume = rain.getDouble("3h");
                                rainInfo = "Rainfall (3h): " + rainVolume + " mm";
                            }
                        }

                        forecastInfo.append("Time: ").append(dateTime).append("\n")
                                .append("Temperature: ").append(temp).append("°C\n")
                                .append("Description: ").append(description).append("\n")
                                .append(rainInfo).append("\n\n");
                    }

                    forecastTextView.setText(forecastInfo.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(WeatherForecastActivity.this, "Error parsing forecast data", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(WeatherForecastActivity.this, "Error retrieving forecast data", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}
