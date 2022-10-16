package com.example.mygrocery.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mygrocery.Adapters.AdapterOrderedItem;
import com.example.mygrocery.Constants;
import com.example.mygrocery.Models.ModelOrderedItem;
import com.example.mygrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    private String orderBy, orderId;

    private ImageButton backBtnODS, mapBtnODS, editBtnODS;
    private TextView orderIdTvODS, dateTvODS, orderStatusTvODS, emailTvODS, phoneTvODS, totalItemsTvODS, amountTvODS, addressTvODS;
    private RecyclerView itemsRV_ODS;
    
    FirebaseAuth firebaseAuth;

    //to open destination in map
    String sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);

        //init views
        backBtnODS = findViewById(R.id.backBtnODS);
        mapBtnODS = findViewById(R.id.mapBtnODS);
        editBtnODS = findViewById(R.id.editBtnODS);
        orderIdTvODS = findViewById(R.id.orderIdTvODS);
        dateTvODS = findViewById(R.id.dateTvODS);
        orderStatusTvODS = findViewById(R.id.orderStatusTvODS);
        emailTvODS = findViewById(R.id.emailTvODS);
        phoneTvODS = findViewById(R.id.phoneTvODS);
        totalItemsTvODS = findViewById(R.id.totalItemsTvODS);
        amountTvODS = findViewById(R.id.amountTvODS);
        addressTvODS = findViewById(R.id.addressTvODS);
        itemsRV_ODS = findViewById(R.id.itemsRV_ODS);


        //get passed values from AdapterOderUser
        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy"); //orderTo contains Id of the shop where we placed our order

        
        firebaseAuth = FirebaseAuth.getInstance();
        
        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtnODS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go back to previous page
                onBackPressed();
            }
        });

        mapBtnODS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        editBtnODS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit order status : "In Progress" ,"Completed", "Cancelled"
                editOrderStatusDialog();
            }
        });

    }

    private void editOrderStatusDialog() {
        //edit order status : "In Progress" ,"Completed", "Cancelled"

        //options to display in the dialog
        final String[] options = {"In Progress" ,"Completed", "Cancelled"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Details")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        String selectedOption = options[which];
                        editOrderStatus(selectedOption);
                    }
                })
                .show();
    }

    private void editOrderStatus(String selectedOption) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", ""+selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        String message = "Order is now "+selectedOption;

                        Toast.makeText(OrderDetailsSellerActivity.this, message, Toast.LENGTH_SHORT).show();
                        prepareNotificationMessage(orderId, message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating status
                        Toast.makeText(OrderDetailsSellerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrderDetails() {
        //load detailed infor of this order based on id
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get order info
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        String latitude = ""+snapshot.child("latitude").getValue();
                        String longitude = ""+snapshot.child("longitude").getValue();

                        //convert timestamp
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormatted = DateFormat.format("dd/MM/yyyy", calendar).toString();

                        //set order status colours
                        if (orderStatus.equals("In Progress")){
                           orderStatusTvODS.setTextColor(getResources().getColor(R.color.blue));
                        }
                        else if (orderStatus.equals("Completed")){
                            orderStatusTvODS.setTextColor(getResources().getColor(R.color.background));
                        }
                        else if (orderStatus.equals("Cancelled")){
                            orderStatusTvODS.setTextColor(getResources().getColor(R.color.red));
                        }

                        //set data
                        orderIdTvODS.setText("OrderID: "+orderId);
                        amountTvODS.setText("Amount: Ksh"+orderCost);
                        orderStatusTvODS.setText(""+orderStatus);
                        dateTvODS.setText(""+dateFormatted);

                        findAddress(latitude, longitude); //to find delivery address

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr="+sourceLatitude+","+sourceLongitude+"&daddr="+destinationLatitude+","+destinationLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sourceLatitude = ""+snapshot.child("latitude").getValue();
                        sourceLongitude= ""+snapshot.child("longitude").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get buyer info
                        destinationLatitude = ""+snapshot.child("latitude").getValue();
                        destinationLongitude= ""+snapshot.child("longitude").getValue();
                        String email= ""+snapshot.child("email").getValue();
                        String phone= ""+snapshot.child("phone").getValue();

                        //set infor
                        emailTvODS.setText(email);
                        phoneTvODS.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        //find address, city, state
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);

            //complete address
            String address = addresses.get(0).getAddressLine(0); //complete address

            //set addresses
            addressTvODS.setText(address);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOrderedItems(){
        //load the products/items of order

        //init list
        orderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //before adding data clear list
                orderedItemArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                    //add to list
                    orderedItemArrayList.add(modelOrderedItem);
                }
                //setup adapter
                adapterOrderedItem = new AdapterOrderedItem(OrderDetailsSellerActivity.this, orderedItemArrayList);
                //set adapter to our recyclerview
                itemsRV_ODS.setAdapter(adapterOrderedItem);

                //set total number of items/products in order
                totalItemsTvODS.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void prepareNotificationMessage(String orderId, String message){
        //when seller  changes order status ie In Progress, Completed, Cancelled .. send notification to buyer

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/"+ Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "Your Order "+orderId;
        String NOTIFICATION_MESSAGE = ""+message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

        //prepare json (what to sen and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",orderBy);
            notificationBodyJo.put("sellerUid",firebaseAuth.getUid());
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC);
            notificationJo.put("data",notificationBodyJo);

        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //notification sent

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //notification failed

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