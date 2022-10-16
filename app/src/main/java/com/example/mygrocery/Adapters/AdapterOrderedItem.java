package com.example.mygrocery.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.Models.ModelOrderedItem;
import com.example.mygrocery.R;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem>{

    private Context context;
    private ArrayList<ModelOrderedItem> orderedItemsList;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderedItem> orderedItemsList) {
        this.context = context;
        this.orderedItemsList = orderedItemsList;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate row_ordered_item.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordered_item, parent, false);

        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {
        //get data
        ModelOrderedItem modelOrderedItem = orderedItemsList.get(position);
        String pId = modelOrderedItem.getpId();
        String name = modelOrderedItem.getName();
        String cost = modelOrderedItem.getCost();
        String price = modelOrderedItem.getPrice();
        String quantity = modelOrderedItem.getQuantity();

        //set data
        holder.itemTitleTvRO.setText(""+name);
        holder.itemPriceEachTvRO.setText("Ksh"+price);
        holder.itemPriceTvRO.setText("Ksh"+cost);
        holder.itemQuantityTvRO.setText("["+quantity+"]");
    }

    @Override
    public int getItemCount() {
        return orderedItemsList.size();
    }

    class HolderOrderedItem extends RecyclerView.ViewHolder {

        //UI views of row_ordered_item.xml
        TextView itemTitleTvRO, itemPriceTvRO, itemPriceEachTvRO, itemQuantityTvRO;

        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);

            //init uis
            itemTitleTvRO = itemView.findViewById(R.id.itemTitleTvRO);
            itemPriceTvRO = itemView.findViewById(R.id.itemPriceTvRO);
            itemPriceEachTvRO = itemView.findViewById(R.id.itemPriceEachTvRO);
            itemQuantityTvRO = itemView.findViewById(R.id.itemQuantityTvRO);

        }
    }
}
