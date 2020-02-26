package btl.btl.weatherapps;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class CityActivity extends AppCompatActivity {

    adapter_item_city adapterItemCity;
    ListView listViewCity;
    List<item_city> list_city;
    ImageView image_back, image_add;
    DataBase dataBase;
    String City_now;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        Intent intent = getIntent();
        City_now = intent.getStringExtra("City_now");

        init();

        add();

        image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back_main(City_now);
            }
        });

        image_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        adapterItemCity = new adapter_item_city(this, list_city);
        listViewCity.setAdapter(adapterItemCity);
        registerForContextMenu(listViewCity);

        listViewCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                back_main(list_city.get(position).getName_city());
            }
        });

    }

    private void add(){
        Cursor dataCity = dataBase.GetData("SELECT * FROM tblThanhPho ");

        while(dataCity.moveToNext()){
            String k =dataCity.getString(1);
            list_city.add(new item_city(k,"a"));
        }
    }

    private void init(){
        dataBase = new DataBase(this,"database.sqlite",null,1);

        list_city = new ArrayList<>();
        listViewCity = (ListView) findViewById(R.id.list_city);
        image_back = (ImageView) findViewById(R.id.image_back);
        image_add = (ImageView) findViewById(R.id.image_add);
    }

    private void add_city(final String city){
        dialog.hide();
        final RequestQueue requestQueue = Volley.newRequestQueue(CityActivity.this);
        String url="https://api.worldweatheronline.com/premium/v1/weather.ashx?key=4246d596a3234273ba075706191210&q="+city+"&format=json&num_of_days=1&mca=no&tp=1&lang=vi";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject obj_data = jsonObject.getJSONObject("data");

//                            Kiểm tra xem có lỗi khi gọi API không, ví dụ như sai tên thành phố
                            if(!obj_data.has("error")) {


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
                                    list_city.add(new item_city(city,"a"));
                                    adapterItemCity.notifyDataSetChanged();
                                }else {
                                    Toast.makeText(CityActivity.this,"Thành phố đã có trong danh sách!",Toast.LENGTH_LONG).show();
                                }


                            }
                            else {
                                Toast.makeText(CityActivity.this,"Tên thành phố không hợp lệ hoặc vấn đề từ phía server. Thử lại sau!",Toast.LENGTH_LONG).show();
                            }
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

    private void back_main(String city){
        Intent back = new Intent(CityActivity.this, MainActivity.class);
        back.putExtra("City_now", city);
        startActivity(back);
        finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.list_city) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_city, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.delete:
                dataBase.QuerySQL("DELETE FROM tblThanhPho WHERE Ten = '"+list_city.get(info.position).getName_city()+"'");
                list_city.remove(info.position);
                adapterItemCity.notifyDataSetChanged();
                return true;
            case R.id.show:
                back_main(list_city.get(info.position).getName_city());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void showDialog() {
        dialog = new Dialog(CityActivity.this);
        dialog.setTitle("Thêm thành phố");
        dialog.setContentView(R.layout.dialog_add_city);
        InputMethodManager inputMethodManager = (InputMethodManager) CityActivity.this.getSystemService(INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        final EditText add_city = (EditText) dialog.findViewById(R.id.add_city);
        Button btn_add = (Button) dialog.findViewById(R.id.btn_add);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) CityActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                add_city(add_city.getText().toString());
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) CityActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                dialog.hide();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
