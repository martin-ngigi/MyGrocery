package com.example.mygrocery.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.example.mygrocery.Activities.OrderDetailsSellerActivity;
import com.example.mygrocery.FilterOrderShop;
import com.example.mygrocery.Models.ModelOrderShop;
import com.example.mygrocery.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOderShop> implements Filterable {

    private Context context;
    public ArrayList<ModelOrderShop> orderShopList, filterList;
    private FilterOrderShop filter;

    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> orderShopList) {
        this.context = context;
        this.orderShopList = orderShopList;
        this.filterList = orderShopList;
    }

    @NonNull
    @Override
    public HolderOderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate Layout row_order_user.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller, parent, false);

        return new HolderOderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOderShop holder, int position) {
        //get data
        ModelOrderShop modelOrderShop = orderShopList.get(position);
        //orderId, orderTime, orderStatus, orderCost, orderBy, orderTo;
        final String orderId = modelOrderShop.getOrderId();
        String orderTime = modelOrderShop.getOrderTime();
        String orderStatus = modelOrderShop.getOrderStatus();
        String orderCost = modelOrderShop.getOrderCost();
        final String orderBy = modelOrderShop.getOrderBy();
        String orderTo = modelOrderShop.getOrderTo();

        //get User/Buyer info
        loadUserInfo(modelOrderShop, holder); ////////////---------------------------------xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

        //set data
        holder.orderIdTvS.setText("OrderID: "+orderId);
        holder.amountTvS.setText("Amount: Ksh"+orderCost);
        holder.statusTvS.setText(""+orderStatus);
        //change order status color
        if (orderStatus.equals("In Progress")){
            holder.statusTvS.setTextColor(context.getResources().getColor(R.color.blue));
        }
        else if (orderStatus.equals("Completed")){
            holder.statusTvS.setTextColor(context.getResources().getColor(R.color.background));
        }
        else if (orderStatus.equals("Cancelled")){
            holder.statusTvS.setTextColor(context.getResources().getColor(R.color.red));
        }

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString(); //eg 16/06/2021

        holder.orderDateTvS.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Order Details Activity, we need two keys: orderTo,orderId
                /**
                Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderBy", orderBy);
                context.startActivity(intent);//after passing values to OrderDetails, get values using intent
                 **/
            }
        });

    }

    private void loadUserInfo(ModelOrderShop modelOrderUser, HolderOderShop holder) {

        //load email of the user/buyer ... modelOrderUser.getOrderBy() contains the uid of the buyer/user
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderUser.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("email").getValue();
                        holder.emailTvS.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderShopList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            //init filter
            filter = new FilterOrderShop(this, filterList);
        }
        return filter;
    }


    class HolderOderShop extends RecyclerView.ViewHolder{

        //ui views
        TextView orderIdTvS, orderDateTvS, emailTvS, amountTvS, statusTvS;

        public HolderOderShop(@NonNull View itemView) {
            super(itemView);

            orderIdTvS = itemView.findViewById(R.id.orderIdTvS);
            orderDateTvS = itemView.findViewById(R.id.orderDateTvS);
            emailTvS = itemView.findViewById(R.id.emailTvS);
            amountTvS = itemView.findViewById(R.id.amountTvS);
            statusTvS = itemView.findViewById(R.id.statusTvS);
        }
    }
}
