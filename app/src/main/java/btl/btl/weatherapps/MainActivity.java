package btl.btl.weatherapps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    EditText edtsearch;
    TextView Txt_now_tem, Txt_min, Txt_max, Txt_type_weather, Txt_humidity, Txt_wind_speed, Txt_hour_tem, Txt_hour;
    ImageView img_type_wether, img_hour, image_my_location, image_list_city, image_MaxTem, image_MinTem, image_WindSpeed, image_Humodity;
    ImageButton button_find, button_share;
    String City;
    LayoutInflater inflater;
    LinearLayout list_detail_hour;
    ListView list_day;
    LinearLayout item_hour_flat;
    List<item_day> detail_day;
    adapter_item_day adapter_day;
    String mylocation;
    DataBase dataBase;

    CallbackManager callbackManager;
    ShareDialog shareDialog;

    private Location location;
    // Đối tượng tương tác với Google API
    private GoogleApiClient gac;

    private Uri imageUri;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_main);
        init();


        adapter_day = new adapter_item_day(this, detail_day);
        list_day.setAdapter(adapter_day);

        // Khi người dùng click vào các ListItem day
        list_day.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                getData(City,detail_day.get(position).getDay());
            }
        });


//        Sự kiện ấn nút vị trí
        image_my_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeWeatherMyLocation();
            }
        });

//        Sự kiện sang màn hình thành phố
        image_list_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(MainActivity.this, CityActivity.class);
                go.putExtra("City_now", City);
                startActivity(go);
            }
        });

//        Sự kiện nút tìm kiếm
        button_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(edtsearch.getText().toString(),"no");
            }
        });


//        Cài đặt nút done và nhận sự kiện nút done
        edtsearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edtsearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== EditorInfo.IME_ACTION_DONE){
                    getData(edtsearch.getText().toString(),"no");
                }
                return false;
            }
        });



//        Nút share
        button_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Share ảnh

//                Các sự kiện thành công, hủy bỏ hay thất bại khi share
                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(MainActivity.this, "Chia sẻ thành công", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(MainActivity.this, "Chia sẻ bị hủy", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(MainActivity.this, "Chia sẻ thất bại", Toast.LENGTH_LONG).show();
                    }
                });

//                Chụp ảnh màn hình, đưa về dạng Bitmap
                View v1 = getWindow().getDecorView().getRootView();
                v1.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                v1.setDrawingCacheEnabled(false);

//                Cài đặt và hiển thị dialog share
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(bitmap)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();

                if(ShareDialog.canShow(SharePhotoContent.class)){
                    shareDialog.show(content);
                }
            }
        });

        // Trước tiên chúng ta cần phải kiểm tra play services
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

    }

//    Callback xem share được không
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

//    Hàm gọi lại vị trí hiện tại
    public void takeWeatherMyLocation(){
        getData(mylocation,"no");
    }


//    Lấy thông tin thời tiết hiện tại và theo giờ
    public void getcurrentweatherdata(final String city, final String day)
    {
        final RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        String url;

//        So sánh xem có ngày truyền vào không, nếu có thì load theo ngày, không thì mặc định hôm nay
        if(day.equals("no")){
            Log.d("popo", "getcurrentweatherdata: "+day);
            url = "https://api.worldweatheronline.com/premium/v1/weather.ashx?key=4246d596a3234273ba075706191210&q="+city+"&format=json&num_of_days=1&mca=no&tp=1&lang=vi";
        }else {
            url = "https://api.worldweatheronline.com/premium/v1/weather.ashx?key=4246d596a3234273ba075706191210&q="+city+"&format=json&num_of_days=1&date="+day+"&mca=no&tp=1&lang=vi";
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject obj_data = jsonObject.getJSONObject("data");

//                            Kiểm tra xem có lỗi khi gọi API không, ví dụ như sai tên thành phố
                            if(!obj_data.has("error")) {
//                            Lưu location vào city
                                City = city;
                                edtsearch.setText(city);


                                JSONArray obj_weather = obj_data.getJSONArray("weather");

//                              So sánh xem có ngày truyền vào không, nếu có thì load thời tiết theo ngày, không thì thời tiết theo thời gian thực
                                if(day.equals("no")){

//                              Thêm thông tin phần tổng quan, thời tiết thời gian thực hiện tại
                                    JSONArray obj_current_condition = obj_data.getJSONArray("current_condition");

                                    String now_tem = obj_current_condition.getJSONObject(0).getString("temp_C");
                                    Txt_now_tem.setText(now_tem + " °C");

                                    int nhietdo = Integer.parseInt(now_tem);
                                    if(nhietdo<10) Txt_now_tem.setTextColor("mã màu đỏ");

                                    String type_weathe = obj_current_condition.getJSONObject(0).getJSONArray("lang_vi").getJSONObject(0).getString("value");
                                    Txt_type_weather.setText(new String(type_weathe.getBytes("ISO-8859-1")));
                                    String type_weathe_english = obj_current_condition.getJSONObject(0).getJSONArray("weatherDesc").getJSONObject(0).getString("value");
                                    img_type_wether.setImageResource(getResources().getIdentifier(convert_type_weather(type_weathe_english) , "drawable", getPackageName()));
                                    String humidity = obj_current_condition.getJSONObject(0).getString("humidity");
                                    Txt_humidity.setText(humidity + " %");
                                    image_Humodity.setImageResource(R.drawable.ic_humidity);
                                    String wind_speed = obj_current_condition.getJSONObject(0).getString("windspeedKmph");
                                    Txt_wind_speed.setText(wind_speed + " KM/h");
                                    image_WindSpeed.setImageResource(R.drawable.ic_wind);



                                    String min_tem = obj_weather.getJSONObject(0).getString("mintempC");
                                    Txt_min.setText(min_tem + " °C");
                                    image_MinTem.setImageResource(R.drawable.ic_min_c);
                                    String max_tem = obj_weather.getJSONObject(0).getString("maxtempC");
                                    Txt_max.setText(max_tem + " °C");
                                    image_MaxTem.setImageResource(R.drawable.ic_max_c);

                                }else {
                                    JSONArray obj_hourly = obj_weather.getJSONObject(0).getJSONArray("hourly");
                                    String now_tem = obj_hourly.getJSONObject(0).getString("tempC");
                                    Txt_now_tem.setText(now_tem + " °C");
                                    String type_weathe = obj_hourly.getJSONObject(0).getJSONArray("lang_vi").getJSONObject(0).getString("value");
                                    Txt_type_weather.setText(new String(type_weathe.getBytes("ISO-8859-1")));
                                    String type_weathe_english = obj_hourly.getJSONObject(0).getJSONArray("weatherDesc").getJSONObject(0).getString("value");
                                    img_type_wether.setImageResource(getResources().getIdentifier(convert_type_weather(type_weathe_english) , "drawable", getPackageName()));
                                    String humidity = obj_hourly.getJSONObject(0).getString("humidity");
                                    Txt_humidity.setText(humidity + " %");
                                    image_Humodity.setImageResource(R.drawable.ic_humidity);
                                    String wind_speed = obj_hourly.getJSONObject(0).getString("windspeedKmph");
                                    Txt_wind_speed.setText(wind_speed + " KM/h");
                                    image_WindSpeed.setImageResource(R.drawable.ic_wind);



                                    String min_tem = obj_weather.getJSONObject(0).getString("mintempC");
                                    Txt_min.setText(min_tem + " °C");
                                    image_MinTem.setImageResource(R.drawable.ic_min_c);
                                    String max_tem = obj_weather.getJSONObject(0).getString("maxtempC");
                                    Txt_max.setText(max_tem + " °C");
                                    image_MaxTem.setImageResource(R.drawable.ic_max_c);
                                }



//                                Thêm thành phố vào dataBase
                                Cursor dataCity = dataBase.GetData("SELECT * FROM tblThanhPho ");
                                int dem=0;
                                while (dataCity.moveToNext()){
                                    if(dataCity.getString(1).toLowerCase().replaceAll("\\s+","").equals(city.toLowerCase().replaceAll("\\s+",""))){
                                        dem++;
                                        break;
                                    }
                                }
                                if(dem==0){
                                    dataBase.QuerySQL("INSERT INTO tblThanhPho VALUES(null,'"+city+"', '"+"a"+"')");
                                }


//                            Xóa sạch danh sách từng giờ
                                list_detail_hour.removeAllViews();

//                            Thêm thông tin theo từng giờ

                                JSONArray hourly_arr = obj_weather.getJSONObject(0).getJSONArray("hourly");
                                for (int i = 0; i < 24; i++) {

                                    item_hour_flat = (LinearLayout) inflater.inflate(R.layout.item_hour, null);
                                    img_hour = (ImageView) item_hour_flat.findViewById(R.id.image_hour);
                                    Txt_hour = (TextView) item_hour_flat.findViewById(R.id.Txt_hour);
                                    Txt_hour_tem = (TextView) item_hour_flat.findViewById(R.id.Txt_hour_tem);


                                    String tem_hour = hourly_arr.getJSONObject(i).getString("tempC");
                                    Txt_hour_tem.setText(tem_hour + " °C");
                                    Txt_hour.setText(i + ":00");
                                    String type_weathe_hour = hourly_arr.getJSONObject(i).getJSONArray("weatherDesc").getJSONObject(0).getString("value");
                                    img_hour.setImageResource(getResources().getIdentifier(convert_type_weather(type_weathe_hour) , "drawable", getPackageName()));


                                    list_detail_hour.addView(item_hour_flat);

                                }

                                get7dayweatherdata(city);
                            }
                            else {
                                Toast.makeText(MainActivity.this,"Tên thành phố không hợp lệ hoặc vấn đề từ phía server. Thử lại sau!",Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add (stringRequest);
    }

//    Lấy thời tiết trong 7 ngày
    public void get7dayweatherdata(String location){
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        String url = "https://api.worldweatheronline.com/premium/v1/weather.ashx?key=4246d596a3234273ba075706191210&q="+location+"&format=json&num_of_days=7&mca=no&tp=24&lang=vi";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject obj_data = jsonObject.getJSONObject("data");

                            JSONArray obj_weather = obj_data.getJSONArray("weather");

                            detail_day.clear();

                            for(int i=0;i<7;i++){
                                String data = obj_weather.getJSONObject(i).getString("date");
                                String min = obj_weather.getJSONObject(i).getString("maxtempC");
                                String max = obj_weather.getJSONObject(i).getString("mintempC");
                                JSONArray hourly_arr = obj_weather.getJSONObject(i).getJSONArray("hourly");
                                String type_weathe_hour = hourly_arr.getJSONObject(0).getJSONArray("weatherDesc").getJSONObject(0).getString("value");
                                detail_day.add(new item_day(data, min, max, getResources().getIdentifier(convert_type_weather(type_weathe_hour) , "drawable", getPackageName())));
                            }

                            adapter_day.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add (stringRequest);


    }




//    Hàm lấy all thông tin
    private void getData(String city, String day) {

//        Kiểm tra kết nối mạng
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

//        Nếu có lấy data, không thì toast
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            getcurrentweatherdata(city, day);
        }else {
            Toast.makeText(this, "Lỗi kết nối Internet, kiểm tra lại", Toast.LENGTH_LONG).show();
        }
    }

    public String convert_type_weather(String type){
        type = type.toLowerCase();
        if(type.contains("sun")){
            return "cloud_sun";
        }else {
            if(type.contains("clear")){
                return "sun";
            }else {
                if(type.contains("cloud")){
                    return "cloud";
                }else {
                    if(type.contains("rain")){
                        return "rain";
                    }else {
                        if(type.contains("drizzle")){
                            return "drizzle";
                        }else {
                            if(type.contains("overcast")){
                                return "over";
                            }else {
                                if(type.contains("haze")){
                                    return "haze";
                                }else {
                                    if(type.contains("storm")||type.contains("thunder")){
                                        return "storm";
                                    }else {
                                        if(type.contains("freezing")||type.contains("snow")){
                                            return "snow";
                                        }else {
                                            if(type.contains("blizzard")){
                                                return "blizzard";
                                            }else {
                                                if(type.contains("ice")){
                                                    return "ice";
                                                }else {
                                                    if(type.contains("wind")){
                                                        return "wind";
                                                    }else {
                                                        return "cloud_sun";
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private void init()
    {
//        Khởi tạo database
        dataBase = new DataBase(this,"database.sqlite",null,1);
        dataBase.QuerySQL("CREATE TABLE IF NOT EXISTS tblThanhPho(Id INTEGER PRIMARY KEY AUTOINCREMENT, Ten NVARCHAR(100), Image CHAR(100))");

        list_detail_hour = (LinearLayout) findViewById(R.id.casts_container);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        list_day = (ListView) findViewById(R.id.list_day);
        edtsearch = (EditText) findViewById(R.id.edit_city);
        Txt_now_tem = (TextView) findViewById(R.id.TxT_now_tem);
        Txt_min =(TextView) findViewById(R.id.TxT_min);
        Txt_max = (TextView) findViewById(R.id.TxT_max);
        Txt_type_weather=(TextView) findViewById(R.id.TxT_type_weather);
        Txt_humidity= (TextView) findViewById(R.id.TxT_humidity);
        Txt_wind_speed=(TextView) findViewById(R.id.TxT_wind_speed);
        img_type_wether = (ImageView) findViewById(R.id.img_type_wether);
        image_my_location = (ImageView) findViewById(R.id.image_my_location);
        image_list_city = (ImageView) findViewById(R.id.image_list_city);
        button_find = (ImageButton) findViewById(R.id.button_find);
        detail_day = new ArrayList<>();
        image_MaxTem = (ImageView) findViewById(R.id.image_MaxTem);
        image_MinTem = (ImageView) findViewById(R.id.image_MinTem);
        image_WindSpeed = (ImageView) findViewById(R.id.image_WindSpeed);
        image_Humodity = (ImageView) findViewById(R.id.image_Humodity);
        button_share = (ImageButton) findViewById(R.id.button_share);

//        Ánh xạ FB
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
    }

    /**
     * Phương thức này dùng để hiển thị trên UI
     * */
    private void getLocation() throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Kiểm tra quyền hạn
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        } else {
            location = LocationServices.FusedLocationApi.getLastLocation(gac);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Lấy thông tin của tọa độ này
                Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());

                String temp = Normalizer.normalize(gcd.getFromLocation(latitude, longitude, 1).get(0).getAdminArea(), Normalizer.Form.NFD);
                Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

                City = pattern.matcher(temp).replaceAll("");
                mylocation=City;

                Intent intent = getIntent();
                String name_now=intent.getStringExtra("City_now");
                if(name_now==null){
                    getData(City,"no");
                }else {
                    getData(name_now,"no");
                }
            } else {
                Log.d("Ang", "getLocation: "+"(Không thể nhận được thị vị trí. " +
                        "Bạn đã kích hoạt location trên thiết bị chưa?)");
            }
        }
    }
    /**
     * Tạo đối tượng google api client
     * */
    protected synchronized void buildGoogleApiClient() {
        if (gac == null) {
            gac = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }
    }
    /**
     * Phương thức kiểm chứng google play services trên thiết bị
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1000).show();
            } else {
                Toast.makeText(this, "Thiết bị này không hỗ trợ.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Đã kết nối với google api, lấy vị trí
        try {
            getLocation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        gac.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Lỗi kết nối: " + connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    protected void onStart() {
        gac.connect();
        super.onStart();
    }
    protected void onStop() {
        gac.disconnect();
        super.onStop();
    }
}
