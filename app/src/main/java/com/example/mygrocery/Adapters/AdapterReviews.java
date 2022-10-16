package com.example.mygrocery.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.Models.ModelReviews;
import com.example.mygrocery.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReviews extends RecyclerView.Adapter<AdapterReviews.HolderReview>{

    private Context context;
    private ArrayList<ModelReviews> reviewsArrayList;

    public AdapterReviews(Context context, ArrayList<ModelReviews> reviewsArrayList) {
        this.context = context;
        this.reviewsArrayList = reviewsArrayList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_reviews
        View view = LayoutInflater.from(context).inflate(R.layout.row_reviews, parent, false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        //get data at position
        ModelReviews modelReviews = reviewsArrayList.get(position);
        String uid =modelReviews.getUid();
        String ratings = modelReviews.getRatings();
        String timestamp = modelReviews.getTimestamp();
        String review = modelReviews.getReview();

        //information(image, name) of the person who rote the review
        loadUserDetail(modelReviews, holder);
        
        //convert timestamp to proper format is dd/MM/yyyy
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString();

        //set data
        holder.ratingBarRR.setRating(Float.parseFloat(ratings));
        holder.reviewTvRR.setText(review);
        holder.dateTvRR.setText(dateFormat);
    }

    private void loadUserDetail(ModelReviews modelReviews, HolderReview holder) {
        //uid of user who wrote review
        String uid =modelReviews.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user infor
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        //set data
                        holder.nameTvRR.setText(name);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(holder.profileIVRR);
                        }
                        catch (Exception e){
                            //if an error occurs
                            holder.profileIVRR.setImageResource(R.drawable.ic_store_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return reviewsArrayList.size();
    }

    //class holder
    class HolderReview extends RecyclerView.ViewHolder{

        //UI views from row_reviews
        private ImageView profileIVRR;
        private TextView nameTvRR, dateTvRR, reviewTvRR;
        private RatingBar ratingBarRR;


        public HolderReview(@NonNull View itemView) {
            super(itemView);

            profileIVRR = itemView.findViewById(R.id.profileIVRR);
            nameTvRR = itemView.findViewById(R.id.nameTvRR);
            dateTvRR = itemView.findViewById(R.id.dateTvRR);
            reviewTvRR = itemView.findViewById(R.id.reviewTvRR);
            ratingBarRR = itemView.findViewById(R.id.ratingBarRR);

        }
    }
}
