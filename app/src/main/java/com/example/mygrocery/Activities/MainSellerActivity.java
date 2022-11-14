package com.example.mygrocery.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygrocery.Adapters.AdapterOrderShop;
import com.example.mygrocery.Adapters.AdapterProductSeller;
import com.example.mygrocery.Constants;
import com.example.mygrocery.Models.ModelOrderShop;
import com.example.mygrocery.Models.ModelProduct;
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

public class MainSellerActivity extends AppCompatActivity {

    /**
     * Declare the views
     */
    private TextView nameTvMS, shopNameTvMS, emailTvMS, tabProductsITv,tabOrdersTv, filteredProductsIv, filteredOrdersTv;
    private ImageButton logoutMS, editProfileMS, addProductBtnMS, filterProductsBtn, filterOrdersBtn, reviewsBtnMS, settingsBtnMS;
    private ImageView profileIvMS;
    private RelativeLayout productsRl, ordersRl;
    private EditText searchProductEt;
    private RecyclerView productsRV, ordersRV;


    /**
     * Declare the FirebaseAuth
     */
    FirebaseAuth firebaseAuth;

    /**
     * progressbar to display while registering user
     */
    AlertDialog dialog;
    
    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        /**
         * Initialize The views
         */
        nameTvMS = findViewById(R.id.nameTvMS);
        logoutMS = findViewById(R.id.logoutMS);
        editProfileMS = findViewById(R.id.editProfileMS);
        tabProductsITv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        shopNameTvMS = findViewById(R.id.shopNameTvMS);
        emailTvMS = findViewById(R.id.emailTvMS);
        addProductBtnMS = findViewById(R.id.addProductBtnMS);
        profileIvMS = findViewById(R.id.profileIvMS);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        filteredProductsIv = findViewById(R.id.filteredProductsIv);
        filterProductsBtn = findViewById(R.id.filterProductsBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRV = findViewById(R.id.productsRV);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        filterOrdersBtn = findViewById(R.id.filterOrdersBtn);
        ordersRV = findViewById(R.id.ordersRV);
        reviewsBtnMS  = findViewById(R.id.reviewsBtnMS);
        settingsBtnMS = findViewById(R.id.settingsBtnMS);

        /**
         * Initialize The firebaseAuth
         */
        firebaseAuth = FirebaseAuth.getInstance();


        /**
         * Initialize The dialog
         */
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setTitle("Please wait ");


        /**
         * Invoke the methods
         */
        checkUser();
        
        loadAllProduct();

        showProductsUI();
        
        loadAllOrders();

        /**
         * search a product
         */
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /**
         * handle logoutMS listener
         */
        logoutMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();
            }
        });

        /**
         * handle editProfileMS listener
         */
        editProfileMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit profile activity
                startActivity(new Intent(MainSellerActivity.this, ProfileEditSellerActivity.class));
            }
        });

        /**
         * handle addProductBtnMS listener
         */
        addProductBtnMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * open edit AddProduct activity
                 */
                startActivity(new Intent(MainSellerActivity.this, AddProductActivity.class));
            }
        });

        /**
         * handle tabOrdersTv listener
         */
        tabProductsITv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Products
                showProductsUI();
            }
        });

        /**
         * handle tabOrdersTv listener
         */
        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * open OrdersUI
                 */
                showOrdersUI();
            }
        });

        /**
         * handle filterProductsBtn listener
         */
        filterProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Choose Category : ")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /**
                                 * get selected item
                                 */
                                String selected = Constants.productCategories1[which];
                                filteredProductsIv.setText(selected);
                                if (selected.equals("All")){
                                    /**
                                     * load all
                                     */
                                    loadAllProduct();
                                }
                                else {
                                    /**
                                     * load filtered
                                     */
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                .show();
            }
        });

        /**
         * handle filterOrdersBtn listener
         */
        filterOrdersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Options to display in dialog
                String[] options = {"All","In Progress", "Completed", "Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle item clicks
                                if(which == 0){
                                    //All clicked
                                    filteredOrdersTv.setText("Showing all Orders");
                                    adapterOrderShop.getFilter().filter("");//show all
                                }
                                else {
                                    //"In Progress", "Completed", "Cancelled" clicked
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Showing "+optionClicked+" Orders"); //e.g "In Progress" orders
                                    adapterOrderShop.getFilter().filter(optionClicked);

                                }
                            }
                        })
                        .show();
            }
        });

        /**
         * handle reviewsBtnMS listener
         */
        reviewsBtnMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Reviews activity
                Intent intent = new Intent(MainSellerActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", ""+firebaseAuth.getUid());
                startActivity(intent);
            }
        });

        /**
         * handle settingsBtnMS listener
         */
        settingsBtnMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainSellerActivity.this, SettingsActivity.class));
            }
        });

    }

    private void loadAllOrders() {
        /**
         * init array list
         */
        orderShopArrayList = new ArrayList<>();

        /**
         * load orders of shop
         */
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /**
                         * clear list before adding data in it
                         */
                        orderShopArrayList.clear();

                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);

                            /**
                             * add to list
                             */
                            orderShopArrayList.add(modelOrderShop);
                        }
                        /**
                         * setup adapter
                         */
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this, orderShopArrayList);
                        /**
                         * set adapter to recyclerview
                         */
                        ordersRV.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadFilteredProducts(String selected) {
        /**
         * get all products
         */
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /**
                         * before getting data, reset list
                         */
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){

                            String productCategory = ""+ds.child("productCategory").getValue();

                            /**
                             * if selected category matches product category, then add in list
                             */
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }

                        }
                        /**
                         * setup adapter
                         */
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        /**
                         * set adapter
                         */
                        productsRV.setAdapter(adapterProductSeller);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProduct() {
        productList = new ArrayList<>();

        /**
         * get all products
         */
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /**
                         * before getting data, reset list
                         */
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        /**
                         * setup adapter
                         */
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this, productList);
                        /**
                         * set adapter
                         */
                        productsRV.setAdapter(adapterProductSeller);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void showProductsUI() {
        /**
         * show Product sUI and hide orders UI
         */
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsITv.setTextColor(getResources().getColor(R.color.black));
        tabProductsITv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect00);
    }

    private void showOrdersUI() {
        /**
         * show Orders UI and hide products UI
         */
        productsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabProductsITv.setTextColor(getResources().getColor(R.color.black));
        tabProductsITv.setBackgroundResource(R.drawable.shape_rect00);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
    }

    /**
     * after login , make user online
     */
    private void makeMeOffline() {
        dialog.setMessage("Log out in progress...");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        /**
         * update value to db
         */
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        /**
                         * update successful
                         */
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        /**
                         * failed updating
                         */
                        dialog.dismiss();
                        Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     *
     */
    private void checkUser() {
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainSellerActivity.this, LoginActivity.class));
            finish();
        }
        else {
            loadMyInfo();
        }
    }

    /**
     * loadMyInfo such as personal infor
     */
    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            /**
                             * get data from db
                             */
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String email = ""+ds.child("email").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();

                            /**
                             * set data
                             */
                            nameTvMS.setText(name);
                            emailTvMS.setText(email);
                            shopNameTvMS.setText(shopName);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_green).into(profileIvMS);
                            }
                            catch (Exception e){
                                profileIvMS.setImageResource(R.drawable.ic_person_green);
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}