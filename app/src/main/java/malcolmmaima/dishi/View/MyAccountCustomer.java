package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import android.view.Menu;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Fragments.ItemFourFragment;
import malcolmmaima.dishi.View.Fragments.ItemThreeFragment;
import malcolmmaima.dishi.View.Fragments.CustomerOrderFragment;


public class MyAccountCustomer extends AppCompatActivity {
    String myPhone;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_customer);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getInstance().getCurrentUser() == null){

            //User is not signed in, send them back to verification page
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MyAccountCustomer.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));//Load Main Activity and clear activity stack
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference dbRef = db.getReference(myPhone);

        //Check whether user is verified, if true send them directly to MyAccountRestaurant
        dbRef.child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String account_name = dataSnapshot.getValue(String.class);
                //Toast.makeText(MyAccountRestaurant.this, "Welcome " + account_name, Toast.LENGTH_LONG).show();
                setTitle(account_name);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        //topToolBar.setLogo(R.drawable.logo);
        //topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));



        //Fragments Implementation
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {

                            case R.id.action_item1:
                                selectedFragment = CustomerOrderFragment.newInstance();
                                break;
                            case R.id.action_item2:
                                selectedFragment = ItemThreeFragment.newInstance();
                                break;
                            case R.id.action_item3:
                                selectedFragment = ItemFourFragment.newInstance();
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, CustomerOrderFragment.newInstance());
        transaction.commit();

        //Used to select an item programmatically
        //bottomNavigationView.getMenu().getItem(2).setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_customer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_cart){
            //Toast.makeText(MyAccountRestaurant.this, "Add Menu", Toast.LENGTH_LONG).show();

            Intent slideactivity = new Intent(MyAccountCustomer.this, MyCart.class);
            Bundle bndlanimation =
                    ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
            startActivity(slideactivity, bndlanimation);

        }
        if (id == R.id.action_settings) {
            //Toast.makeText(MyAccountRestaurant.this, "Settings", Toast.LENGTH_LONG).show();
            Intent slideactivity = new Intent(MyAccountCustomer.this, SettingsActivity.class);
            Bundle bndlanimation =
                    ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
            startActivity(slideactivity, bndlanimation);
        }
        if(id == R.id.action_refresh){
            Toast.makeText(MyAccountCustomer.this, "Refresh App", Toast.LENGTH_LONG).show();
        }
        if(id == R.id.action_logout){
            //Toast.makeText(MyAccountRestaurant.this, "Logout", Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MyAccountCustomer.this,MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}


