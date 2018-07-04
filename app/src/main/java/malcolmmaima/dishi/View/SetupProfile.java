package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rey.material.widget.Switch;

import java.io.IOException;

import malcolmmaima.dishi.Model.DishiUser;
import malcolmmaima.dishi.Model.ImageUploadInfo;
import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.customfonts.EditText_Roboto_Regular;

//@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SetupProfile extends AppCompatActivity implements com.rey.material.widget.Spinner.OnItemSelectedListener, AdapterView.OnItemSelectedListener {

    Button logoutbutton, continueBtn, backButton;
    private ImageView profile_pic;

    EditText_Roboto_Regular userName, userBio, userEmail, changeNumber;
    RadioButton maleRd, femaleRd;
    RadioGroup gender;
    Spinner accType;
    Switch notifications;
    String myPhone;

    private FirebaseAuth mAuth;
    String account_type;

    // Folder path for Firebase Storage.
    String Storage_Path = "Users/";

    // Creating URI.
    Uri FilePathUri;

    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    DatabaseReference databaseReference;

    // Image request code for onActivityResult() .
    int Image_Request_Code = 7;

    ProgressDialog progressDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_setup);

        // Assigning Id to ProgressDialog.
        progressDialog = new ProgressDialog(SetupProfile.this);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getInstance().getCurrentUser() == null || mAuth.getInstance().getCurrentUser().getPhoneNumber() == null){
            //User is not signed in, send them back to verification page
            //Toast.makeText(this, "Not logged in!", Toast.LENGTH_LONG).show();
            //Slide to new activity
            Intent slideactivity = new Intent(SetupProfile.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Bundle bndlanimation =
                    ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
            startActivity(slideactivity, bndlanimation);

        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number

        // Assign FirebaseStorage instance to storageReference.
        storageReference = FirebaseStorage.getInstance().getReference();

        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(myPhone);

        //Our EditText Boxes
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.emailAddress);
        userBio = findViewById(R.id.userBio);
        changeNumber = findViewById(R.id.phoneNumber);

        //Profile pic
        profile_pic = findViewById(R.id.profilePic);

        //Our RadioButtons
        maleRd = findViewById(R.id.maleRd);
        femaleRd = findViewById(R.id.femaleRd);
        gender = findViewById(R.id.gender);

        //Our Account Type Spinner
        accType = findViewById(R.id.accType);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.accounts, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        accType.setAdapter(adapter);
        accType.setOnItemSelectedListener(this);

        //Our Notifications Switch
        notifications = findViewById(R.id.Notifications);

        continueBtn = findViewById(R.id.continueBtn);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SetupProfile.this,MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Creating intent.
                Intent intent = new Intent();

                // Setting intent type as image to select image from phone storage.
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Please Select Image"), Image_Request_Code);

            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UploadUserData();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            FilePathUri = data.getData();

            try {

                // Getting selected image into Bitmap.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri);

                // Setting up bitmap selected image into ImageView.
                profile_pic.setImageBitmap(bitmap);

            }
            catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    // Creating Method to get the selected image file Extension from File Path URI.
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }

    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    public void UploadUserData() {

        // Checking whether FilePathUri Is empty or not and text fields are empty
        if (FilePathUri != null && CheckFieldValidation()) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            myPhone = user.getPhoneNumber(); //Current logged in user phone number

            // Setting progressDialog Title.
            progressDialog.setTitle("Saving...");
            // Showing progressDialog.
            progressDialog.show();
            progressDialog.setCancelable(false);

            // Creating second StorageReference.
            final StorageReference storageReference2nd = storageReference.child(Storage_Path + "/" + myPhone + "/" + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //Get image URL: //Here we get the image url from the firebase storage
                            storageReference2nd.getDownloadUrl().addOnSuccessListener(new OnSuccessListener() {

                                @Override
                                public void onSuccess(Object o) {
                                    DishiUser dishiUser = new DishiUser();

                                    dishiUser.setProfilepic(o.toString());

                                    //Log.d("profile", "onSuccess: profile image: " + imageUploadInfo.getImageURL());

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    myPhone = user.getPhoneNumber(); //Current logged in user phone number

                                    String name = userName.getText().toString();
                                    String email = userEmail.getText().toString();
                                    String userbio = userBio.getText().toString();

                                    int userGender = gender.getCheckedRadioButtonId();

                                    // find the radio button by returned id
                                    RadioButton radioButton = findViewById(userGender);

                                    String gender = radioButton.getText().toString();

                                    Boolean switchState = notifications.isChecked();


                                    //Toast.makeText(SetupProfile.this, "Phone: " + myPhone + " Name: " + name + " email: " + email + " Gender: " + gender + " Account Type:" + account_type + " Notification: " + switchState, Toast.LENGTH_LONG).show();

                                    dishiUser.setName(name);
                                    dishiUser.setBio(userbio);
                                    dishiUser.setEmail(email);
                                    dishiUser.setGender(gender);
                                    dishiUser.setAccount_type(account_type); // int value
                                    dishiUser.setNotifications(switchState.toString()); // boolean value
                                    dishiUser.setVerified("true");
                                    dishiUser.setEngaged("true"); //For nduthi accounts (determine if nduthi guy is engaged with an active order

                                    // Write user data to the database
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    final DatabaseReference myRef = database.getReference(myPhone);

                                    myRef.setValue(dishiUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                progressDialog.dismiss();
                                                // Write was successful!
                                                if(account_type.equals("1")){ // Cusomer account
                                                    //Slide to new activity
                                                    myRef.child("location-filter").setValue(10).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(SetupProfile.this, "Customer Account", Toast.LENGTH_LONG).show();
                                                            Intent slideactivity = new Intent(SetupProfile.this, MyAccountCustomer.class)
                                                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            Bundle bndlanimation =
                                                                    ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                                            startActivity(slideactivity, bndlanimation);

                                                            // Hiding the progressDialog after done uploading.
                                                            progressDialog.dismiss();
                                                        }
                                                    });

                                                }

                                                else if(account_type.equals("2")){ //Provider account
                                                    //Slide to new activity
                                                    Toast.makeText(SetupProfile.this, "Provider Account", Toast.LENGTH_LONG).show();
                                                    Intent slideactivity = new Intent(SetupProfile.this, MyAccountRestaurant.class)
                                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    Bundle bndlanimation =
                                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                                    startActivity(slideactivity, bndlanimation);

                                                    // Hiding the progressDialog after done uploading.
                                                    progressDialog.dismiss();
                                                }

                                                else if (account_type.equals("3")){ //Nduthi account
                                                    //Slide to new activity
                                                    Toast.makeText(SetupProfile.this, "Nduthi Account", Toast.LENGTH_LONG).show();
                                                    Intent slideactivity = new Intent(SetupProfile.this, MyAccountNduthi.class)
                                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    Bundle bndlanimation =
                                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                                    startActivity(slideactivity, bndlanimation);

                                                    // Hiding the progressDialog after done uploading.
                                                    progressDialog.dismiss();
                                                }

                                                else { // Others
                                                    Toast.makeText(SetupProfile.this, "'Others' account still inn development", Toast.LENGTH_LONG).show();
                                                    progressDialog.dismiss();
                                                }

                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Write failed

                                                        Toast.makeText(SetupProfile.this, "Failed: " + e.toString() + ". Try again!", Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });

                        }
                    })
                    // If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            // Hiding the progressDialog.
                            progressDialog.dismiss();

                            // Showing exception erro message.
                            Toast.makeText(SetupProfile.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })

                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            // Setting progressDialog Title.
                            //progressDialog.setTitle("Image is Uploading...");

                        }
                    });
        }
        else {

            Toast.makeText(SetupProfile.this, "Please enter all details", Toast.LENGTH_LONG).show();

        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this, "Verified: "+ verified, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        account_type = Integer.toString(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        account_type = "0";
    }

    @Override
    public void onItemSelected(com.rey.material.widget.Spinner parent, View view, int position, long id) {
        //account_type = Integer.parseInt((String) accType.getItemAtPosition(position));

    }

    //checking if field are empty
    private boolean CheckFieldValidation(){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        boolean valid=true;

        if(userName.getText().toString().equals("")){
            userName.setError("Can't be Empty");
            valid=false;
        }

        if(userEmail.equals(emailPattern)){
            userEmail.setError("Invalid Email");
            valid=false;
        }

        if(userEmail.getText().toString().equals("")){
            userEmail.setError("Cant't be empty");
            valid=false;
        }

        if (gender.getCheckedRadioButtonId() == -1){

            Toast.makeText(SetupProfile.this, "You must select gender", Toast.LENGTH_SHORT).show();
            valid=false;
        }

        if(account_type.equals("0")){
            Toast.makeText(this, "You must select account type", Toast.LENGTH_SHORT).show();
            valid=false;
        }


        return valid;
    }


}