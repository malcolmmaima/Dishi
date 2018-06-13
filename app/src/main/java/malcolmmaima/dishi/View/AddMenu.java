package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;

public class AddMenu extends AppCompatActivity {

    TextView productName,productPrice,productDescription;
    //CircleImageView foodPic;
    private ImageView foodPic;
    Button save;
    String myPhone;

    // Creating URI.
    Uri FilePathUri;

    // Folder path for Firebase Storage.
    String Storage_Path = "Users";

    String ppicStatus;

    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog ;

    // Image request code for onActivityResult() .
    int Image_Request_Code = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_menu_activity);

        // Assigning Id to ProgressDialog.
        progressDialog = new ProgressDialog(AddMenu.this);

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Add New Item");


        // Assign FirebaseStorage instance to storageReference.
        storageReference = FirebaseStorage.getInstance().getReference();

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });
        //topToolBar.setLogo(R.drawable.logo);
        //topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));

        final DatabaseReference menusRef;
        FirebaseDatabase db;
        FirebaseUser user;
        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number

        foodPic = findViewById(R.id.foodpic);
        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        save = findViewById(R.id.save);

        foodPic.setOnClickListener(new View.OnClickListener() {
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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Checking whether FilePathUri Is empty or not and passes validation check.
                if (FilePathUri != null && CheckFieldValidation()) {
                    // Setting progressDialog Title.
                    progressDialog.setTitle("Adding...");

                    // Showing progressDialog.
                    progressDialog.show();
                    progressDialog.setCancelable(false);
                    uploadMenu();

                }
                else {
                    if(CheckFieldValidation() == false){
                        Toast.makeText(AddMenu.this, "Please enter all fields", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AddMenu.this, "Please Select Food Image", Toast.LENGTH_LONG).show();
                        ppicStatus = "empty";
                    }

                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_multiple) {
            Toast.makeText(AddMenu.this, "Add Multiple", Toast.LENGTH_LONG).show();
        }
        if(id == R.id.action_save){
            // Checking whether FilePathUri Is empty or not and passes validation check.
            if (FilePathUri != null && CheckFieldValidation()) {
                // Setting progressDialog Title.
                progressDialog.setTitle("Adding...");

                // Showing progressDialog.
                progressDialog.show();
                progressDialog.setCancelable(false);
                uploadMenu();

            }
            else {

                Toast.makeText(AddMenu.this, "Please Select Food Image", Toast.LENGTH_LONG).show();
                ppicStatus = "empty";
            }

        }
        return super.onOptionsItemSelected(item);
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
                foodPic.setImageBitmap(bitmap);

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

    //checking if field are empty
    private boolean CheckFieldValidation() {

        boolean valid = true;

        if (productName.getText().toString().equals("")) {
            productName.setError("Can't be Empty");
            valid = false;
        }

        if (productPrice.getText().toString().equals("")) {
            productPrice.setError("Can't be Empty");
            valid = false;
        }

        if (productDescription.getText().toString().equals("")) {
            productDescription.setError("Can't be Empty");
            valid = false;
        }

        return valid;
    }

    public void uploadMenu(){

        final DatabaseReference menusRef;
        FirebaseDatabase db;

        db = FirebaseDatabase.getInstance();
        menusRef = db.getReference(myPhone + "/mymenu"); //Under the user's node, place their menu items

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number

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

                                String name = productName.getText().toString();
                                String price = productPrice.getText().toString();
                                String description = productDescription.getText().toString();

                                String key = menusRef.push().getKey(); //The child node in mymenu for storing menu items
                                ProductDetails productDetails = new ProductDetails();

                                productDetails.setName(name);
                                productDetails.setPrice(price);
                                productDetails.setDescription(description);
                                productDetails.setImageURL(o.toString());

                                Log.d("myimage", "onSuccess: product image: " + productDetails.getImageURL());

                                menusRef.child(key).setValue(productDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Write was successful!
                                        // Hiding the progressDialog after done uploading.
                                        progressDialog.dismiss();

                                        Snackbar snackbar = Snackbar
                                                .make((RelativeLayout) findViewById(R.id.parentlayout), "Added successfully!", Snackbar.LENGTH_LONG);

                                        snackbar.show();

                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Write failed
                                                progressDialog.dismiss();
                                                Toast.makeText(AddMenu.this, "Failed: " + e.toString() + ". Try again!", Toast.LENGTH_LONG).show();
                                            }
                                        });

                                productName.setText("");
                                productPrice.setText("");
                                productDescription.setText("");
                                foodPic.setImageDrawable(getResources().getDrawable(R.drawable.ic_food_menu));
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
                        Toast.makeText(AddMenu.this, exception.getMessage(), Toast.LENGTH_LONG).show();
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
}
