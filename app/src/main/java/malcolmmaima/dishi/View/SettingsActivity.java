package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import malcolmmaima.dishi.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Settings");

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String myPhone;
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                myPhone = user.getPhoneNumber(); //Current logged in user phone number

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                final DatabaseReference dbRef = db.getReference(myPhone);

                //Different accounts all have access to the same settings activity, so we need to redirect to the proper account
                dbRef.child("Account type").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                        int account_type = dataSnapshot.getValue(Integer.class);

                        if(account_type == 1){ //Customer account
                            Toast.makeText(SettingsActivity.this, "Customer Account", Toast.LENGTH_LONG).show();
                            Intent slideactivity = new Intent(SettingsActivity.this, MyAccountCustomer.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Bundle bndlanimation =
                                    ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                            startActivity(slideactivity, bndlanimation);
                        }

                        else if (account_type == 2){ //Provider Restaurant account
                            Toast.makeText(SettingsActivity.this, "Provider Account", Toast.LENGTH_LONG).show();
                            Intent slideactivity = new Intent(SettingsActivity.this, MyAccountRestaurant.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Bundle bndlanimation =
                                    ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                            startActivity(slideactivity, bndlanimation);
                        }

                        else if (account_type == 3){ //Nduthi account
                            //Slide to new activity
                            Toast.makeText(SettingsActivity.this, "Nduthi Account", Toast.LENGTH_LONG).show();
                            Intent slideactivity = new Intent(SettingsActivity.this, MyAccountNduthi.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Bundle bndlanimation =
                                    ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                            startActivity(slideactivity, bndlanimation);
                        }

                        else { // Others
                            Toast.makeText(SettingsActivity.this, "'Others' account still in development", Toast.LENGTH_LONG).show();
                        }

                        //Debugging purposes
                        //Toast.makeText(SplashActivity.this, "Account type: " + account_type, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //DB error, try again...if fails login again
                        Toast.makeText(SettingsActivity.this, "Error: " + databaseError.toString() , Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        //topToolBar.setLogo(R.drawable.logo);
        //topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_save){
            Toast.makeText(SettingsActivity.this, "Save Settings", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}

