package com.example.mygrocery.Tests.EspressoUITests;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygrocery.R;

public class Espresso2Activity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_espresso22);

        TextView viewById = (TextView) findViewById(R.id.resultView);
        //get data from Espresso2Activity.java
        Bundle inputData = getIntent().getExtras();
        String input = inputData.getString("input");
        viewById.setText(input);
    }
}