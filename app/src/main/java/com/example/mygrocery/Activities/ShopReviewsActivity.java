package com.example.mygrocery.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.mygrocery.Adapters.AdapterReviews;
import com.example.mygrocery.Models.ModelReviews;
import com.example.mygrocery.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    //views From UI
    private String shopUid;
    private ImageButton backBtnSR;
    private ImageView profileIvSR;
    private TextView shopNameTvSR, ratingTvSR;
    private RatingBar ratingBarSR;
    private RecyclerView recyclerviewRatingsSR;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelReviews> reviewsArrayList;
    private AdapterReviews adapterReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        //get shopUid that was passed from ShopDetails activity
        shopUid = getIntent().getStringExtra("shopUid");

        //init Ui views
        backBtnSR  = findViewById(R.id.backBtnSR);
        profileIvSR  = findViewById(R.id.profileIvSR);
        shopNameTvSR  = findViewById(R.id.shopNameTvSR);
        ratingTvSR  = findViewById(R.id.ratingTvSR);
        ratingBarSR  = findViewById(R.id.ratingBarSR);
        recyclerviewRatingsSR  = findViewById(R.id.recyclerviewRatingsSR);

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        loadShopDetails(); //load shop name and image
        loadReviews();

        backBtnSR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); //go back to previous page
            }
        });

    }

    private float ratingSum = 0;
    private void loadReviews() {

        //init arrayList
        reviewsArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //clear list before adding data into it
                        reviewsArrayList.clear();

                        ratingSum = 0;

                        for (DataSnapshot ds : snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());//eg 4.3
                            ratingSum = ratingSum + rating; //for average rating

                            ModelReviews modelReviews = ds.getValue(ModelReviews.class);
                            reviewsArrayList.add(modelReviews);
                        }
                        //setup adapter
                        adapterReviews = new AdapterReviews(ShopReviewsActivity.this, reviewsArrayList);
                        //set to recyclerview
                        recyclerviewRatingsSR.setAdapter(adapterReviews);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating  = ratingSum/numberOfReviews;

                        ratingTvSR.setText(String.format("%.2f", avgRating)+"["+numberOfReviews+"]"); //eg 4.7[10]
                        ratingBarSR.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get shop infor
                        String shopName = ""+snapshot.child("shopName").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        //set data
                        shopNameTvSR.setText(shopName);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIvSR);
                        }
                        catch (Exception e){
                            //if an error occurs
                            profileIvSR.setImageResource(R.drawable.ic_person_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}