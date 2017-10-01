package android.cgweather.com.cgweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.cgweather.com.cgweather.db.City;
import android.cgweather.com.cgweather.db.County;
import android.cgweather.com.cgweather.db.Province;
import android.cgweather.com.cgweather.util.HttpUtil;
import android.cgweather.com.cgweather.util.Utility;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/9/28.
 */

public class ChooseAreaFragment extends Fragment {
        public  static  final int LEVEL_PROVINCE = 0 ;
        public  static  final int LEVEL_CITY = 1 ;
        public  static  final int LEVEL_COUNTY = 2;
        private ProgressDialog progressDialog;
        private TextView titleText;
        private Button backButton;
        private ListView listview;
        private ArrayAdapter<String> adapter;
        private List<String> datalist = new ArrayList<>();

    /**
     * 省列表
     */
       private List<Province> provinceList;

    /**
     * 市列表
     */
      private List<City> cityList;

    /**
     * 县列表
     */
      private List<County> countyList;

      private Province selectedProvince;

      private City selectedCity;

      private County selectedCounty;

      private  int currentLevel;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listview = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,datalist);
        listview.setAdapter(adapter);
        return  view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCites();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else  if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else  if (getActivity() instanceof  WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);

                    }
                }
            }



        });


        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCites();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

        queryProvinces();

    }

    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            datalist.clear();
            for(Province province:provinceList){
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            currentLevel = LEVEL_PROVINCE;

        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");

        }

    }


    private void queryCites() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            datalist.clear();
            for (City city:cityList){
                datalist.add(city.getCityName());

            }
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");

        }

    }

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityId=?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            datalist.clear();
            for (County county:countyList){
                datalist.add(county.getCountyName());

            }
            adapter.notifyDataSetChanged();
            listview.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode =selectedProvince.getProvinceCode();
            int cidyCode = selectedCity.getCityCode();
            String address ="http://guolin.tech/api/china/"+provinceCode+"/"+cidyCode;
            queryFromServer(address,"county");
        }


    }
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    boolean result = false;
                    if ("province".equals(type)){
                        result = Utility.handleProvinceResponse(responseText);

                    }else  if("city".equals(type)){
                        result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                    }else if ("county".equals(type)){
                        result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                    }
                    if (result){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                closeProgressDialog();
                                if ("province".equals(type)){
                                    queryProvinces();
                                }else if("city".equals(type)){
                                    queryCites();
                                }else if("county".equals(type)){
                                    queryCounties();
                                }
                            }
                        });
                    }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog =  new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCancelable(false);

        }
             progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }


}
