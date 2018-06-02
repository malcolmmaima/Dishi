package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

import malcolmmaima.dishi.Model.ImageUploadInfo;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.customfonts.EditText_Roboto_Regular;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SetupProfile extends AppCompatActivity implements com.rey.material.widget.Spinner.OnItemSelectedListener, AdapterView.OnItemSelectedListener {

    Button logoutbutton, continueBtn, backButton;
    private ImageView profile_pic;

    EditText_Roboto_Regular userName, userBio, userEmail, changeNumber;
    RadioButton maleRd, femaleRd;
    RadioGroup gender;
    Spinner accType;
    Switch notifications;
    String myPhone;
    String ppicStatus;

    private FirebaseAuth mAuth;
    int account_type;

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

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = db.getReference(myPhone);

        //Check whether user is verified, if true send them directly to MyAccount
        dbRef.child("Verified").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean verified = dataSnapshot.getValue(Boolean.class);

                //Toast.makeText(SetupProfile.this, "Verified: " + verified, Toast.LENGTH_LONG).show();
                if(verified == null){
                    verified = false;
                }

                if(verified == true){
                    //Slide to new activity
                    Intent slideactivity = new Intent(SetupProfile.this, MyAccount.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                    startActivity(slideactivity, bndlanimation);

                } else {
                    //Remain on SetupProfile to verify profile details
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


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

                UploadImageFileToFirebaseStorage();

                if(CheckFieldValidation()) {
                    String name = userName.getText().toString();
                    String email = userEmail.getText().toString();
                    String userbio = userBio.getText().toString();

                    int userGender = gender.getCheckedRadioButtonId();

                    // find the radio button by returned id
                    RadioButton radioButton = findViewById(userGender);

                    String gender = radioButton.getText().toString();

                    Boolean switchState = notifications.isChecked();


                    //Toast.makeText(SetupProfile.this, "Phone: " + myPhone + " Name: " + name + " email: " + email + " Gender: " + gender + " Account Type:" + account_type + " Notification: " + switchState, Toast.LENGTH_LONG).show();

                    // Setting progressDialog Title.
                    progressDialog.setTitle("Saving...");

                    // Showing progressDialog.
                    progressDialog.show();

                        // Write user data to the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference(myPhone);
                    myRef.child("Name").setValue(name);
                    myRef.child("Bio").setValue(userbio);
                    myRef.child("Email").setValue(email);
                    myRef.child("Gender").setValue(gender);
                    myRef.child("Account type").setValue(account_type);
                    myRef.child("Notifications").setValue(switchState);
                    myRef.child("Verified").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Write was successful!

                            //Slide to new activity
                            Intent slideactivity = new Intent(SetupProfile.this, MyAccount.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Bundle bndlanimation =
                                    ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                            startActivity(slideactivity, bndlanimation);

                            // Hiding the progressDialog after done uploading.
                            progressDialog.dismiss();

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

                // After selecting image change choose button above text.
                //ChooseButton.setText("Image Selected");

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
    public void UploadImageFileToFirebaseStorage() {

        // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) {

            ppicStatus = "set";
            // Setting progressDialog Title.
            //progressDialog.setTitle("Image is Uploading...");

            // Showing progressDialog.
            //progressDialog.show();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            myPhone = user.getPhoneNumber(); //Current logged in user phone number

            // Creating second StorageReference.
            StorageReference storageReference2nd = storageReference.child(Storage_Path + "/" + myPhone + "/" + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Getting image name from EditText and store into string variable.
                            String TempImageName = userName.getText().toString().trim();

                            // Hiding the progressDialog after done uploading.
                            //progressDialog.dismiss();

                            // Showing toast message after done uploading.
                            //Toast.makeText(getApplicationContext(), "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();

                            @SuppressWarnings("VisibleForTests")
                            ImageUploadInfo imageUploadInfo = new ImageUploadInfo(TempImageName, taskSnapshot.getUploadSessionUri().toString());

                            // Getting image upload ID.
                            String ImageUploadId = databaseReference.push().getKey();

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            myPhone = user.getPhoneNumber(); //Current logged in user phone number

                            // Write image data to the database
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference(myPhone);
                            myRef.child("Profile").setValue(imageUploadInfo);

                            // Adding image upload id s child element into databaseReference.
                            //databaseReference.child(ImageUploadId).setValue(imageUploadInfo);
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

            Toast.makeText(SetupProfile.this, "Please Select Profile Image", Toast.LENGTH_LONG).show();
            ppicStatus = "empty";

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
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        account_type = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        account_type = 0;
    }

    @Override
    public void onItemSelected(com.rey.material.widget.Spinner parent, View view, int position, long id) {
        //account_type = Integer.parseInt((String) accType.getItemAtPosition(position));

    }

    //checking if field are empty
    private boolean CheckFieldValidation(){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        boolean valid=true;

        if(ppicStatus.equals("empty")){
            Toast.makeText(SetupProfile.this, "You must select image", Toast.LENGTH_SHORT).show();
            valid=false;
        }

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

        if(account_type == 0){
            Toast.makeText(this, "You must select account type", Toast.LENGTH_SHORT).show();
            valid=false;
        }


        return valid;
    }


}