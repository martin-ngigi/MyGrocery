package com.example.mygrocery.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygrocery.Constants;
import com.example.mygrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;

public class AddProductActivity extends AppCompatActivity {

    private ImageButton backBtnAP;
    private ImageView productIconIvAP;
    private EditText titleEtAP, descriptionEtAP, quantityEtAP, priceEtAP, discountPriceEtAP, discountNoteEtAP;
    private TextView categoryAP;
    private SwitchCompat discountSwitch;
    private Button addProductBtnAP;

    //permission constants
    private static  final int CAMERA_REQUEST_CODE=200;
    private static  final int STORAGE_REQUEST_CODE=300;

    //IMAGE PICK CONSTANT
    private static  final int IMAGE_PICK_GALLERY_CODE=400;
    private static  final int IMAGE_PICK_CAMERA_CODE=500;

    //permission arrays
    private String[] cameraPermission;
    private String[] storagePermission;
    //image picked Uri
    private Uri image_uri;

    //progressbar to display while registering user
    AlertDialog dialog;

    //declare an instance of firebase
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        backBtnAP = findViewById(R.id.backBtnAP);
        productIconIvAP = findViewById(R.id.productIconIvAP);
        titleEtAP = findViewById(R.id.titleEtAP);
        descriptionEtAP = findViewById(R.id.descriptionEtAP);
        categoryAP = findViewById(R.id.categoryAP);
        quantityEtAP = findViewById(R.id.quantityEtAP);
        priceEtAP = findViewById(R.id.priceEtAP);
        discountPriceEtAP = findViewById(R.id.discountPriceEtAP);
        discountNoteEtAP = findViewById(R.id.discountNoteEtAP);
        discountSwitch = findViewById(R.id.discountSwitch);
        addProductBtnAP = findViewById(R.id.addProductBtnAP);

        //unchecked, hide discountPriceEt, discountNote
        discountPriceEtAP.setVisibility(View.GONE);
        discountNoteEtAP.setVisibility(View.GONE);

        //init permission array
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setTitle("Please wait ");

        //if discountSwitch btn is checked, show discountPriceEt, discountNote
        //if discountSwitch btn is checked, hide discountPriceEt, discountNote
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //checked,show discountPriceEt, discountNote
                    discountPriceEtAP.setVisibility(View.VISIBLE);
                    discountNoteEtAP.setVisibility(View.VISIBLE);
                }
                else {
                    //unchecked, hide discountPriceEt, discountNote
                    discountPriceEtAP.setVisibility(View.GONE);
                    discountNoteEtAP.setVisibility(View.GONE);
                }
            }
        });

        backBtnAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productIconIvAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });
        
        categoryAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });

        addProductBtnAP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow
                // 1 . input data
                // 2. validate data
                // 3. add data to db
                inputData();
            }
        });
    }

    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailableSwitch = false;
    private void inputData() {
        // 1 . input data
        productTitle = titleEtAP.getText().toString().trim();
        productDescription = descriptionEtAP.getText().toString().trim();
        productCategory = categoryAP.getText().toString().trim();
        productQuantity = quantityEtAP.getText().toString().trim();
        originalPrice = priceEtAP.getText().toString().trim();
        discountAvailableSwitch = discountSwitch.isChecked(); //true or false

        // 2. validate data
        if (TextUtils.isEmpty(productTitle)){
            titleEtAP.setError("Please enter product title");
            titleEtAP.setFocusable(true);
            Toast.makeText(this, "Please product title", Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }
        if (TextUtils.isEmpty(productDescription)){
            descriptionEtAP.setError("Please enter product description");
            descriptionEtAP.setFocusable(true);
            Toast.makeText(this, "Please product description", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        if (TextUtils.isEmpty(productCategory)){
            categoryAP.setError("Please enter product category");
            categoryAP.setFocusable(true);
            Toast.makeText(this, "Please product category", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        if (TextUtils.isEmpty(productQuantity)){
            quantityEtAP.setError("Please enter product quantity");
            quantityEtAP.setFocusable(true);
            Toast.makeText(this, "Please product quantity", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        if (TextUtils.isEmpty(originalPrice)){
            priceEtAP.setError("Please enter product price");
            priceEtAP.setFocusable(true);
            Toast.makeText(this, "Please enter product prce", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        if (discountAvailableSwitch){
            //product is with discount
            discountPrice = discountPriceEtAP.getText().toString().trim();
            discountNote = discountNoteEtAP.getText().toString().trim();

            if (TextUtils.isEmpty(discountPrice)){
                discountPriceEtAP.setError("Please enter discount Price ");
                discountPriceEtAP.setFocusable(true);
                Toast.makeText(this, "Please enter discount Price", Toast.LENGTH_SHORT).show();
                return;//don't proceed further
            }
            if (TextUtils.isEmpty(discountNote)){
                discountNoteEtAP.setError("Please enter discount Note");
                discountNoteEtAP.setFocusable(true);
                Toast.makeText(this, "Please discount Note", Toast.LENGTH_SHORT).show();
                return;//don't proceed further
            }

        }
        else {
            //product is without discount
            discountPrice = "0";
            discountNote = "";
        }

        // 3. add data to db
        addProduct();

    }

    private void addProduct() {
        // 3. add data to db
        dialog.setMessage("Adding product...");
        dialog.show();

        String timeStamp = ""+System.currentTimeMillis();

        if (image_uri == null){
            //upload without Image

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId",""+timeStamp);
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("productIcon", ""); //no image, set empty
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountAvailable",""+discountAvailableSwitch);
            hashMap.put("timeStamp",""+timeStamp);
            hashMap.put("uid",""+firebaseAuth.getUid());


            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Products").child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //db updated
                            dialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed updating db
                            dialog.dismiss();
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
        else {
            //upload with Image

            //first upload image to db
            String filePathAndName = "product_images/"+""+timeStamp;
            //upload image
            StorageReference storageReference =  FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get uri of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productId",""+timeStamp);
                                hashMap.put("productTitle",""+productTitle);
                                hashMap.put("productDescription",""+productDescription);
                                hashMap.put("productCategory",""+productCategory);
                                hashMap.put("productQuantity",""+productQuantity);
                                hashMap.put("productIcon", ""+downloadImageUri); //set product image
                                hashMap.put("originalPrice",""+originalPrice);
                                hashMap.put("discountPrice",""+discountPrice);
                                hashMap.put("discountNote",""+discountNote);
                                hashMap.put("discountAvailable",""+discountAvailableSwitch);
                                hashMap.put("timeStamp",""+timeStamp);
                                hashMap.put("uid",""+firebaseAuth.getUid());


                                //save to db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).child("Products").child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //db updated
                                                dialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed updating db
                                                dialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            dialog.dismiss();
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearData() {
        //clear data after uploading product
        titleEtAP.setText("");
        descriptionEtAP.setText("");
        categoryAP.setText("");
        quantityEtAP.setText("");
        priceEtAP.setText("");
        discountPriceEtAP.setText("");
        discountNoteEtAP.setText("");
        productIconIvAP.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24_green);
        image_uri = null;
    }

    private void categoryDialog() {
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get picked category
                        String category = Constants.productCategories[which];

                        //set picked category
                        categoryAP.setText(category);
                    }
                })
                .show();
    }

    private void showImagePickDialog() {
        //options to display in dialog
        String[] options ={"Camera"," Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which == 0){
                            //camera clicked
                            if (checkCameraPermission()){
                                //camera permission allowed
                                pickFromCamera();
                            }
                            else {
                                //camera permission not allowed
                                requestCameraPermission();
                            }
                        }
                        else {
                            //gallery clicked
                            if (checkStoragePermission()){
                                //storage permission allowed
                                pickFromGallery();

                            }
                            else {
                                //storage permission not allowed
                                requestStoragePermission();
                            }

                        }
                    }
                }).show();
    }

    private void pickFromGallery(){
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        //intent to pick image from camera

        //using media store ro pick high/original quality image
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);


        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted= grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //permission accepted
                        pickFromCamera();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Camera permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission accepted
                        pickFromGallery();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //get picked image
                image_uri = data.getData();
                //set to image view
                productIconIvAP.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //set to image view
                productIconIvAP.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}