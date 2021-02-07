package com.rukayat_oyefeso.rainfall_assignment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private CurrentWeatherService currentWeatherService;

    private ProgressBar weatherProgressBar;
    private TextView Temperature, Location, WeatherCondition, Wind, Humidity, Visibility, Pressure, Description;
    private ImageView weatherConditionIcon;
    private EditText LocationSearch;
    private FloatingActionButton fab;

    private Button forecast_button;

    private boolean isGettingWeather = false;
    private int text_count;

    public static String currentLocation = "";
    public static int current_temperature;
    public static String condition;

    private double latitude;
    private double longitude;

    String json_String; //5 day forecast

    private static double SPLASH_TIMEOUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentWeatherService = new CurrentWeatherService(this);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        weatherProgressBar = findViewById(R.id.weather_progress_bar);
        Temperature = (TextView) findViewById(R.id.Temperature);
        Location = (TextView) findViewById(R.id.Location);
        WeatherCondition = (TextView) findViewById(R.id.Condition);
        Description = (TextView) findViewById(R.id.Description);
        Wind = (TextView) findViewById(R.id.Wind);
        Humidity = (TextView) findViewById(R.id.Humidity);
        Visibility = (TextView) findViewById(R.id.Visibility);
        Pressure = (TextView) findViewById(R.id.Pressure);
        weatherConditionIcon = (ImageView) findViewById(R.id.icon);
        LocationSearch = (EditText) findViewById(R.id.LocationSearch);
        fab = (FloatingActionButton) findViewById(R.id.FabSearch);

        forecast_button = findViewById(R.id.Forecast_btn);

        //find location
        GetGPSLocation getGPSLocation = new GetGPSLocation(getApplicationContext());
        android.location.Location l = getGPSLocation.getLocation();
        if(l != null)
        {
            latitude = l.getLatitude();
            longitude = l.getLongitude();
            findAutomaticWeather(latitude, longitude);
        }

        //try getting data after a few seconds again
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //find location
                GetGPSLocation getGPSLocation = new GetGPSLocation(getApplicationContext());
                android.location.Location l = getGPSLocation.getLocation();
                if(l != null)
                {
                    latitude = l.getLatitude();
                    longitude = l.getLongitude();
                    findAutomaticWeather(latitude, longitude);
                }
            }
        }, 6000);


        //manual location search

        LocationSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { //when the user types, we see if the user wants to search something
                count = s.toString().trim().length(); //no white space search allowed
                fab.setImageResource(count == 0 ? R.drawable.ic_refresh : R.drawable.ic_search);
                text_count = count;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text_count == 0) {
                    if(!currentLocation.equals("")){
                        RefreshWeather();
                    }
                    else{
                        //find location
                        GetGPSLocation getGPSLocation = new GetGPSLocation(getApplicationContext());
                        android.location.Location l = getGPSLocation.getLocation();
                        if(l != null)
                        {
                            latitude = l.getLatitude();
                            longitude = l.getLongitude();
                            findAutomaticWeather(latitude, longitude);
                        }
                    }

                } else {
                    searchForWeather(LocationSearch.getText().toString());
                    LocationSearch.setText("");
                }
            }
        });
        forecast_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!currentLocation.equals(""))
                    openForecastActivity();
                else{
                    Toast.makeText(getApplicationContext(), "Enter a location first", Toast.LENGTH_LONG).show();
                }
            }
        });

        Intent intent = new Intent(MainActivity.this, BroadcastReminder.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent,0);


        createNotificationChannel();

        AlarmManager alarmManager  = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
       // calendar.set(Calendar.HOUR_OF_DAY);
        calendar.set(Calendar.MINUTE,2);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_HOUR,pendingIntent);

    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.dropdown_menu:
//                Toast.makeText(this, "Preference Settings Selected", Toast.LENGTH_SHORT);
                Intent intent = new Intent(this, Preference_Settings.class);
                this.startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    protected void onDestroy() {
        super.onDestroy();
        currentWeatherService.cancel();
    }

    private void RefreshWeather() {
        if (isGettingWeather) {
            return;
        }
        searchForWeather(currentLocation);
    }

    private void searchForWeather(@NonNull final String location) {
        toggleProgress(true);
        isGettingWeather = true;
        currentWeatherService.getWeatherCurrent(location, currentWeatherCallback);
    }

    private void findAutomaticWeather(double lat, double lon){
        toggleProgress(true);
        isGettingWeather = true;
        currentWeatherService.findWeatherCurrentAuto(lat, lon, currentWeatherCallback);
    }

    private void toggleProgress(final boolean showProgress) {

        weatherProgressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);

    }

    private final CurrentWeatherService.CurrentWeatherCallback currentWeatherCallback = new CurrentWeatherService.CurrentWeatherCallback() {


        @SuppressLint("SetTextI18n")
        @Override
        public void onCurrentWeather(@NonNull CurrentWeather currentWeather) {
            currentLocation = currentWeather.location;
            Temperature.setText(String.valueOf(currentWeather.KelvinToCelcius()));
            current_temperature = currentWeather.KelvinToCelcius();
            Location.setText(currentWeather.location);
            WeatherCondition.setText(currentWeather.WeatherCondition);
            condition = currentWeather.WeatherCondition;
            Description.setText(currentWeather.Description);
            Wind.setText("Wind: " + currentWeather.WindSpeed + " km/h");
            Humidity.setText("Humidity: " + currentWeather.Humidity + "%");
            Visibility.setText("Visibility: " + currentWeather.Visibility + " km");
            Pressure.setText("Pressure: " + currentWeather.Pressure + " kPa");
            weatherConditionIcon.setImageResource(WeatherUtility.weatherIcon(currentWeather.conditionID));
            toggleProgress(false);
            isGettingWeather = false;
        }

        @Override
        public void onError(@Nullable Exception e) {
            toggleProgress(false);
            isGettingWeather = false;
            Toast.makeText(MainActivity.this, "There was an error getting data - Make sure you are connected to the internet, or try entering a different location.", Toast.LENGTH_LONG).show();
        }
    };

    public void openForecastActivity(){
        getJSON();
    }

    ///////////////////////5 DAY FORECAST/////////////////////////////

    public void getJSON(){
        Toast.makeText(this, "Checking Forecast", Toast.LENGTH_SHORT).show();
        new BackgroundTask().execute();
    }

    class BackgroundTask extends AsyncTask<Void, Void, String>
    {
        String JSONSTRING;
        String json_URL;
        @Override
        protected void onPreExecute(){
            json_URL = "http://api.openweathermap.org/data/2.5/forecast?q=" + MainActivity.currentLocation +"&appid=b850e386a8325925fdcb9539ff308b41";
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL URL = new URL(json_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) URL.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                while((JSONSTRING = bufferedReader.readLine()) != null){
                    stringBuilder.append(JSONSTRING+"\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return  stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            json_String = result;
            parseJSON();
        }
    }

    public void parseJSON(){
        if(json_String == null){
            Toast.makeText(getApplicationContext(), "Please wait and try again", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(this, ForecastActivity.class);
            intent.putExtra("json_data", json_String);
            startActivity(intent);
        }
    }

    private void createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "RainfallNotify";
            String description = "Channel for Rainfall";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("rainFallNotify", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }

}