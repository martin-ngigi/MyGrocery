package com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.Contract;
import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.LoginPresenter;
import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.data.LoginCredentials;
import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.data.LoginRepositoryImpl;
import com.example.mygrocery.R;

public class LoginMockitoActivity extends AppCompatActivity implements Contract.LoginView {

    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_mockito);

        presenter = new LoginPresenter(new LoginRepositoryImpl(), this);

        final EditText emailView = findViewById(R.id.email);
        final EditText passwordView = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email = emailView.getText().toString();
                String password = passwordView.getText().toString();

                LoginCredentials credentials = new LoginCredentials(email, password);
                presenter.login(credentials);
            }

        });
    }

    @Override
    public void onSuccess() {
        startActivity(new Intent(this, MainMockitoActivity.class));
        finish();
    }

    @Override
    public void onFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
