package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import malcolmmaima.dishi.R;

public class SplashActivity extends AppCompatActivity {

    Boolean verified;

    String myPhone;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseDatabase mydb = FirebaseDatabase.getInstance();
        DatabaseReference connectionRef;

        connectionRef = mydb.getReference();
        if(connectionRef.onDisconnect().equals(true)){
            Toast.makeText(this, "Not connected!", Toast.LENGTH_LONG).show();
        }

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getInstance().getCurrentUser() == null || mAuth.getInstance().getCurrentUser().getPhoneNumber() == null) {

            //User is not signed in, send them back to verification page
            //Toast.makeText(this, "Not logged in!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));//Load Main Activity and clear activity stack
        } else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            myPhone = user.getPhoneNumber(); //Current logged in user phone number

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            final DatabaseReference dbRef = db.getReference(myPhone);

            //Check whether user is verified, if true send them directly to MyAccountRestaurant
            dbRef.child("verified").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String verified = dataSnapshot.getValue(String.class);

                    //Toast.makeText(SplashActivity.this, "Verified: " + verified, Toast.LENGTH_LONG).show();
                    if(verified == null){
                        verified = "false";

                        dbRef.child("verified").setValue(verified).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Intent mainActivity = new Intent(SplashActivity.this, MainActivity.class);
                                mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//Load Main Activity and clear activity stack
                                startActivity(mainActivity);


                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Write failed

                                        Toast.makeText(SplashActivity.this, "Error!", Toast.LENGTH_LONG).show();
                                    }
                                });

                        }

                    else if (verified.toString().equals("true")) { //Will need to check account type as well, then redirect to account type

                        //User is verified, so we need to check their account type and redirect accordingly
                        dbRef.child("account_type").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                                String account_type = dataSnapshot.getValue(String.class);
                                //String account_type = Integer.toString(acc_type);

                                if(account_type.equals("1")){ //Customer account
                                    Toast.makeText(SplashActivity.this, "Customer Account", Toast.LENGTH_LONG).show();
                                    Intent slideactivity = new Intent(SplashActivity.this, MyAccountCustomer.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Bundle bndlanimation =
                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                    startActivity(slideactivity, bndlanimation);
                                }

                                else if (account_type.equals("2")){ //Provider Restaurant account
                                    Toast.makeText(SplashActivity.this, "Provider Account", Toast.LENGTH_LONG).show();
                                    Intent slideactivity = new Intent(SplashActivity.this, MyAccountRestaurant.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Bundle bndlanimation =
                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                    startActivity(slideactivity, bndlanimation);
                                }

                                else if (account_type.equals("3")){ //Nduthi account
                                    //Slide to new activity
                                    Toast.makeText(SplashActivity.this, "Nduthi Account", Toast.LENGTH_LONG).show();
                                    Intent slideactivity = new Intent(SplashActivity.this, MyAccountNduthi.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Bundle bndlanimation =
                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                    startActivity(slideactivity, bndlanimation);
                                }

                                else { // Others
                                    Toast.makeText(SplashActivity.this, "'Others' account still in development", Toast.LENGTH_LONG).show();
                                }

                                    //Debugging purposes
                                     //Toast.makeText(SplashActivity.this, "Account type: " + account_type, Toast.LENGTH_LONG).show();
                                }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                //DB error, try again...if fails login again
                            }
                        });

                    } else {
                        //User is not verified so have them verify their profile details first
                        startActivity(new Intent(SplashActivity.this, SetupProfile.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));//Load Main Activity and clear activity stack
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }
}
