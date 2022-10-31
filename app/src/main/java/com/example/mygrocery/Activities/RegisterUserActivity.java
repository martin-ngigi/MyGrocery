package com.example.mygrocery.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygrocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterUserActivity extends AppCompatActivity implements LocationListener {

    ImageButton backBtnRU, gpsBtnRU;
    ImageView profileIvRU;
    TextView registerSellerBtn;
    EditText nameEtRU, phoneEtRU, countryEtRU, stateEtRU, cityEtRU, addressEtRU, emailEtRU, passwordETRU, confirmPasswordETRU;
    Button registerBtnRU;

    private String fullName, phoneNumber, country, state, city, address, email, password, confirmPassword;

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //IMAGE PICK CONSTANT
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;
    //image picked Uri
    private Uri image_uri;

    private double latitude, longitude;

    private LocationManager locationManager;

    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //declare an instance of firebase
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        backBtnRU = findViewById(R.id.backBtnRU);
        gpsBtnRU = findViewById(R.id.gpsBtnRU);
        profileIvRU = findViewById(R.id.profileIvRU);
        registerSellerBtn = findViewById(R.id.registerSellerBtn);
        nameEtRU = findViewById(R.id.nameEtRU);
        phoneEtRU = findViewById(R.id.phoneEtRU);
        countryEtRU = findViewById(R.id.countryEtRU);
        stateEtRU = findViewById(R.id.stateEtRU);
        cityEtRU = findViewById(R.id.cityEtRU);
        addressEtRU = findViewById(R.id.addressEtRU);
        emailEtRU = findViewById(R.id.emailEtRU);
        passwordETRU = findViewById(R.id.passwordETRU);
        confirmPasswordETRU = findViewById(R.id.confirmPasswordETRU);
        registerBtnRU = findViewById(R.id.registerBtnRU);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait... ");
        progressDialog.setCancelable(false);

        //init permission array
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        backBtnRU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gpsBtnRU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect current location
                //detect current location
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                } else {
                    //not allowed
                    requestLocationPermission();
                }
            }
        });

        profileIvRU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick image
                showImagePickDialog();
            }
        });

        registerBtnRU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //register user
                inputData();
            }
        });

        registerSellerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open register seller activity
                startActivity(new Intent(RegisterUserActivity.this, RegisterSellerActivity.class));
                finish();
            }
        });
    }

    private void inputData() {
        //input data
        fullName = nameEtRU.getText().toString().trim();
        phoneNumber = phoneEtRU.getText().toString().trim();
        country = countryEtRU.getText().toString().trim();
        state = stateEtRU.getText().toString().trim();
        city = cityEtRU.getText().toString().trim();
        address = addressEtRU.getText().toString().trim();
        email = emailEtRU.getText().toString().trim();
        password = passwordETRU.getText().toString().trim();
        confirmPassword = confirmPasswordETRU.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            nameEtRU.setError("Please enter name");
            nameEtRU.setFocusable(true);
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneEtRU.setError("Please enter phoneNumber");
            phoneEtRU.setFocusable(true);
            Toast.makeText(this, "Please enter phoneNumber", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(this, "Please click GPS button located at the RIGHT TOP MOST", Toast.LENGTH_SHORT).show();
            return;
        }
        //validate
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //set error and focus to email edit
            emailEtRU.setError("Invalid Email");
            emailEtRU.setFocusable(true);
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();

            return;
        }
        if (password.length() < 6) {
            //set error and focus to password edit
            passwordETRU.setError("Password length at least 6 characters");
            passwordETRU.setFocusable(true);
            Toast.makeText(this, "Password length at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordETRU.setError("Passwords don't match");
            confirmPasswordETRU.setFocusable(true);
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return;
        }

        createAccount();


    }

    private void createAccount() {
        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //created account
                        saveFirebaseData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed creating account
                        progressDialog.dismiss();
                        Toast.makeText(RegisterUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFirebaseData() {
        progressDialog.setMessage("Saving account information");
        String timestamp = "" + System.currentTimeMillis();

        if (image_uri == null) {
            //save infor without image

            //setup data to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("email", email);
            hashMap.put("name", fullName);
            hashMap.put("phone", phoneNumber);
            hashMap.put("country", country);
            hashMap.put("state", state);
            hashMap.put("city", city);
            hashMap.put("address", address);
            hashMap.put("latitude", Double.toString(latitude));
            hashMap.put("longitude", Double.toString(longitude));
            hashMap.put("timestamp", timestamp);
            hashMap.put("accountType", "User");
            hashMap.put("online", "true");
            hashMap.put("profileImage", "");

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //db updated
                            progressDialog.dismiss();
                            startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed updating db
                            progressDialog.dismiss();
                            Toast.makeText(RegisterUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            //save info with image

            //name and path of image
            String filePathAndName = "profile_images/" + firebaseAuth.getUid();
            //upload image

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get uri of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {

                                //setup data to save
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", "" + firebaseAuth.getUid());
                                hashMap.put("email", email);
                                hashMap.put("name", fullName);
                                hashMap.put("phone", phoneNumber);
                                hashMap.put("country", country);
                                hashMap.put("state", state);
                                hashMap.put("city", city);
                                hashMap.put("address", address);
                                hashMap.put("latitude", latitude);
                                hashMap.put("longitude", longitude);
                                hashMap.put("timestamp", timestamp);
                                hashMap.put("accountType", "User");
                                hashMap.put("online", "true");
                                hashMap.put("profileImage", "" + downloadImageUri); //uri of uploaded image

                                //save to db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //db updated
                                                progressDialog.dismiss();
                                                startActivity(new Intent(RegisterUserActivity.this, MainUserActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed updating db
                                                progressDialog.dismiss();
                                                Toast.makeText(RegisterUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });


                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(RegisterUserActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showImagePickDialog() {
        //options to display in dialog
        String[] options = {"Camera ", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle clicks
                        if (which == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {
                                //camera permission allowed
                                pickFromCamera();
                            } else {
                                //camera permission not allowed
                                requestCameraPermission();
                            }
                        } else {
                            //gallery clicked
                            if (checkStoragePermission()) {
                                //storage permission allowed
                                pickFromGallery();

                            } else {
                                //storage permission not allowed
                                requestStoragePermission();
                            }

                        }
                    }
                }).show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void findAddress() {
        //find address, city, state
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0); //complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set addresses
            countryEtRU.setText(country);
            stateEtRU.setText(state);
            cityEtRU.setText(city);
            addressEtRU.setText(address);


        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private  boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
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
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //gps/location disabled
        Toast.makeText(this, "Please turn on location...", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        //permission accepted
                        detectLocation();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this, "location permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

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
                profileIvRU.setImageURI(image_uri);
                Toast.makeText(this, "IMAGE SET SUCCESSFULLY", Toast.LENGTH_SHORT).show();

            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //set to image view
                profileIvRU.setImageURI(image_uri);
                Toast.makeText(this, "IMAGE SET SUCCESSFULLY", Toast.LENGTH_SHORT).show();

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}