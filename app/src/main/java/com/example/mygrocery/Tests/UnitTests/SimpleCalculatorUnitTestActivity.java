package com.example.mygrocery.Tests.UnitTests;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mygrocery.R;

public class SimpleCalculatorUnitTestActivity extends AppCompatActivity {

    private EditText numAEt, numBEt;
    private Button equalsBtn;
    private TextView answerTv;

    String numA, numB;
    int answer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_calculator_unit_test);

        numAEt = findViewById(R.id.numAEt);
        numBEt = findViewById(R.id.numBEt);
        equalsBtn = findViewById(R.id.equalsBtn);
        answerTv = findViewById(R.id.answerTv);



        equalsBtn.setOnClickListener( e->{
            numA = numAEt.getText().toString();
            numB = numBEt.getText().toString();
            answer = calculateSum(Integer.parseInt(numA), Integer.parseInt(numB));
            answerTv.setText(""+answer);
        });

    }

    private int calculateSum(int a, int b) {
        return  a+b;
    }

}