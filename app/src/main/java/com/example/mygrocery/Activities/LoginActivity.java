package com.example.mygrocery.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygrocery.MVP.View.ViewMainActivity;
import com.example.mygrocery.Tests.EspressoUITests.Espresso2Activity;
import com.example.mygrocery.Tests.EspressoUITests.LanguageActivity;
import com.example.mygrocery.Tests.UnitTests.MockitoUnitTest.loginform.view.LoginMockitoActivity;
import com.example.mygrocery.R;
import com.example.mygrocery.Tests.UnitTests.SimpleCalculatorUnitTestActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEt, passwordET;
    private TextView forgotPasswordTv,noAccount;
    private Button loginBtn;
    private String email, password;

    private Button simplecalcBtn, mvpBtn, mockitoTestBtn, myEspressoBtn, languageBtn;


    //progressbar to display while registering user
    AlertDialog dialog;

    //declare an instance of firebase
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEt = findViewById(R.id.emailEt);
        passwordET = findViewById(R.id.passwordET);
        forgotPasswordTv = findViewById(R.id.forgotPasswordTv);
        noAccount = findViewById(R.id.noAccountTv);
        loginBtn = findViewById(R.id.loginBtn);


        simplecalcBtn= findViewById(R.id.simplecalcBtn);
        mvpBtn = findViewById(R.id.mvpBtn);
        mockitoTestBtn = findViewById(R.id.mockitoTestBtn);
        myEspressoBtn = findViewById(R.id.myEspressoBtn);
        languageBtn = findViewById(R.id.languageBtn);

        email = emailEt.getText().toString().trim();
        password = passwordET.getText().toString().trim();

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setTitle("Please wait ");

        //not have account
        noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
                finish();
            }
        });

        //forgot
        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        simplecalcBtn.setOnClickListener( e->{
            startActivity(new Intent(LoginActivity.this, SimpleCalculatorUnitTestActivity.class));
        });

        mvpBtn.setOnClickListener( e->{
            startActivity(new Intent(LoginActivity.this, ViewMainActivity.class));
        });

        mockitoTestBtn.setOnClickListener(e-> {
            startActivity(new Intent(LoginActivity.this, LoginMockitoActivity.class));
        });

        myEspressoBtn.setOnClickListener( e->{
            startActivity(new Intent(LoginActivity.this, Espresso2Activity.class));
        });

        languageBtn.setOnClickListener( e->{
            startActivity(new Intent(LoginActivity.this, LanguageActivity.class));
        });

    }

    private void loginUser() {

        email = emailEt.getText().toString().trim();
        password = passwordET.getText().toString().trim();

        //validate
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //set error and focus to email edit
            emailEt.setError("Invalid Email");
            emailEt.setFocusable(true);
            return;
        }
        if (password.length()<6){
            //set error and focus to password edit
            passwordET.setError("Password length at least 6 characters");
            passwordET.setFocusable(true);
            return;
        }
        dialog.setMessage("Login progress...");
        dialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //login successful
                        makeMeOnline();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //loggin failed
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeMeOnline() {
        //after login , make user online
        dialog.setMessage("Checking user...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "true");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successful
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        //failed updating
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void checkUserType() {
        //if user is seller, start seller main activity
        //if user is user, start user main activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String accountType = ""+ds.child("accountType").getValue();
                            if (accountType.equals("Seller")){
                                //user is seller
                                dialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, MainSellerActivity.class));
                                finish();
                            }
                            else {
                                //user is buyer
                                dialog.dismiss();
                                startActivity(new Intent(LoginActivity.this, MainUserActivity.class));
                                finish();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //user is buyer
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail(){
        return this.email;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return this.password;
    }
}