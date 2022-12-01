package com.example.iftp2new;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.iftp2new.databinding.ActivityMainBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityMainBinding binding;
    ArrayList<String> tempList;
    ArrayList<String> timeList;
    ArrayAdapter<String> listAdapter;
    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;
    Button showGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportFragmentManager().beginTransaction().add(R.id.graphContainer, new GraphFragment()).commit();
        showGraph = findViewById(R.id.showGraphbtn);
        showGraph.setOnClickListener(this);

        initializeTempList();
        binding.fetchDatabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new fetchData().start();
            }
        });
/*
        //this is causing the app to crash everytime it starts
        //get graph from layout
        GraphView graph = (GraphView) findViewById(R.id.graph);
        //form series (curve for graph)
        LineGraphSeries<DataPoint> series =
                new LineGraphSeries<>();
        for (int x = 0 ; x<90; x++) {
            double y = Math.sin(2 * x * 0.2) - 2 * Math.sin(x * 0.2);
            series.appendData(new DataPoint(x,y), true, 90);
        }
        graph.addSeries(series);

*/
    }


    public void onClick(View v){

        if (v.getId()==R.id.showGraphbtn){
            getSupportFragmentManager().beginTransaction().replace(R.id.graphContainer, new GraphFragment()).commit();
        }
    }


    private void initializeTempList(){
        tempList = new ArrayList<>();
        timeList = new ArrayList<>();
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,tempList);
        binding.tempList.setAdapter(listAdapter);

    }

    class fetchData extends  Thread {

        String data = "";
        @Override
        public void run() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Fetching Data");
                progressDialog.setCancelable(false);
                progressDialog.show();
                }
            });

            try {
                URL url = new URL("https://api.npoint.io/6f7682e53dc4c51b5daa");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    data = data + line;
                }

                if (!data.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray temps = jsonObject.getJSONArray("Temp");
                    tempList.clear();

                    for (int i = 0; i < temps.length(); i++) {
                        JSONObject tempObject = temps.getJSONObject(i);
                        String Time = tempObject.getString("timeStamp");
                        String Temp = tempObject.getString("tempReading");
                        tempList.add("Temp " + (i + 1) + " = " + Temp + " Time: " + Time);
                    }
                }
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    listAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}