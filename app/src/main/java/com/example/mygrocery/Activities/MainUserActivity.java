package com.example.mygrocery.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygrocery.Adapters.AdapterOrderUser;
import com.example.mygrocery.Adapters.AdapterShop;
import com.example.mygrocery.Models.ModelOrderUser;
import com.example.mygrocery.Models.ModelShop;
import com.example.mygrocery.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTvMU, emailTvMU, phoneTvMU, tabShopsTvMU, tabOrdersTvMU, allShopsTvMU, localShopsTvMU;
    private ImageButton logoutMU, editProfileMU, settingsBtnMU;
    private RelativeLayout shopsRl, ordersRl;
    private ImageView profileIvMU;
    private RecyclerView shopsRV, ordersRVMU;

    FirebaseAuth firebaseAuth;

    //progressbar to display while registering user
    AlertDialog dialog;

    private ArrayList<ModelShop> shopList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTvMU = findViewById(R.id.nameTvMU);
        logoutMU = findViewById(R.id.logoutMU);
        editProfileMU = findViewById(R.id.editProfileMU);
        emailTvMU = findViewById(R.id.emailTvMU);
        phoneTvMU = findViewById(R.id.phoneTvMU);
        tabShopsTvMU = findViewById(R.id.tabShopsTvMU);
        tabOrdersTvMU = findViewById(R.id.tabOrdersTvMU);
        shopsRl = findViewById(R.id.shopsRl);
        ordersRl = findViewById(R.id.ordersRl);
        profileIvMU = findViewById(R.id.profileIvMU);
        shopsRV = findViewById(R.id.shopsRV);
        allShopsTvMU = findViewById(R.id.allShopsTvMU);
        localShopsTvMU = findViewById(R.id.localShopsTvMU);
        ordersRVMU = findViewById(R.id.ordersRVMU);
        settingsBtnMU= findViewById(R.id.settingsBtnMU);

        firebaseAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setTitle("Please wait ");


        checkUser();

       // loadShops();
        
        //at first show shops UI
        showShopsUI();
        
        logoutMU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();
            }
        });

        editProfileMU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit profile activity
                //open edit profile
                /**
                startActivity(new Intent(MainUserActivity.this, ProfileEditUserActivity.class));
                 **/
            }
        });

        tabShopsTvMU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open shops
                showShopsUI();
            }
        });

        tabOrdersTvMU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open OrdersUI
                showOrdersUI();
            }
        });

        settingsBtnMU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainUserActivity.this, SettingsActivity.class));
            }
        });



    }

    private void showShopsUI() {
        //show Product sUI and hide orders UI
        shopsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTvMU.setTextColor(getResources().getColor(R.color.black));
        tabShopsTvMU.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTvMU.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTvMU.setBackgroundResource(R.drawable.shape_rect00);
    }

    private void showOrdersUI() {
        //show Orders UI and hide products UI
        shopsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabShopsTvMU.setTextColor(getResources().getColor(R.color.black));
        tabShopsTvMU.setBackgroundResource(R.drawable.shape_rect00);

        tabOrdersTvMU.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTvMU.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void makeMeOffline() {
        //after login , make user online
        dialog.setMessage("Log out in progress...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //update successful
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        //failed updating
                        dialog.dismiss();
                        Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void checkUser() {
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainUserActivity.this, LoginActivity.class));
            finish();
        }
        else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            //get Data
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();

                            //set data
                            nameTvMU.setText(name);
                            emailTvMU.setText(email);
                            phoneTvMU.setText(phone);


                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_green).into(profileIvMU);
                            }
                            catch (Exception e){
                                profileIvMU.setImageResource(R.drawable.ic_person_green);
                            }

                            //load only those shops that are in the city of the user
                            loadShops(city);
                            loadOrders();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrders() {
        //init orders list
        ordersList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before adding
                ordersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    String uid = ds.getRef().getKey();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (snapshot.exists()){
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                            //add to list
                                            ordersList.add(modelOrderUser);
                                        }
                                        //set up adapter
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this, ordersList);
                                        //set to recyclerview
                                        ordersRVMU.setAdapter(adapterOrderUser);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void loadShops(String myCity) {
        //init list
        shopList = new ArrayList<>();

        //first display all shops on arrival

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        shopList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelShop modelShop = ds.getValue(ModelShop.class);

                            String shopCity = ""+ds.child("city").getValue();

                            //show only user city shops
                            if (shopCity.equals(myCity)){
                                shopList.add(modelShop);
                            }

                            //if you want to to display all shops, skip the if statement
                            //shopList.add(modelShop);
                        }

                        //setup adapter
                        adapterShop = new AdapterShop(MainUserActivity.this, shopList);

                        //set adapter to recyclerview
                        shopsRV.setAdapter(adapterShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //on clicking all shops, display all shops
        allShopsTvMU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.orderByChild("accountType").equalTo("Seller")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //clear list before adding
                                shopList.clear();
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    ModelShop modelShop = ds.getValue(ModelShop.class);

                                    String shopCity = ""+ds.child("city").getValue();

                                    //if you want to to display all shops, skip the if statement
                                    shopList.add(modelShop);
                                    allShopsTvMU.setBackgroundResource(R.drawable.shape_rect04);
                                    localShopsTvMU.setBackgroundResource(R.drawable.shape_rect02);
                                }

                                //setup adapter
                                adapterShop = new AdapterShop(MainUserActivity.this, shopList);

                                //set adapter to recyclerview
                                shopsRV.setAdapter(adapterShop);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

        //on clicking local shops, display nearby shops ie shops within my city
        localShopsTvMU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.orderByChild("accountType").equalTo("Seller")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //clear list before adding
                                shopList.clear();
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    ModelShop modelShop = ds.getValue(ModelShop.class);

                                    String shopCity = ""+ds.child("city").getValue();

                                    //show only user city shops
                                    if (shopCity.equals(myCity)){
                                        shopList.add(modelShop);
                                        localShopsTvMU.setBackgroundResource(R.drawable.shape_rect04);
                                        allShopsTvMU.setBackgroundResource(R.drawable.shape_rect02);
                                    }

                                    //if you want to to display all shops, skip the if statement
                                    //shopList.add(modelShop);
                                }

                                //setup adapter
                                adapterShop = new AdapterShop(MainUserActivity.this, shopList);

                                //set adapter to recyclerview
                                shopsRV.setAdapter(adapterShop);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

    }

    private void loadShops() {
        //init list
        shopList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        shopList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelShop modelShop = ds.getValue(ModelShop.class);

                            String shopCity = ""+ds.child("city").getValue();


                            shopList.add(modelShop);
                        }

                        //setup adapter
                        adapterShop = new AdapterShop(MainUserActivity.this, shopList);

                        //set adapter to recyclerview
                        shopsRV.setAdapter(adapterShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}