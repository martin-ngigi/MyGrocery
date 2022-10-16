package com.example.mygrocery.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mygrocery.Activities.EditProductActivity;
import com.example.mygrocery.FilterProducts;
import com.example.mygrocery.Models.ModelProduct;
import com.example.mygrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import dmax.dialog.SpotsDialog;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList, filterList;
    private FilterProducts filter;
    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_users.xml)
        View view= LayoutInflater.from(context).inflate(R.layout.row_products_seller, parent, false);

        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {

        //get data
        ModelProduct modelProduct = productsList.get(position);
        String id=modelProduct.getProductId();
        String uid=modelProduct.getUid();
        String discountAvailable=modelProduct.getDiscountAvailable();
        String discountNote=modelProduct.getDiscountNote();
        String discountPrice=modelProduct.getDiscountPrice();
        String productCategory=modelProduct.getProductCategory();
        String productDescription=modelProduct.getProductDescription();
        String icon=modelProduct.getProductIcon();
        String quantity=modelProduct.getProductQuantity();
        String title=modelProduct.getProductTitle();
        String timestamp=modelProduct.getTimeStamp();
        String originalPrice=modelProduct.getOriginalPrice();

        double originalPriceDouble = Double.parseDouble(originalPrice);
        double discountPriceDouble = Double.parseDouble(discountPrice);
        double discountedPriceDouble = originalPriceDouble - discountPriceDouble;
        String discountedPriceString = Double.toString(discountedPriceDouble);

        //set data
        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText(""+discountedPriceString);
        holder.originalPriceTv.setText(""+originalPrice);
        if (discountAvailable.equals("true")){
            //product is on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add flag through original price
            holder.ksh2.setPaintFlags(holder.ksh2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            //product is not on discount
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
        }

        try {
            Picasso.get().load(icon)
                    .placeholder(R.drawable.ic_baseline_add_shopping_cart_24_green)
                    .into(holder.productIconIvRP);
        }
        catch (Exception e){
            holder.productIconIvRP.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24_green);
        }

        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks, show details in bs_product_details
                detailBottomSheet(modelProduct);
            }
        });

    }

    private void detailBottomSheet(ModelProduct modelProduct) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view for bottom sheet
        View view= LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller, null);
        //set view to bottom sheet

        //set view to bottomsheet
        bottomSheetDialog.setContentView(view);

        //init view of bottom sheet
        ImageView productIconIvBSS;
        ImageButton backBtnBSS, deleteBtnBSS, editBtnBSS;
        TextView nameTvBSS, discountedNoteTvBSS, titleTv, descriptionTvBSS,categoryTvBSS, quantityTvBSS, discountedPriceTvBSS, originalPriceTvBSS, ksh2;

        productIconIvBSS = view.findViewById(R.id.productIconIvBSS);
        backBtnBSS = view.findViewById(R.id.backBtnBSS);
        deleteBtnBSS = view.findViewById(R.id.deleteBtnBSS);
        editBtnBSS = view.findViewById(R.id.editBtnBSS);
        nameTvBSS = view.findViewById(R.id.nameTvBSS);
        discountedNoteTvBSS = view.findViewById(R.id.discountedNoteTvBSS);
        titleTv = view.findViewById(R.id.titleTv);
        descriptionTvBSS = view.findViewById(R.id.descriptionTvBSS);
        categoryTvBSS = view.findViewById(R.id.categoryTvBSS);
        quantityTvBSS = view.findViewById(R.id.quantityTvBSS);
        discountedPriceTvBSS = view.findViewById(R.id.discountedPriceTvBSS);
        originalPriceTvBSS = view.findViewById(R.id.originalPriceTvBSS);
        ksh2 = view.findViewById(R.id.ksh2);


        //get data
        String id=modelProduct.getProductId();
        String uid=modelProduct.getUid();
        String discountAvailable=modelProduct.getDiscountAvailable();
        String discountNote=modelProduct.getDiscountNote();
        String discountPrice=modelProduct.getDiscountPrice();
        String productCategory=modelProduct.getProductCategory();
        String productDescription=modelProduct.getProductDescription();
        String icon=modelProduct.getProductIcon();
        String quantity=modelProduct.getProductQuantity();
        String title=modelProduct.getProductTitle();
        String timestamp=modelProduct.getTimeStamp();
        String originalPrice=modelProduct.getOriginalPrice();

        double originalPriceDouble = Double.parseDouble(originalPrice);
        double discountPriceDouble = Double.parseDouble(discountPrice);
        double discountedPriceDouble = originalPriceDouble - discountPriceDouble;

        //set data
        titleTv.setText(title);
        descriptionTvBSS.setText(productDescription);
        categoryTvBSS.setText(productCategory);
        quantityTvBSS.setText(quantity);
        discountedNoteTvBSS.setText(discountNote);
        discountedPriceTvBSS.setText(""+discountedPriceDouble);
        originalPriceTvBSS.setText(""+originalPrice);

        if (discountAvailable.equals("true")){
            //product is on discount
            discountedPriceTvBSS.setVisibility(View.VISIBLE);
            discountedNoteTvBSS.setVisibility(View.VISIBLE);
            originalPriceTvBSS.setPaintFlags(originalPriceTvBSS.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add flag through original price
            ksh2.setPaintFlags(ksh2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            //product is not on discount
            discountedPriceTvBSS.setVisibility(View.GONE);
            discountedNoteTvBSS.setVisibility(View.GONE);
        }

        try {
            Picasso.get().load(icon)
                    .placeholder(R.drawable.ic_baseline_add_shopping_cart_24_green)
                    .into(productIconIvBSS);
        }
        catch (Exception e){
            productIconIvBSS.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24_green);
        }

        //show dialog
        bottomSheetDialog.show();

        //edit click
        editBtnBSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit product activity
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", id); // pass productId to EditActivity
                context.startActivity(intent);
            }
        });

        //delete click
        deleteBtnBSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure to delete product "+title+" ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete
                                deleteProduct(id); //id is the product id
                                
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //CANCEL
                                dialog.dismiss();
                            }
                        }).show();

            }
        });

        //back click
        backBtnBSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bottom sheet dismiss
                bottomSheetDialog.dismiss();
            }
        });



    }

    private void deleteProduct(String id) {
        //delete product using its id ie {timestamp}
        //progressbar to display while registering user
        AlertDialog dialog;
        dialog = new SpotsDialog.Builder().setContext(context).setCancelable(false).build();
        dialog.setTitle("Please wait ");
        dialog.setMessage("deleting product...");
        dialog.show();


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //successfully deleted
                        dialog.dismiss();
                        Toast.makeText(context, "successfully deleted product", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to deleted
                        dialog.dismiss();
                        Toast.makeText(context, "failed to deleted product", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProducts(this, filterList);
        }

        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{

        private ImageView productIconIvRP, nextIv;
        private TextView discountedNoteTv,titleTv, quantityTv, discountedPriceTv, originalPriceTv, ksh2;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIconIvRP = itemView.findViewById(R.id.productIconIvRP);
            nextIv = itemView.findViewById(R.id.nextIv);
            discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
            ksh2 = itemView.findViewById(R.id.ksh2);

        }
    }
}
