package com.example.mygrocery.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**import com.example.mygrocery.Activities.ShopDetailsActivity;**/
import com.example.mygrocery.Models.ModelProduct;
import com.example.mygrocery.Models.ModelShop;
import com.example.mygrocery.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop>{

    private Context context;
    public ArrayList<ModelShop> shopList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_shop.xml)
        View view= LayoutInflater.from(context).inflate(R.layout.row_shop, parent, false);

        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  HolderShop holder, int position) {
        //get data
        //, , , shopName, , , , , , ,
        //            , , , , , , ;
        ModelShop modelShop = shopList.get(position);
        String accountType = modelShop.getAccountType();
        String address = modelShop.getAddress();
        String city = modelShop.getCity();
        String country = modelShop.getCountry();
        String deliveryFee = modelShop.getDeliveryFee();
        String email = modelShop.getEmail();
        String longitude = modelShop.getLongitude();
        String latitude = modelShop.getLatitude();
        String online = modelShop.getOnline();
        String name = modelShop.getName();
        String phone = modelShop.getPhone();
        String uid = modelShop.getUid();
        String timestamp = modelShop.getTimestamp();
        String shopOpen = modelShop.getShopOpen();
        String state = modelShop.getState();
        String profileImage = modelShop.getProfileImage();
        String shopName = modelShop.getShopName();

        loadReviews(modelShop, holder); //load avg ratings, set to rating bar

        //set data
        holder.shopNameTvRS.setText(shopName);
        holder.phoneTvRS.setText(phone);
        holder.addressTvRS.setText(address);

        //check if online
        if (online.equals("true")){
            //shop owner is online
            holder.onlineIvRS.setVisibility(View.VISIBLE);
        }
        else {
            //shop owner is offline
            holder.onlineIvRS.setVisibility(View.GONE);
        }

        //check if if shop open
        if (shopOpen.equals("true")){
            //shop  is open
            holder.shopClosedTVRS.setVisibility(View.GONE);
        }
        else {
            //shop  is closed
            holder.onlineIvRS.setVisibility(View.VISIBLE);
        }

        //set shop image
        try {
            Picasso.get().load(profileImage)
                    .placeholder(R.drawable.ic_store_gray)
                    .into(holder.shopIvRS);
        }
        catch (Exception e){
            holder.shopIvRS.setImageResource(R.drawable.ic_store_gray);
        }

        //handle click listener for show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid", uid);
                context.startActivity(intent);
                 **/
            }
        });


    }

    private float ratingSum = 0;
    private void loadReviews(ModelShop modelShop, HolderShop holder) {
        String shopUid = modelShop.getUid();

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

                        holder.ratingBarRS.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }


    //view holder
    class HolderShop extends RecyclerView.ViewHolder{

        //ui views of row_shop.xml
        private ImageView shopIvRS, onlineIvRS, nextIvRS;
        private TextView shopClosedTVRS,shopNameTvRS, phoneTvRS, addressTvRS;
        private RatingBar ratingBarRS;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            shopIvRS = itemView.findViewById(R.id.shopIvRS);
            onlineIvRS = itemView.findViewById(R.id.onlineIvRS);
            nextIvRS = itemView.findViewById(R.id.nextIvRS);
            shopClosedTVRS = itemView.findViewById(R.id.shopClosedTVRS);
            shopNameTvRS = itemView.findViewById(R.id.shopNameTvRS);
            phoneTvRS = itemView.findViewById(R.id.phoneTvRS);
            addressTvRS = itemView.findViewById(R.id.addressTvRS);
            ratingBarRS = itemView.findViewById(R.id.ratingBarRS);



        }
    }
}
