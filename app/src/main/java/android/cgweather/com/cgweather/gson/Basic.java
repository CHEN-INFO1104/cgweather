package android.cgweather.com.cgweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cg on 2017/9/29.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public  class Update{

        @SerializedName("loc")
        public  String updatetime;
    }

}
