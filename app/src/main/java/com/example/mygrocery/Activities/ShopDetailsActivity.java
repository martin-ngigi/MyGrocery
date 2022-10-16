package com.example.mygrocery.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mygrocery.Adapters.AdapterCartItem;
import com.example.mygrocery.Adapters.AdapterProductUser;
import com.example.mygrocery.Constants;
import com.example.mygrocery.Models.ModelCartItem;
import com.example.mygrocery.Models.ModelProduct;
import com.example.mygrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    private ImageView shopIvSD;
    private TextView callBtnSD, mapBtnSD, shopNameTvSD, phoneTvSD, emailTvSD, openCloseTvSD,deliveryFeeTvSD, addressTvSD, filteredProductsTvSD, cartCountTv;
    private ImageButton backBtnSD,cartBtnSD, filterProductsBtnSD, reviewsBtnSD ;
    private EditText searchProductEtSD;
    private RecyclerView productsRVSD;
    public String deliveryFee;
    private RatingBar ratingBarSD;

    private String shopUid;
    private String myLatitude, myLongitude, myPhone;
    private String shopName, shopEmail, shopPhone, shopAddress, shopLatitude, shopLongitude;

    //progressbar to display while registering user
    AlertDialog progressDialog;

    FirebaseAuth firebaseAuth;

    private ArrayList<ModelProduct> productsLists;
    private AdapterProductUser adapterProductUser;

    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    private  EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        //init views
        shopIvSD = findViewById(R.id.shopIvSD);
        shopNameTvSD = findViewById(R.id.shopNameTvSD);
        phoneTvSD = findViewById(R.id.phoneTvSD);
        emailTvSD = findViewById(R.id.emailTvSD);
        openCloseTvSD = findViewById(R.id.openCloseTvSD);
        deliveryFeeTvSD = findViewById(R.id.deliveryFeeTvSD);
        addressTvSD = findViewById(R.id.addressTvSD);
        callBtnSD = findViewById(R.id.callBtnSD);
        mapBtnSD = findViewById(R.id.mapBtnSD);
        backBtnSD = findViewById(R.id.backBtnSD);
        cartBtnSD = findViewById(R.id.cartBtnSD);
        filterProductsBtnSD = findViewById(R.id.filterProductsBtnSD);
        searchProductEtSD = findViewById(R.id.searchProductEtSD);
        productsRVSD = findViewById(R.id.productsRVSD);
        filteredProductsTvSD = findViewById(R.id.filteredProductsTvSD);
        cartCountTv =findViewById(R.id.cartCountTv);
        reviewsBtnSD = findViewById(R.id.reviewsBtnSD);
        ratingBarSD = findViewById(R.id.ratingBarSD);

        //get shopUid from adapterShop
        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth = FirebaseAuth.getInstance();
        
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews();//avg ratings

        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        //each shop has its own products so, if user add items to cart and go back and open cart in different shop then cart should be different
        //so delete cart data whenever user open its activity
        deleteCartDialog();
        cartCount();

        //search
        searchProductEtSD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        backBtnSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to previous activity
                onBackPressed();
            }
        });

        cartBtnSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cartDialog
                showCartDialog();
            }
        });

        callBtnSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        
        mapBtnSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });


        filterProductsBtnSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category : ")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constants.productCategories1[which];
                                filteredProductsTvSD.setText(selected);
                                if (selected.equals("All")){
                                    //load all
                                    loadShopProducts();
                                }
                                else {
                                    //load filtered
                                    //adapterProductUser.getFilter().filter(selected); NB NOT WORKING, SO USED THE METHOD BELOW
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        reviewsBtnSD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass shopId to shopReviews activity
                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewsActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });


    }


    private float ratingSum = 0;
    private void loadReviews() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        ratingSum = 0;

                        for (DataSnapshot ds : snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());//eg 4.3
                            ratingSum = ratingSum + rating; //for average rating

                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating  = ratingSum/numberOfReviews;

                        ratingBarSD.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteCartDialog() {

        easyDB.deleteAllDataFromTable(); //delete all records from cart

    }

    public void cartCount(){
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count<=0){
            //no items in cart, hide cart count Tv
            cartCountTv.setVisibility(View.GONE);
        }
        else {
            //have items in cart, show cart count TV and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);//concatenate wth string because we cant set integer in textview
        }

    }

    public  double allTotalPrice = 0.00;
    public TextView subTotalTvDC, deliveryFeeTvDC, allTotalPriceTvDC;
    private void showCartDialog() {

        //init list
        cartItemList = new ArrayList<>();

        //inflate layout(dialog_cart.xml)
        View view= LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);

        //init views
        TextView shopNameDC;
        RecyclerView cartItemsRvDC;
        Button checkOutBtn;

        shopNameDC = view.findViewById(R.id.shopNameDC);
        subTotalTvDC = view.findViewById(R.id.subTotalTvDC);
        deliveryFeeTvDC = view.findViewById(R.id.deliveryFeeTvDC);
        allTotalPriceTvDC = view.findViewById(R.id.totalTvDC);
        cartItemsRvDC = view.findViewById(R.id.cartItemsRvDC);
        checkOutBtn = view.findViewById(R.id.checkOutBtn);

        //init progress dialog
        progressDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        progressDialog.setTitle("Please wait ");

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        shopNameDC.setText(shopName);

         EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

         //get all records from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice+Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity
            );

            cartItemList.add(modelCartItem);
        }

        //set up adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        //set to recyclerview
        cartItemsRvDC.setAdapter(adapterCartItem);

        deliveryFeeTvDC.setText("Ksh"+deliveryFee);
        subTotalTvDC.setText("Ksh"+String.format("%.2f", allTotalPrice));
        allTotalPriceTvDC.setText("Ksh"+(allTotalPrice+Double.parseDouble(deliveryFee.replaceAll("Ksh",""))));

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });

        //place order
        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate delivery address
                if(myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")){
                    //user didnt enter address in profile
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in your profile before placing an order...", Toast.LENGTH_SHORT).show();
                    return; //dont proceed further
                }

                if(myPhone.equals("") || myPhone.equals("null") ){
                    //user didnt phone number in profile
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your phone Number in your profile before placing an order...", Toast.LENGTH_SHORT).show();
                    return; //dont proceed further
                }
                if (cartItemList.size() == 0){
                    //cartlist empty
                    Toast.makeText(ShopDetailsActivity.this, "No items in the cart", Toast.LENGTH_SHORT).show();
                    return; //dont proceed further
                }
                
                submitOrder();
                
            }
        });

    }

    private void submitOrder() {
        //show dialog
        progressDialog.setMessage("Placing order in progress...");
        progressDialog.show();

        //timestamp for id and order time
        String timestamp = ""+System.currentTimeMillis();

        String cost = allTotalPriceTvDC.getText().toString().trim().replace("Ksh", ""); //remove Ksh if it contains

        //set up order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","In Progress");//In progress/Completed/Cancelled
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("latitude",""+myLatitude);
        hashMap.put("longitude",""+myLongitude);
        hashMap.put("deliveryFee",""+deliveryFee);

        //add to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added successfully
                        // now add order items
                        for (int i = 0; i<cartItemList.size(); i++){
                            //get data
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String name = cartItemList.get(i).getName();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            //set data
                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);

                            //add data to db
                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);

                        }
                        //dismiss progress dialog
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order placed successfully", Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);

                        }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //dismiss progress dialog
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr="+myLatitude+","+myLongitude+"&daddr="+shopLatitude+","+shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        //call
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
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
                            myPhone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();
                            myLatitude =  ""+ds.child("latitude").getValue();
                            myLongitude =  ""+ds.child("longitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop data
                String name = ""+snapshot.child("name").getValue();
                shopName = ""+snapshot.child("shopName").getValue();
                shopEmail = ""+snapshot.child("email").getValue();
                shopPhone = ""+snapshot.child("phone").getValue();
                shopAddress = ""+snapshot.child("address").getValue();
                shopLatitude = ""+snapshot.child("latitude").getValue();
                shopLongitude = ""+snapshot.child("longitude").getValue();
                deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                String profileImage = ""+snapshot.child("profileImage").getValue();
                String shopOpen = ""+snapshot.child("shopOpen").getValue();

                //set data
                shopNameTvSD.setText(name);
                emailTvSD.setText(shopEmail);
                deliveryFeeTvSD.setText("Delivery Fee Ksh"+deliveryFee);
                addressTvSD.setText(shopAddress);
                phoneTvSD.setText(shopPhone);

                if (shopOpen.equals("true")) {
                    openCloseTvSD.setText("Open");
                }
                else {
                    openCloseTvSD.setText("Closed");
                }

                try {
                    Picasso.get().load(profileImage).into(shopIvSD);
                }
                catch (Exception e){
                   // shopIvSD.setImageResource(R.drawable.ic_store_gray);
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProducts() {
        //init list
        productsLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear the list before adding items
                        productsLists.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsLists.add(modelProduct);
                        }

                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productsLists);
                        //ser Adapter
                        productsRVSD.setAdapter(adapterProductUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(String selected) {
        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting data, reset list
                        productsLists.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){

                            String productCategory = ""+ds.child("productCategory").getValue();

                            //if selected category matches product category, then add in list
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productsLists.add(modelProduct);
                            }

                        }
                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productsLists);
                        //set adapter
                        productsRVSD.setAdapter(adapterProductUser);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void prepareNotificationMessage(String orderId){
        //when user places an order, send notification to seller

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/"+Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New Order "+orderId;
        String NOTIFICATION_MESSAGE = "Congratulations... ! You have new order";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json (what to sen and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",firebaseAuth.getUid());
            notificationBodyJo.put("sellerUid",shopUid);
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC);
            notificationJo.put("data",notificationBodyJo);

        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        
        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);//after passing values, get values using intent


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed sending fcm, still start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                startActivity(intent);//after passing values, get values using intent

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key="+Constants.FCM_KEY);

                return headers;
            }
        };

        //unique the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }


}