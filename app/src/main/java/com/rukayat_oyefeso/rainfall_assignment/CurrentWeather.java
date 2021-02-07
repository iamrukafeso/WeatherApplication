package com.rukayat_oyefeso.rainfall_assignment;

public class CurrentWeather {

    final String location;
    final String conditionID;
    final String WeatherCondition;
    final String Description;
    final double TempKelvin;
    final double WindSpeed, Humidity, Visibility, Pressure;

    private static final String TAG = "TEMPERATURE";

    public CurrentWeather(String location, String conditionID, String weatherCondition, String description, double tempKelvin,  double windSpeed, double humidity, double visibility, double pressure) {
        this.location = location;
        this.conditionID = conditionID;
        WeatherCondition = weatherCondition;
        Description = description;
        TempKelvin = tempKelvin;
        WindSpeed = windSpeed;
        Humidity = humidity;
        Visibility = visibility;
        Pressure = pressure;
    }


    public int KelvinToCelcius() {
        return (int) Math.round(TempKelvin - 273.15);
    }
}

