package com.rukayat_oyefeso.rainfall_assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

    import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Preference_Settings extends AppCompatActivity {

    Spinner spinner;
    Button sendRequest;
    Button getCurrentLocation;
    String spinnerValue = "";
    EditText city;
    EditText days;
    String cityName = "";
    String weatherDays = "";
    Integer numberOfDays = 0;
    private FusedLocationProviderClient fus;
    private DatabaseHelper myDab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference__settings);

        myDab = new DatabaseHelper(this);
    spinner = (Spinner) findViewById(R.id.spinner);
    sendRequest = (Button) findViewById(R.id.sendReq);
    // Disable Send Request Button at start
    //sendRequest.setEnabled(false);
    getCurrentLocation = (Button) findViewById(R.id.currentLoc);
    city = (EditText) findViewById(R.id.editTextCityName);
    days = (EditText) findViewById(R.id.editNumberOfDays);
    //cityName = city.getText().toString();
    //Toast.makeText(MainActivity.this, "Selected city: " + cityName, Toast.LENGTH_LONG).show();
    fus = LocationServices.getFusedLocationProviderClient(this);

        getCurrentLocation.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (ActivityCompat.checkSelfPermission(Preference_Settings.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {

                ActivityCompat.requestPermissions(Preference_Settings.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        }
        });
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveData();
            }
        });

    //addListenerOnSendRequestButton();
}

    //    // Get current user location
    public void getUserLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fus.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();

                if(location !=  null){

                    List<Address> addressList;

                    try {
                        Geocoder ge = new Geocoder(Preference_Settings.this, Locale.getDefault());

                        addressList = ge.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                        String cityName = addressList.get(0).getAdminArea();


                        if(cityName.contains("County")){
                            String cityArr [] = cityName.split(" ");
                            city.setText(cityArr[1]);
                        }
                        else{
                            city.setText(cityName);
                        }
                        Log.i("CityName",addressList.toString());


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    // Button Listener (Send Request)
    public void addListenerOnSendRequestButton() {
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the value from the selected dropdown list
                spinnerValue = String.valueOf(spinner.getSelectedItem());

                // Get the number of days from the edit text input field
                weatherDays = days.getText().toString();
                numberOfDays = Integer.parseInt(weatherDays);
                //Toast.makeText(MainActivity.this, "Value from the spinner: " + spinnerValue + ", Number of days: " + numberOfDays, Toast.LENGTH_LONG).show();

                // Get the city name from the edit text input box
                cityName = city.getText().toString();
                //selectCity();
                Toast.makeText(Preference_Settings.this, "Selected city: " + cityName, Toast.LENGTH_LONG).show();

            }
        });
    }

    private void saveData(){
        String strCity = city.getText().toString();
        String strDays = days.getText().toString();
        String strSpinner = spinner.getSelectedItem().toString();

        if(strCity.isEmpty() || strDays.isEmpty() || strSpinner.isEmpty()){
            Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
        }
        else{
            if (Integer.parseInt(strDays) < 1 || Integer.parseInt(strDays) > 5)
            {
                days.setError("Please enter days between 1 and 5");
            }
            else  {
                myDab.addData(strCity,strDays,strSpinner);
                Intent mainIntent = new Intent(Preference_Settings.this,MainActivity.class);
//                mainIntent.putExtra("myCityName", strCity);
                startActivity(mainIntent);
            }
        }

    }
}