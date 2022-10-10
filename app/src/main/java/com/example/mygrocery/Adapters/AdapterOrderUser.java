package com.example.mygrocery.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**import com.example.mygrocery.Activities.OrderDetailsUserActivity;**/
import com.example.mygrocery.Models.ModelOrderUser;
import com.example.mygrocery.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOderUser> {

    Context context;
    ArrayList<ModelOrderUser> orderUsersList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUsersList) {
        this.context = context;
        this.orderUsersList = orderUsersList;
    }

    @NonNull
    @Override
    public HolderOderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate Layout row_order_user.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user, parent, false);

        return new HolderOderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOderUser holder, int position) {
        //get data
        ModelOrderUser modelOrderUser = orderUsersList.get(position);
        //orderId, orderTime, orderStatus, orderCost, orderBy, orderTo;
        String orderId = modelOrderUser.getOrderId();
        String orderTime = modelOrderUser.getOrderTime();
        String orderStatus = modelOrderUser.getOrderStatus();
        String orderCost = modelOrderUser.getOrderCost();
        String orderBy = modelOrderUser.getOrderBy();
        String orderTo = modelOrderUser.getOrderTo();

        //get shop info
        loadShopInfo(modelOrderUser, holder);

        //set data
        holder.orderIdTv.setText("OrderID: "+orderId);
        holder.amountTv.setText("Amount: Ksh"+orderCost);
        holder.statusTv.setText(""+orderStatus);
        //change order status color
        if (orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.blue));
        }
        else if (orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.background));
        }
        else if (orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.red));
        }

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString(); //eg 16/06/2021

        holder.dateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Order Details Activity, we need two keys: orderTo,orderId
                /**
                Intent intent = new Intent(context, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", orderTo);
                intent.putExtra("orderId", orderId);
                context.startActivity(intent);//after passing values to OrderDetails, get values using
                 **/
            }
        });

    }

    private void loadShopInfo(ModelOrderUser modelOrderUser, HolderOderUser holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        holder.shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUsersList.size();
    }


    class HolderOderUser extends RecyclerView.ViewHolder{

        //ui views
        TextView orderIdTv, dateTv, shopNameTv, amountTv, statusTv;

        public HolderOderUser(@NonNull View itemView) {
            super(itemView);

            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
