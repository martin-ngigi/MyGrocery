package com.example.mygrocery.Tests.EspressoUITests;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygrocery.R;

public class LanguageActivity extends AppCompatActivity {

    // Textview to show the language
    // chosen by the user
    TextView preferred_language;

    private Button espresso2Btn;


    // onClick is called whenever
    // a user clicks a button
    public void onClick(View view) {

        // whenever a user chooses a preferred language
        // by tapping button, it changes the chosen
        // language textView
        switch (view.getId()){
            case R.id.english:
                preferred_language.setText("English");
                break;
            case R.id.french:
                preferred_language.setText("French");
                break;
            case R.id.german:
                preferred_language.setText("German");
                break;
            case R.id.hindi:
                preferred_language.setText("Hindi");
                break;
            case R.id.urdu:
                preferred_language.setText("Urdu");
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        // initializing the textview
        preferred_language = findViewById(R.id.preferred_language);

        espresso2Btn = findViewById(R.id.espresso2Btn);
        espresso2Btn.setOnClickListener( e ->{
            startActivity(new Intent(LanguageActivity.this, Espresso2Activity.class));
        });

    }
}
