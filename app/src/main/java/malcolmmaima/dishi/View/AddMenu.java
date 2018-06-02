package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;

public class AddMenu extends AppCompatActivity {

    TextView productName,productPrice,productDescription;
    Button save;
    String myPhone;

    ProgressDialog progressDialog ;

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

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent slideactivity = new Intent(getApplicationContext(), MyAccount.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Bundle bndlanimation = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                startActivity(slideactivity, bndlanimation);
            }
        });
        //topToolBar.setLogo(R.drawable.logo);
        //topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));

        final DatabaseReference menusRef;
        FirebaseDatabase db;

        FirebaseUser user;

        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number

        db = FirebaseDatabase.getInstance();
        menusRef = db.getReference(myPhone + "/mymenu");

        productName = findViewById(R.id.productName);
        productPrice = findViewById(R.id.productPrice);
        productDescription = findViewById(R.id.productDescription);
        save = findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckFieldValidation()){
                String name = productName.getText().toString();
                String price = productPrice.getText().toString();
                String description = productDescription.getText().toString();


                String key = menusRef.push().getKey();
                ProductDetails productDetails = new ProductDetails();

                productDetails.setName(name);
                productDetails.setPrice(price);
                productDetails.setDescription(description);

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
            if(CheckFieldValidation()){
                // Setting progressDialog Title.
                progressDialog.setTitle("Adding...");

                // Showing progressDialog.
                progressDialog.show();

                final DatabaseReference menusRef;
                FirebaseDatabase db;
                db = FirebaseDatabase.getInstance();
                menusRef = db.getReference(myPhone + "/mymenu");

                //Toast.makeText(AddMenu.this, "Save Menu", Toast.LENGTH_LONG).show();
                String name =  productName.getText().toString();
                String price =  productPrice.getText().toString();
                String description =  productDescription.getText().toString();


                String key = menusRef.push().getKey();
                ProductDetails productDetails = new ProductDetails();

                productDetails.setName(name);
                productDetails.setPrice(price);
                productDetails.setDescription(description);

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
            }

        }
        return super.onOptionsItemSelected(item);
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
}
