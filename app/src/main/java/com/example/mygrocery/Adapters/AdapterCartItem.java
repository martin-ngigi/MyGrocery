package com.example.mygrocery.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.Activities.ShopDetailsActivity;
import com.example.mygrocery.Models.ModelCartItem;
import com.example.mygrocery.R;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> modelCartItems) {
        this.context = context;
        this.cartItems = modelCartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_cart_items.xml)
        View view= LayoutInflater.from(context).inflate(R.layout.row_cart_items, parent, false);

        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {
        //get data
        ModelCartItem modelCartItem = cartItems.get(position);
        String id = modelCartItem.getId();
        String title = modelCartItem.getName();
        String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        //set data
        holder.itemTitleTvRC.setText(""+title);
        holder.itemPriceTvRC.setText(""+cost);
        holder.itemQuantityTvRC.setText("["+quantity+"]");
        holder.itemPriceEachTvRC.setText(""+price);

        //handle remove click listener, delete items from cart
        holder.itemRemoveTvRC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will create table if it doesnt exist, but in that case it must exist
                EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                        .doneTableColumn();
                
                easyDB.deleteRow(1,id);
                Toast.makeText(context, "Removed from cart", Toast.LENGTH_SHORT).show();

                //refresh list
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double tx = Double.parseDouble((((ShopDetailsActivity)context).allTotalPriceTvDC.getText().toString().trim().replace("Ksh","")));
                double totalPrice = tx - Double.parseDouble(cost.replace("Ksh", ""));
                double deliveryFee = Double.parseDouble((((ShopDetailsActivity)context).deliveryFee.replace("Ksh","")));
                double subTotal = Double.parseDouble(String.format("%.2f", totalPrice)) -  Double.parseDouble(String.format("%.2f", deliveryFee));
                ((ShopDetailsActivity)context).allTotalPrice = 0.00;
                ((ShopDetailsActivity)context).subTotalTvDC.setText("Ksh"+String.format("%.2f", subTotal));
                ((ShopDetailsActivity)context).allTotalPriceTvDC.setText("Ksh"+String.format("%.2f", Double.parseDouble(String.format("%.2f", totalPrice))));

                //after removing from cart, update cart count
                ((ShopDetailsActivity)context).cartCount();

            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class HolderCartItem extends RecyclerView.ViewHolder{

        TextView itemTitleTvRC, itemPriceTvRC, itemPriceEachTvRC, itemQuantityTvRC, itemRemoveTvRC;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTvRC = itemView.findViewById(R.id.itemTitleTvRC);
            itemPriceTvRC = itemView.findViewById(R.id.itemPriceTvRC);
            itemPriceEachTvRC = itemView.findViewById(R.id.itemPriceEachTvRC);
            itemQuantityTvRC = itemView.findViewById(R.id.itemQuantityTvRC);
            itemTitleTvRC = itemView.findViewById(R.id.itemTitleTvRC);
            itemRemoveTvRC = itemView.findViewById(R.id.itemRemoveTvRC);

        }
    }
}
