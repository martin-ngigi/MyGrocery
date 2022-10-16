package com.example.mygrocery.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.HashMap;

public class WriteReviewActivity extends AppCompatActivity {

    private String shopUid;
    private ImageButton backBtnWR;
    private ImageView profileIvWR;
    private TextView shopNameTvWR;
    private RatingBar ratingBarWR;
    private EditText reviewEtWR;
    private ImageButton submitBtnWR;

    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        shopUid = getIntent().getStringExtra("shopUid");

        backBtnWR = findViewById(R.id.backBtnWR);
        profileIvWR = findViewById(R.id.profileIvWR);
        shopNameTvWR = findViewById(R.id.shopNameTvWR);
        ratingBarWR = findViewById(R.id.ratingBarWR);
        reviewEtWR = findViewById(R.id.reviewEtWR);
        submitBtnWR = findViewById(R.id.submitBtnWR);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        //load shop info ie shop name, shop image
        loadShopInfo();
        
        //if user has written review to this shop, load it
        loadMyReview();
        
        //go back on back press
        backBtnWR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        //input data
        submitBtnWR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop info
                String shopName = ""+snapshot.child("shopName").getValue();
                String shopImage = ""+snapshot.child("profileImage").getValue();

                //set shop info to ui
                shopNameTvWR.setText(shopName);

                try {
                    Picasso.get().load(shopImage).placeholder(R.drawable.ic_store_gray).into(profileIvWR);
                }
                catch (Exception e){
                    profileIvWR.setImageResource(R.drawable.ic_store_gray);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadMyReview() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            //my review is available in this shop

                            //get review details
                            String uid = ""+snapshot.child("uid").getValue();
                            String ratings = ""+snapshot.child("ratings").getValue();
                            String review = ""+snapshot.child("review").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();

                            //set review details to our UI
                            float myRatings = Float.parseFloat(ratings);
                            ratingBarWR.setRating(myRatings);
                            reviewEtWR.setText(review);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String ratings = ""+ratingBarWR.getRating();
        String review = reviewEtWR.getText().toString().trim();

        //for time of review
        String timestamp = ""+System.currentTimeMillis();

        //setup in hashmap
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+firebaseAuth.getUid());
        hashMap.put("ratings",""+ratings);
        hashMap.put("review",""+review);
        hashMap.put("timestamp",""+timestamp);

        //put in sb : DB > Users > shopUid > Ratings
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings").child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //review added to db
                        Toast.makeText(WriteReviewActivity.this, "Review Pushed Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                        Toast.makeText(WriteReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}