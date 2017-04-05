package com.kaushik.app.smartpower;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kaushik.app.smartpower.adapter.RecyclerRowAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerRowAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<String> items = new ArrayList<>();




    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inflating layout


        //Recyclerview handling
        items.add("Switch");
        recyclerView = (RecyclerView) findViewById(R.id.ble_rc);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerRowAdapter(this,items);

        recyclerView.setAdapter(adapter);

        //Ble handling

    }

    protected void onResume() {
        super.onResume();

    }

}
