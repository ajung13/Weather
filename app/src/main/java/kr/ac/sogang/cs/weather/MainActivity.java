package kr.ac.sogang.cs.weather;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    String city = "Tokyo", country = "JP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("location"));
        tabs.addTab(tabs.newTab().setText("current"));
        tabs.addTab(tabs.newTab().setText("weekly"));

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                LinearLayout tv1 = (LinearLayout) findViewById(R.id.location);
                LinearLayout tv2 = (LinearLayout) findViewById(R.id.current);
                ScrollView tv3 = (ScrollView) findViewById(R.id.weekly);

                if (position == 0) {
                    tv1.setVisibility(View.VISIBLE);
                    tv2.setVisibility(View.INVISIBLE);
                    tv3.setVisibility(View.INVISIBLE);
                    frag1();
                } else if (position == 1) {
                    tv1.setVisibility(View.INVISIBLE);
                    tv2.setVisibility(View.VISIBLE);
                    tv3.setVisibility(View.INVISIBLE);
                    frag2();
                } else if (position == 2) {
                    tv1.setVisibility(View.INVISIBLE);
                    tv2.setVisibility(View.INVISIBLE);
                    tv3.setVisibility(View.VISIBLE);
                    tv3.smoothScrollTo(0,0);
                    frag3();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

    }

    private void frag1(){
        final EditText cityText = (EditText)findViewById(R.id.inputcity);
        final EditText countryText = (EditText)findViewById(R.id.inputcountry);
        final Button locationbutton = (Button)findViewById(R.id.locbutton);
        locationbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                city = cityText.getText().toString();
                country = countryText.getText().toString();
                if(connectionAPI())
                    Toast.makeText(getApplicationContext(), "Location updated", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Wrong input", Toast.LENGTH_LONG).show();
            }
        });
    }

    HttpURLConnection conn;
    private void frag2(){
        TextView tv_today = (TextView) findViewById(R.id.today);
        TextView tv_tomor = (TextView) findViewById(R.id.tomorrow);
        TextView tv_location = (TextView) findViewById(R.id.current_loc);
        tv_today.setText("Connection Failed");
        tv_tomor.setText("Connection Failed");
        tv_location.setText("Location Error");

        try{
            if(connectionAPI()){
                String line_today="";
                String line_tomorrow="";
                String line_location="location:\n";
                String line="";
                String result="";

                InputStream is = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader in = new BufferedReader(reader);

                while((line=in.readLine())!=null){
                    result=result.concat(line);
                }

                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObj = (JSONObject) jsonParser.parse(result);

                JSONObject jsonCity = (JSONObject) jsonObj.get("city");
                line_location += jsonCity.get("name") + ", " + jsonCity.get("country");

                JSONArray listArray = (JSONArray) jsonObj.get("list");
                JSONObject jsonToday = (JSONObject) listArray.get(0);
                JSONObject jsonTomorrow = (JSONObject) listArray.get(1);

                JSONArray weather = (JSONArray) jsonToday.get("weather");
                JSONObject jsonWeather = (JSONObject) weather.get(0);
                line_today = "weather: " + jsonWeather.get("main") + " ("
                        + jsonWeather.get("description") + ")";
                weather = (JSONArray) jsonTomorrow.get("weather");
                jsonWeather = (JSONObject) weather.get(0);
                line_tomorrow = "weather: " + jsonWeather.get("main") + " ("
                        + jsonWeather.get("description") + ")";

                float ktemp, temp;
                JSONObject jsonTemp = (JSONObject) jsonToday.get("temp");
                ktemp = Float.parseFloat(jsonTemp.get("day").toString());
                temp = ktemp - (float)273.15;
                line_today += "\ntemperature: " + String.format("%.2f", temp);
                jsonTemp = (JSONObject) jsonTomorrow.get("temp");
                ktemp = Float.parseFloat(jsonTemp.get("day").toString());
                temp = ktemp - (float)273.15;
                line_tomorrow += "\ntemperature: " + String.format("%.2f", temp);

                line_today += "\nhumidity: " + jsonToday.get("humidity");
                line_today += "\nspeed: " + jsonToday.get("speed");
                line_today += "\nclouds: " + jsonToday.get("clouds");
                line_tomorrow += "\nhumidity: " + jsonTomorrow.get("humidity");
                line_tomorrow += "\nspeed: " + jsonTomorrow.get("speed");
                line_tomorrow += "\nclouds: " + jsonTomorrow.get("clouds");

                tv_location.setText(line_location);
                tv_today.setText(line_today);
                tv_tomor.setText(line_tomorrow);

                in.close();
            }
            else {
                tv_today.setText("Can't find location");
                tv_tomor.setText("Can't find location");
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

    private void frag3(){
        TextView[] tv = new TextView[6];
        tv[0] = (TextView) findViewById(R.id.day2);
        tv[1] = (TextView) findViewById(R.id.day3);
        tv[2] = (TextView) findViewById(R.id.day4);
        tv[3] = (TextView) findViewById(R.id.day5);
        tv[4] = (TextView) findViewById(R.id.day6);
        tv[5] = (TextView) findViewById(R.id.day7);
        TextView tvloc = (TextView) findViewById(R.id.current_loc2);

        for(int i=0; i<6; i++)
            tv[i].setText("Connection Failed");

        try{
            if(connectionAPI()){
                String line_day="";
                String line_location="location:\n";
                String line="";
                String result="";

                InputStream is = conn.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader in = new BufferedReader(reader);

                while((line=in.readLine())!=null){
                    result=result.concat(line);
                }

                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObj = (JSONObject) jsonParser.parse(result);

                JSONObject jsonCity = (JSONObject) jsonObj.get("city");
                line_location += jsonCity.get("name") + ", " + jsonCity.get("country");
                tvloc.setText(line_location);

                JSONArray listArray = (JSONArray) jsonObj.get("list");
                for(int i=0; i<6; i++){
                    JSONObject jsonDay = (JSONObject) listArray.get(i+2);
                    JSONArray weather = (JSONArray) jsonDay.get("weather");
                    JSONObject jsonWeather = (JSONObject) weather.get(0);
                    line_day = "weather: " + jsonWeather.get("main") + " ("
                            + jsonWeather.get("description") + ")";

                    float ktemp, temp;
                    JSONObject jsonTemp = (JSONObject) jsonDay.get("temp");
                    ktemp = Float.parseFloat(jsonTemp.get("day").toString());
                    temp = ktemp - (float)273.15;
                    line_day += "\ntemperature: " + String.format("%.2f", temp);

                    line_day += "\nspeed: " + jsonDay.get("speed");
                    line_day += "\nclouds: " + jsonDay.get("clouds");

                    tv[i].setText(line_day);
                }

                in.close();
            }
            else {
                for(int i=0;i<6;i++)
                    tv[i].setText("Can't find location");
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

    private boolean connectionAPI(){
        try{
            String urladdr = "http://api.openweathermap.org/data/2.5/forecast/daily?"
                    + "q=" + city + "," + country + "&appid=cd53a12449aad191d4ac445306607962&cnt=8";

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            conn = (HttpURLConnection)new URL(urladdr).openConnection();
            conn.setConnectTimeout(100000);
            conn.setReadTimeout(100000);
            conn.connect();

            if(conn.getResponseCode()==HttpURLConnection.HTTP_OK)
                return true;
            else
                return false;
        }catch(Exception e){
            return false;
        }
    }

}
