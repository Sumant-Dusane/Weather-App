package io.triads.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView cityNameTV, temperature, conditionRV;
    private TextView timeCard, temperatureCard, windCard;
    private ImageView icon, weatherBg, searchIcon;
    private TextInputEditText searchCity;
    private RecyclerView weatherForecast;
    private ArrayList<WeatherModal> weatherModalArrayList;
    private WeatherAdapter weatherAdapter;
    private RelativeLayout homeBg;
    private ProgressBar progressBar;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityNameStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().setAttributes(params);

        cityNameTV = findViewById(R.id.cityName);
        temperature = findViewById(R.id.temperature);
        conditionRV = findViewById(R.id.conditionRV);
        timeCard = findViewById(R.id.timeCard);
        temperatureCard = findViewById(R.id.temperatureCard);
        windCard = findViewById(R.id.windSpeedCard);
        icon = findViewById(R.id.icon);
        searchIcon = findViewById(R.id.searhCityIcon);
        homeBg = findViewById(R.id.homeBg);
        progressBar = findViewById(R.id.progressBar);
        searchCity = findViewById(R.id.searchCity);
        weatherForecast = findViewById(R.id.weatherForecast);
        weatherBg = findViewById(R.id.weatherBg);
        weatherModalArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherModalArrayList);
        weatherForecast.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null){
            cityNameStr = getCityName(location.getLongitude(),location.getLatitude());
        } else {
            cityNameStr = "Mumbai";
            Toast.makeText(getApplicationContext(), "Null Longitude, Latitude", Toast.LENGTH_SHORT).show();
        }
        getWeather(cityNameStr);

        searchIcon.setOnClickListener(v -> {
            String city = searchCity.getText().toString();
            if (city.isEmpty()){
                searchCity.setError("Please Enter City Name");
            }else {
                searchCity.setText(cityNameStr);
                getWeather(city);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "Please Provide Permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "City Not Found";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 10);

            for (Address address: addressList){
                if (address != null){
                    String city = address.getLocality();
                    if (city!= null && !city.equals("")){
                        cityName = city;
                    }else {
                        searchCity.setError("City Not Found");
                        searchCity.requestFocus();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeather(String cityName){
        String apiUrl = "http://api.weatherapi.com/v1/forecast.json?key=c6ee6d77f0734bf1a67184214222804&q=" + cityName + "&days=1&aqi=no&alerts=no";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest =  new JsonObjectRequest(Request.Method.GET, apiUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                homeBg.setVisibility(View.VISIBLE);
                weatherModalArrayList.clear();
                try {
                    String temp = response.getJSONObject("current").getString("temp_c");
                    temperature.setText(temp + "°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http://" .concat(conditionIcon)).into(icon);
                    conditionRV.setText(condition);

                    if (isDay == 1){
                        Picasso.get().load("https://www.setaswall.com/wp-content/uploads/2017/03/Sky-Phone-Wallpaper-1080x2340-050-340x550.jpg").into(weatherBg);
                    }else if (isDay == 0){
                        //night
                        Picasso.get().load("https://images.unsplash.com/photo-1435224654926-ecc9f7fa028c?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1yZWxhdGVkfDF8fHxlbnwwfHx8fA%3D%3D&w=1000&q=80").into(weatherBg);
//

                    }
                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastD = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastD.getJSONArray("hour");

                    for (int i = 0; i < hourArray.length(); i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");

                        weatherModalArrayList.add(new WeatherModal(time, temper, img, wind));
                    }
                    weatherAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                Log.e("myTag" ,error.toString());
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}