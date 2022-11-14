package com.example.mygrocery.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygrocery.Adapters.AdapterOrderedItem;
import com.example.mygrocery.Models.ModelOrderedItem;
import com.example.mygrocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUserActivity extends AppCompatActivity {

    private String orderTo, orderId;

    /** declare views
     *
     */
    private ImageButton backBtnOD, writeReviewBtn;
    private TextView orderIdTvOD, dateTvOD, orderStatusTvOD, shopNameTvOD, totalItemsTvOD, amountTvOD, addressTvOD;
    private RecyclerView itemsRVOD;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        /**
         * get passed values from AdapterOderUser
         */
        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo"); //orderTo contains Id of the shop where we placed our order
        orderId = intent.getStringExtra("orderId");

        backBtnOD = findViewById(R.id.backBtnOD);
        orderIdTvOD = findViewById(R.id.orderIdTvOD);
        dateTvOD = findViewById(R.id.dateTvOD);
        orderStatusTvOD = findViewById(R.id.orderStatusTvOD);
        shopNameTvOD = findViewById(R.id.shopNameTvOD);
        totalItemsTvOD = findViewById(R.id.totalItemsTvOD);
        amountTvOD = findViewById(R.id.amountTvOD);
        addressTvOD = findViewById(R.id.addressTvOD);
        itemsRVOD = findViewById(R.id.itemsRVOD);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        
        loadShopInfo();
        loadOrderDetails();
        loadOrderItems();

        /**
         *  handle backBtnOD click listener
         */
        backBtnOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();//go back to previous Activity
            }
        });

        /**
         *  handle writeReviewBtn click listener
         */
        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1= new Intent(OrderDetailsUserActivity.this, WriteReviewActivity.class);
                /**
                 * passing shopuid to WriteReviewActivity
                 */
                intent1.putExtra("shopUid", orderTo); //passing shopuid to WriteReviewActivity
                startActivity(intent1);
            }
        });

    }

    /**
     * load Order Items
     */
    private void loadOrderItems() {
        /**
         * init list
         */
        orderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemArrayList.clear(); // before loading items, first clear list
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            //add to list
                            orderedItemArrayList.add(modelOrderedItem);
                        }
                        /**
                         * all items added to list
                         * set up adapter
                         */
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsUserActivity.this, orderedItemArrayList);
                        /**
                         * set adapter
                         */
                        itemsRVOD.setAdapter(adapterOrderedItem);

                        /**
                         * set item count
                         */
                        totalItemsTvOD.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        /**
         * get order details
         */
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /**
                         * get data
                         */
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        String latitude = ""+snapshot.child("latitude").getValue();
                        String longitude = ""+snapshot.child("longitude").getValue();

                        /**
                         * convert timestamp to proper format
                         */
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString(); //eg 14/11/2021 5:36 PM

                        /**
                         * set delivery status colour
                         */
                        if (orderStatus.equals("In Progress")){
                            orderStatusTvOD.setTextColor(getResources().getColor(R.color.blue));
                        }
                        else if (orderStatus.equals("Completed")){
                            orderStatusTvOD.setTextColor(getResources().getColor(R.color.background));
                        }
                        else if (orderStatus.equals("Cancelled")){
                            orderStatusTvOD.setTextColor(getResources().getColor(R.color.red));
                        }

                        /**
                         * set data
                         */
                        orderIdTvOD.setText(orderId);
                        orderStatusTvOD.setText(orderStatus);
                        amountTvOD.setText("Ksh "+orderCost+" [ Including delivery fee Ksh "+deliveryFee+" ]");
                        dateTvOD.setText(formatedDate);
                        
                        findAddress(latitude, longitude);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    /**
     * get shop info
     */
    private void loadShopInfo() {
        //get shop info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /**
                         * get shopname
                         */
                        String shopName = ""+snapshot.child("shopName").getValue();
                        shopNameTvOD.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        /**
         * find address, city, state
         */
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);

            String address = addresses.get(0).getAddressLine(0); //complete address

            /**
             * set addresses
             */
            addressTvOD.setText(address);


        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}