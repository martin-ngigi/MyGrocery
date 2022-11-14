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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;

public class EditProductActivity extends AppCompatActivity {

    private String productId;

    /**
     * Declare the views
     */
    private ImageButton backBtnEp;
    private ImageView productIconIvEP;
    private EditText titleEtEP, descriptionEtEP, quantityEtEP, priceEtEP, discountPriceEtEP, discountNoteEtEP;
    private TextView categoryEP;
    private SwitchCompat discountSwitchEP;
    private Button updateProductBtnEP;

    /**
     * permission constants
     */
    private static  final int CAMERA_REQUEST_CODE=200;
    private static  final int STORAGE_REQUEST_CODE=300;

    /**
     * IMAGE PICK CONSTANT
     */
    private static  final int IMAGE_PICK_GALLERY_CODE=400;
    private static  final int IMAGE_PICK_CAMERA_CODE=500;

    /**
     * permission arrays
     */
    private String[] cameraPermission;
    private String[] storagePermission;
    /**
     * image picked Uri
     */
    private Uri image_uri;

    /**
     * progressbar to display while registering user
     */
    AlertDialog dialog;

    /**
     * declare an instance of firebase
     */
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        /**
         * get id from intent
         */
        productId = getIntent().getStringExtra("productId");


        /**
         * initialize the views
         */
        backBtnEp = findViewById(R.id.backBtnEP);
        productIconIvEP = findViewById(R.id.productIconIvEP);
        titleEtEP = findViewById(R.id.titleEtEP);
        descriptionEtEP = findViewById(R.id.descriptionEtEP);
        categoryEP = findViewById(R.id.categoryEP);
        quantityEtEP = findViewById(R.id.quantityEtEP);
        priceEtEP = findViewById(R.id.priceEtEP);
        discountPriceEtEP = findViewById(R.id.discountPriceEtEP);
        discountNoteEtEP = findViewById(R.id.discountNoteEtEP);
        discountSwitchEP = findViewById(R.id.discountSwitchEP);
        updateProductBtnEP = findViewById(R.id.updateProductBtnEP);

        /**
         * unchecked, hide discountPriceEt, discountNote
         */
        discountPriceEtEP.setVisibility(View.GONE);
        discountNoteEtEP.setVisibility(View.GONE);

        /**
         * init permission array
         */
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};

        /**
         *  Initialize Firebase Auth
         */
        firebaseAuth = FirebaseAuth.getInstance();
        loadProductDetails(); // to set on views

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        dialog.setTitle("Please wait ");

        /**
         * if discountSwitch btn is checked, show discountPriceEt, discountNote
         * if discountSwitch btn is checked, hide discountPriceEt, discountNote
         */
        discountSwitchEP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    /**
                     * checked,show discountPriceEt, discountNote
                     */
                    discountPriceEtEP.setVisibility(View.VISIBLE);
                    discountNoteEtEP.setVisibility(View.VISIBLE);
                }
                else {
                    /**
                     * unchecked, hide discountPriceEt, discountNote
                     */
                    discountPriceEtEP.setVisibility(View.GONE);
                    discountNoteEtEP.setVisibility(View.GONE);
                }
            }
        });

        /**
         * handle backBtnEp listener
         */
        backBtnEp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /**
         * handle productIconIvEP listener
         */
        productIconIvEP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        /**
         * handle categoryEP listener
         */
        categoryEP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });

        /**
         * handle updateProductBtnEP listener
         */
        updateProductBtnEP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow
                /**
                 * 1 . input data
                 *2. validate data
                 *  3. update data to db
                 */
                inputData();
            }
        });


    }

    /**
     * load products from database
     */
    private void loadProductDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /**
                         * get data
                         */
                        String productId = ""+snapshot.child("productId").getValue();
                        String productTitle = ""+snapshot.child("productTitle").getValue();
                        String productDescription = ""+snapshot.child("productDescription").getValue();
                        String productCategory = ""+snapshot.child("productCategory").getValue();
                        String productQuantity = ""+snapshot.child("productQuantity").getValue();
                        String productIcon = ""+snapshot.child("productIcon").getValue();
                        String originalPrice = ""+snapshot.child("originalPrice").getValue();
                        String discountPrice = ""+snapshot.child("discountPrice").getValue();
                        String discountNote = ""+snapshot.child("discountNote").getValue();
                        String discountAvailable = ""+snapshot.child("discountAvailable").getValue();
                        String timeStamp = ""+snapshot.child("timeStamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();

                        double originalPriceDouble = Double.parseDouble(originalPrice);
                        double discountPriceDouble = Double.parseDouble(discountPrice);
                        double discountedPriceDouble = originalPriceDouble - discountPriceDouble;

                        /**
                         * set data to views
                         */
                        if (discountAvailable.equals("true")){
                            discountSwitchEP.setChecked(true);

                            /**
                             * checked, show discountPriceEt, discountNote
                             */
                            discountPriceEtEP.setVisibility(View.VISIBLE);
                            discountNoteEtEP.setVisibility(View.VISIBLE);

                        }
                        else {
                            discountSwitchEP.setChecked(false);

                            /**
                             * checked, show discountPriceEt, discountNote
                             */
                            discountPriceEtEP.setVisibility(View.GONE);
                            discountNoteEtEP.setVisibility(View.GONE);
                        }

                        /**
                         * set data to UI
                         */
                        titleEtEP.setText(productTitle);
                        descriptionEtEP.setText(productDescription);
                        categoryEP.setText(productCategory);
                        discountNoteEtEP.setText(discountNote);
                        quantityEtEP.setText(productQuantity);
                        priceEtEP.setText(""+originalPrice);
                        discountPriceEtEP.setText(""+discountPrice);

                        try {
                            /**
                             * get image
                             */
                            Picasso.get().load(productIcon)
                                    .placeholder(R.drawable.ic_baseline_add_shopping_cart_24_green)
                                    .into(productIconIvEP);
                        }
                        catch (Exception e){
                            /**
                             * handle error
                             */
                            productIconIvEP.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24_green);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailableSwitch = false;
    private void inputData() {
        /**
         * 1 . input data
         */
        productTitle = titleEtEP.getText().toString().trim();
        productDescription = descriptionEtEP.getText().toString().trim();
        productCategory = categoryEP.getText().toString().trim();
        productQuantity = quantityEtEP.getText().toString().trim();
        originalPrice = priceEtEP.getText().toString().trim();
        discountAvailableSwitch = discountSwitchEP.isChecked(); //true or false

        /**
         *  2. validate data
         *  productTitle is empty
         */
        if (TextUtils.isEmpty(productTitle)){
            titleEtEP.setError("Please enter product title");
            titleEtEP.setFocusable(true);
            Toast.makeText(this, "Please product title", Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }
        /**
         *  descriptionEtEP is empty
         */
        if (TextUtils.isEmpty(productDescription)){
            descriptionEtEP.setError("Please enter product description");
            descriptionEtEP.setFocusable(true);
            Toast.makeText(this, "Please product description", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        /**
         *  categoryEP is empty
         */
        if (TextUtils.isEmpty(productCategory)){
            categoryEP.setError("Please enter product category");
            categoryEP.setFocusable(true);
            Toast.makeText(this, "Please product category", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        /**
         *  quantityEtEP is empty
         */
        if (TextUtils.isEmpty(productQuantity)){
            quantityEtEP.setError("Please enter product quantity");
            quantityEtEP.setFocusable(true);
            Toast.makeText(this, "Please product quantity", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        /**
         *  priceEtEP is empty
         */
        if (TextUtils.isEmpty(originalPrice)){
            priceEtEP.setError("Please enter product price");
            priceEtEP.setFocusable(true);
            Toast.makeText(this, "Please enter product prce", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        if (discountAvailableSwitch){
            /**
             * product is with discount
             */
            discountPrice = discountPriceEtEP.getText().toString().trim();
            discountNote = discountNoteEtEP.getText().toString().trim();

            if (TextUtils.isEmpty(discountPrice)){
                discountPriceEtEP.setError("Please enter discount Price ");
                discountPriceEtEP.setFocusable(true);
                Toast.makeText(this, "Please enter discount Price", Toast.LENGTH_SHORT).show();
                return;//don't proceed further
            }
            if (TextUtils.isEmpty(discountNote)){
                discountNoteEtEP.setError("Please enter discount Note");
                discountNoteEtEP.setFocusable(true);
                Toast.makeText(this, "Please discount Note", Toast.LENGTH_SHORT).show();
                return;//don't proceed further
            }

        }
        else {
            /**
             * product is without discount
             */
            discountPrice = "0";
            discountNote = "";
        }

        /**
         * 3. update data to db
         */
        updateProduct();

    }

    private void updateProduct() {

        /**
         * show dialog
         */
        dialog.setMessage("Adding product...");
        dialog.show();


        if (image_uri == null) {
            //upload without Image

            /**
             * set data in hashmap to update
             */
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("discountAvailable",""+discountAvailableSwitch);

            /**
             * save to db
             */
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Products").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            /**
                             * /successfully updated
                             */
                            dialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            /**
                             * failed to update
                             */
                            dialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "failed to update"+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }
        else {
            /**
             * /upload with Image
             *
             *first upload image to db
             */
            String filePathAndName = "product_images/"+""+productId;
            /**
             * upload image
             */
            StorageReference storageReference =  FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            /**
                             * get uri of uploaded image
                             */
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){

                                /**
                                 * set data in hashmap to update
                                 */
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productTitle",""+productTitle);
                                hashMap.put("productDescription",""+productDescription);
                                hashMap.put("productCategory",""+productCategory);
                                hashMap.put("productIcon", ""+downloadImageUri); //set product image
                                hashMap.put("productQuantity",""+productQuantity);
                                hashMap.put("originalPrice",""+originalPrice);
                                hashMap.put("discountPrice",""+discountPrice);
                                hashMap.put("discountNote",""+discountNote);
                                hashMap.put("discountAvailable",""+discountAvailableSwitch);

                                /**
                                 * /save to db
                                 */
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).child("Products").child(productId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                /**
                                                 * successfully updated
                                                 */
                                                dialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                /**
                                                 * failed to update
                                                 */
                                                dialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "failed to update"+e.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            /**
                             * failed to update
                             */
                            dialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "failed to update"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void categoryDialog() {
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /**
                         * get picked category
                         */
                        String category = Constants.productCategories[which];

                        /**
                         * set picked category
                         */
                        categoryEP.setText(category);
                    }
                })
                .show();
    }

    private void showImagePickDialog() {
        /**
         * options to display in dialog
         */
        String[] options ={"Camera"," Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /**
                         * handle clicks
                         */
                        if (which == 0){
                            /**
                             * camera clicked
                             */
                            if (checkCameraPermission()){
                                /**
                                 * camera permission allowed
                                 */
                                pickFromCamera();
                            }
                            else {
                                /**
                                 * camera permission not allowed
                                 */
                                requestCameraPermission();
                            }
                        }
                        else {
                            /**
                             * gallery clicked
                             */
                            if (checkStoragePermission()){
                                /**
                                 * storage permission allowed
                                 */
                                pickFromGallery();

                            }
                            else {
                                /**
                                 * storage permission not allowed
                                 */
                                requestStoragePermission();
                            }

                        }
                    }
                }).show();
    }

    private void pickFromGallery(){
        /**
         * intent to pick image from gallery
         */
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        /**
         * intent to pick image from camera
         *
         * using media store ro pick high/original quality image
         */
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    /**
     * check permissions
     * @return results
     */
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    /**
     * check permissions
     */
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    /**
     * check permissions
     * @return
     */
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);


        return result && result1;
    }

    /**
     * check permissions
     */
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    /**
     * check permissions results
     * @param requestCode check permissions results
     * @param permissions check permissions results
     * @param grantResults check permissions results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted= grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        /**
                         * permission accepted
                         */
                        pickFromCamera();
                    }
                    else {
                        /**
                         * permission denied
                         */
                        Toast.makeText(this, "Camera permissions are required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        /**
                         * permission accepted
                         */
                        pickFromGallery();
                    }
                    else {
                        /**
                         * permission denied
                         */
                        Toast.makeText(this, "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * check permissions results
     * @param requestCode check permissions results
     * @param resultCode check permissions results
     * @param data check permissions results
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                /**
                 * get picked image
                 */
                image_uri = data.getData();
                /**
                 * set to image view
                 */
                productIconIvEP.setImageURI(image_uri);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                /**
                 * set to image view
                 */
                productIconIvEP.setImageURI(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}