package com.example.mygrocery.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.example.mygrocery.Activities.ShopDetailsActivity;
import com.example.mygrocery.FilterProducts;
import com.example.mygrocery.FilterProductsUser;
import com.example.mygrocery.Models.ModelProduct;
import com.example.mygrocery.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList, filterList;
    private FilterProductsUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_users.xml)
        View view= LayoutInflater.from(context).inflate(R.layout.row_products_buyer, parent, false);

        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {

        //get data
        ModelProduct modelProduct = productsList.get(position);
        String id=modelProduct.getProductId();
        String uid=modelProduct.getUid();
        String discountAvailable=modelProduct.getDiscountAvailable();
        String discountNote=modelProduct.getDiscountNote();
        String discountPrice=modelProduct.getDiscountPrice();
        String productCategory=modelProduct.getProductCategory();
        String productDescription=modelProduct.getProductDescription();
        String productIcon=modelProduct.getProductIcon();
        String quantity=modelProduct.getProductQuantity();
        String title=modelProduct.getProductTitle();
        String timestamp=modelProduct.getTimeStamp();
        String originalPrice=modelProduct.getOriginalPrice();

        double originalPriceDouble = Double.parseDouble(originalPrice);
        double discountPriceDouble = Double.parseDouble(discountPrice);
        double discountedPriceDouble = originalPriceDouble - discountPriceDouble;
        String discountedPriceString = Double.toString(discountedPriceDouble);

        //set data
        holder.titleTvRB.setText(title);
        holder.descriptionTvRB.setText(productDescription);
        holder.discountedNoteTvRB.setText(discountNote);
        holder.discountedPriceTvRB.setText(""+discountedPriceString);
        holder.originalPriceTvRB.setText(""+originalPrice);

        if (discountAvailable.equals("true")){
            //product is on discount
            holder.discountedPriceTvRB.setVisibility(View.VISIBLE);
            holder.discountedNoteTvRB.setVisibility(View.VISIBLE);
            holder.originalPriceTvRB.setPaintFlags(holder.originalPriceTvRB.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add flag through original price
            holder.ksh2RB.setPaintFlags(holder.ksh2RB.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            //product is not on discount
            holder.discountedPriceTvRB.setVisibility(View.GONE);
            holder.discountedNoteTvRB.setVisibility(View.GONE);
            holder.originalPriceTvRB.setPaintFlags(0);
        }

        try {
            Picasso.get().load(productIcon)
                    .placeholder(R.drawable.ic_baseline_add_shopping_cart_24_green)
                    .into(holder.productIconIvRB);
        }
        catch (Exception e){
            holder.productIconIvRB.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24_green);
        }

        holder.addToCartTvRB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add product to cart
                showQuantityDialog(modelProduct);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show product details

            }
        });

    }

    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 0;
    private void showQuantityDialog(ModelProduct modelProduct) {
        //inflate layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);
        ImageView productIvPQ;
        TextView titleTvPQ, pQuantityTvPQ, descriptionTvPQ, discountNoteTvPQ, originalPriceTvPQ, discountedPriceTvPQ, finalPriceTvPQ, quantityTvQD, decrementTvQD, incrementTvQD;
        Button continueBtnBQ;

        productIvPQ = view.findViewById(R.id.productIvPQ);
        titleTvPQ = view.findViewById(R.id.titleTvPQ);
        pQuantityTvPQ = view.findViewById(R.id.pQuantityTvPQ);
        descriptionTvPQ = view.findViewById(R.id.descriptionTvPQ);
        discountNoteTvPQ = view.findViewById(R.id.discountNoteTvPQ);
        originalPriceTvPQ = view.findViewById(R.id.originalPriceTvPQ);
        discountedPriceTvPQ = view.findViewById(R.id.discountedPriceTvPQ);
        finalPriceTvPQ = view.findViewById(R.id.finalPriceTvPQ);
        quantityTvQD = view.findViewById(R.id.quantityTvQD);
        decrementTvQD= view.findViewById(R.id.decrementTvQD);
        incrementTvQD = view.findViewById(R.id.incrementTvQD);
        continueBtnBQ = view.findViewById(R.id.continueBtnBQ);

        //get data from model
        String productId=modelProduct.getProductId();
        String discountNote=modelProduct.getDiscountNote();
        String discountPrice=modelProduct.getDiscountPrice();
        String description=modelProduct.getProductDescription();
        String image=modelProduct.getProductIcon();
        String productQuantity=modelProduct.getProductQuantity();
        String title=modelProduct.getProductTitle();
        String originalPrice=modelProduct.getOriginalPrice();

        double originalPriceDouble = Double.parseDouble(originalPrice);
        double discountPriceDouble = Double.parseDouble(discountPrice);
        double discountedPriceDouble = originalPriceDouble - discountPriceDouble;
        String discountedPriceString = Double.toString(discountedPriceDouble);

        String price;
        if (modelProduct.getDiscountAvailable().equals("true")){
            //product has discount
            //price = modelProduct.getDiscountPrice();
            price = discountedPriceString;
            discountNoteTvPQ.setVisibility(View.VISIBLE);
            originalPriceTvPQ.setPaintFlags(originalPriceTvPQ.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add flag through original price

        }
        else {
            //product doesnt have discount
            discountNoteTvPQ.setVisibility(View.GONE);
            discountedPriceTvPQ.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }

        cost = Double.parseDouble(price.replaceAll("Ksh", ""));
        finalCost = Double.parseDouble(price.replaceAll("Ksh", ""));
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        //set data
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_shopping_cart_gray).into(productIvPQ);
        }
        catch (Exception e){
            productIvPQ.setImageResource(R.drawable.ic_shopping_cart_gray);
        }

        titleTvPQ.setText(""+title);
        pQuantityTvPQ.setText(""+productQuantity);
        descriptionTvPQ.setText(""+description);
        discountNoteTvPQ.setText(""+discountNote);
        quantityTvQD.setText(""+quantity);
        originalPriceTvPQ.setText("Ksh"+modelProduct.getOriginalPrice());
        discountedPriceTvPQ.setText("Ksh"+discountedPriceString);
        finalPriceTvPQ.setText("Ksh"+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        //increment quantity of the product
        incrementTvQD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost=finalCost+cost;
                quantity++;

                finalPriceTvPQ.setText("Ksh"+finalCost);
                quantityTvQD.setText(""+quantity);
            }
        });

        //decrement quantity of the product only if the quantity > 1
        decrementTvQD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (quantity>1){
                   finalCost = finalCost - cost;
                   quantity--;

                   finalPriceTvPQ.setText("Ksh"+finalCost);
                   quantityTvQD.setText(""+quantity);
               }
            }
        });

        continueBtnBQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleTvPQ.getText().toString().trim();
                String priceEach = price;
                String totalPrice = finalPriceTvPQ.getText().toString().trim().replace("Ksh", "");
                String quantity = quantityTvQD.getText().toString().trim();

                addToCart(productId, title, priceEach, totalPrice , quantity);
                
                dialog.dismiss();
            }
        });


    }

    private int itemId = 1;
    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;
        EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id", itemId)
                .addData("Item_PID", productId)
                .addData("Item_Name", title)
                .addData("Item_Price_Each", priceEach)
                .addData("Item_Price", price)
                .addData("Item_Quantity", quantity)
                .doneDataAdding();
        Toast.makeText(context, "Added to cart Successfully", Toast.LENGTH_SHORT).show();

        //update cart count
        /**
        ((ShopDetailsActivity)context).cartCount();
         **/
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProductsUser(this, filterList);
        }

        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder {


        ImageView productIconIvRB, nextIvRB  ;
        TextView discountedNoteTvRB, titleTvRB, descriptionTvRB,addToCartTvRB, ksh1RB, discountedPriceTvRB, ksh2RB, originalPriceTvRB  ;


        public HolderProductUser(@NonNull View itemView) {
            super(itemView);


            productIconIvRB = itemView.findViewById(R.id.productIconIvRB);
            nextIvRB = itemView.findViewById(R.id.nextIvRB);
            discountedNoteTvRB = itemView.findViewById(R.id.discountedNoteTvRB);
            titleTvRB = itemView.findViewById(R.id.titleTvRB);
            discountedPriceTvRB = itemView.findViewById(R.id.discountedPriceTvRB);
            originalPriceTvRB = itemView.findViewById(R.id.originalPriceTvRB);
            ksh2RB = itemView.findViewById(R.id.ksh2RB);
            descriptionTvRB = itemView.findViewById(R.id.descriptionTvRB);
            addToCartTvRB = itemView.findViewById(R.id.addToCartTvRB);
            ksh1RB = itemView.findViewById(R.id.ksh1RB);
        }
    }
}
