package android.cgweather.com.cgweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cg on 2017/9/29.
 */

public class Weather {

    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;


}
