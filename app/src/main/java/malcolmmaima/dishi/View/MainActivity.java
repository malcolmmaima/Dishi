package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import malcolmmaima.dishi.R;

public class MainActivity extends AppCompatActivity {

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    FirebaseAuth mAuth;
    EditText phoneed, codeed;
    FloatingActionButton fabbutton;
    String mVerificationId;
    TextView timertext;
    Timer timer;
    ImageView verifiedimg;
    Boolean mVerified = false;
    String phonenumber;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    String myPhone;

    ProgressDialog progressDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportActionBar().setTitle("Dishi");

        // Assigning Id to ProgressDialog.
        progressDialog = new ProgressDialog(MainActivity.this);

        if(mAuth.getInstance().getCurrentUser() != null){

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            myPhone = user.getPhoneNumber(); //Current logged in user phone number

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference dbRef = db.getReference(myPhone);

            //Check whether user is verified, if true send them directly to MyAccountRestaurant
            dbRef.child("verified").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Boolean verified = dataSnapshot.getValue(Boolean.class);

                    Toast.makeText(MainActivity.this, "Verified: " + verified, Toast.LENGTH_LONG).show();
                    if(verified == true){
                        startActivity(new Intent(MainActivity.this,MyAccountRestaurant.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    } else {
                        //User is not verified so have them verify their profile details first
                        startActivity(new Intent(MainActivity.this, SetupProfile.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));//Load Main Activity and clear activity stack
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        phoneed = findViewById(R.id.numbered);
        codeed = findViewById(R.id.verificationed);
        fabbutton = findViewById(R.id.sendverifybt);
        timertext = findViewById(R.id.timertv);
        verifiedimg = findViewById(R.id.verifiedsign);
        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d("TAG", "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    progressDialog.dismiss();
                    Snackbar snackbar = Snackbar
                            .make((LinearLayout) findViewById(R.id.parentlayout), "Verification Failed !! Invalid verification Code", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
                else if (e instanceof FirebaseTooManyRequestsException) {
                    progressDialog.dismiss();
                    Snackbar snackbar = Snackbar
                            .make((LinearLayout) findViewById(R.id.parentlayout), "Verification Failed !! Too many request. Try after some time. ", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }

                else if(e instanceof FirebaseNetworkException){
                    progressDialog.dismiss();
                    Snackbar snackbar = Snackbar
                            .make((LinearLayout) findViewById(R.id.parentlayout), "Failed! Check connection and try again. ", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:" + verificationId);

                progressDialog.dismiss();
                codeed.setVisibility(View.VISIBLE);

                Snackbar snackbar = Snackbar
                        .make((LinearLayout) findViewById(R.id.parentlayout), "Code Sent", Snackbar.LENGTH_LONG);

                snackbar.show();

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        fabbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabbutton.getTag().equals(getResources().getString(R.string.tag_send))) {
                    if (!phoneed.getText().toString().trim().isEmpty() && phoneed.getText().toString().trim().length() >= 10) {
                        // Setting progressDialog Title.
                        progressDialog.setTitle("Verifying...");
                        progressDialog.setMessage("Please wait");
                        // Showing progressDialog.
                        progressDialog.show();
                        progressDialog.setCancelable(false);

                        startPhoneNumberVerification(phoneed.getText().toString().trim());
                        mVerified = false;
                        starttimer();
                        fabbutton.setImageResource(R.drawable.ic_arrow_forward_white_24dp);
                        fabbutton.setTag(getResources().getString(R.string.tag_verify));
                        phonenumber = phoneed.getText().toString();
                    }
                    else {
                        progressDialog.dismiss();
                        phoneed.setError("Please enter valid mobile number");
                    }
                }

                if (fabbutton.getTag().equals(getResources().getString(R.string.tag_verify))) {
                    if (!codeed.getText().toString().trim().isEmpty() && !mVerified) {
                        Snackbar snackbar = Snackbar
                                .make((LinearLayout) findViewById(R.id.parentlayout), "Please wait...", Snackbar.LENGTH_LONG);

                        snackbar.show();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, codeed.getText().toString().trim());
                        signInWithPhoneAuthCredential(credential);
                    }
                    if (mVerified) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        myPhone = user.getPhoneNumber(); //Current logged in user phone number

                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference dbRef = db.getReference(myPhone);

                        dbRef.child("verified").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Boolean verified = dataSnapshot.getValue(Boolean.class);

                                Toast.makeText(MainActivity.this, "Verified: " + verified, Toast.LENGTH_LONG).show();
                                if(verified == true){
                                    //Slide to new activity
                                    Intent slideactivity = new Intent(MainActivity.this, MyAccountRestaurant.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Bundle bndlanimation =
                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                    startActivity(slideactivity, bndlanimation);

                                } else {
                                    //User is not verified so have them verify their profile details first
                                    Intent slideactivity = new Intent(MainActivity.this, SetupProfile.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Bundle bndlanimation =
                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                    startActivity(slideactivity, bndlanimation);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }

                }


            }
        });

        timertext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phoneed.getText().toString().trim().isEmpty() && phoneed.getText().toString().trim().length() >= 10) {
                    resendVerificationCode(phoneed.getText().toString().trim(), mResendToken);
                    mVerified = false;
                    starttimer();
                    codeed.setVisibility(View.VISIBLE);
                    fabbutton.setImageResource(R.drawable.ic_arrow_forward_white_24dp);
                    fabbutton.setTag(getResources().getString(R.string.tag_verify));
                    Snackbar snackbar = Snackbar
                            .make((LinearLayout) findViewById(R.id.parentlayout), "Resending verification code...", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAuth.getInstance().getCurrentUser() != null){

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            myPhone = user.getPhoneNumber(); //Current logged in user phone number

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            final DatabaseReference dbRef = db.getReference(myPhone);

            //Check whether user is verified, if true send them directly to MyAccountRestaurant
            dbRef.child("verified").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Boolean verified = dataSnapshot.getValue(Boolean.class);

                    Toast.makeText(MainActivity.this, "Verified: " + verified, Toast.LENGTH_LONG).show();
                    if(verified == true){
                        //User is verified, so we need to check their account type and redirect accordingly
                        dbRef.child("account_type").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                                int account_type = dataSnapshot.getValue(Integer.class);

                                if(account_type == 1){ //Customer account
                                    Toast.makeText(MainActivity.this, "Customer Account", Toast.LENGTH_LONG).show();
                                    Intent slideactivity = new Intent(MainActivity.this, MyAccountCustomer.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Bundle bndlanimation =
                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                    startActivity(slideactivity, bndlanimation);
                                }

                                else if (account_type == 2){ //Provider Restaurant account
                                    Toast.makeText(MainActivity.this, "Provider Account", Toast.LENGTH_LONG).show();
                                    Intent slideactivity = new Intent(MainActivity.this, MyAccountRestaurant.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Bundle bndlanimation =
                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                    startActivity(slideactivity, bndlanimation);
                                }

                                else if (account_type == 3){ //Nduthi account
                                    //Slide to new activity
                                    Toast.makeText(MainActivity.this, "Nduthi Account", Toast.LENGTH_LONG).show();
                                    Intent slideactivity = new Intent(MainActivity.this, MyAccountNduthi.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    Bundle bndlanimation =
                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                    startActivity(slideactivity, bndlanimation);
                                }

                                else { // Others
                                    Toast.makeText(MainActivity.this, "'Others' account still in development", Toast.LENGTH_LONG).show();
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
                        Intent slideactivity = new Intent(MainActivity.this, SetupProfile.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                        startActivity(slideactivity, bndlanimation);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            mVerified = true;
                            timer.cancel();
                            verifiedimg.setVisibility(View.VISIBLE);
                            timertext.setVisibility(View.INVISIBLE);
                            phoneed.setEnabled(false);
                            codeed.setVisibility(View.INVISIBLE);
                            Snackbar snackbar = Snackbar
                                    .make((LinearLayout) findViewById(R.id.parentlayout), "Successfully Verified", Snackbar.LENGTH_LONG);

                            snackbar.show();

                            myPhone = user.getPhoneNumber(); //Current logged in user phone number

                            FirebaseDatabase db = FirebaseDatabase.getInstance();
                            final DatabaseReference dbRef = db.getReference(myPhone);

                            //Check whether user is verified, if true send them directly to MyAccount_(n)
                            dbRef.child("verified").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String verified = dataSnapshot.getValue(String.class);

                                    if(verified == null) {
                                        verified = "false";

                                        dbRef.child("verified").setValue(verified).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //First time signup
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Write failed
                                                        Toast.makeText(MainActivity.this, "error: " + e, Toast.LENGTH_SHORT).show();

                                                    }
                                                });
                                    }

                                    //Toast.makeText(MainActivity.this, "Verified: " + verified, Toast.LENGTH_LONG).show();
                                    if(verified.toString().equals("true")){
                                        //User is verified, so we need to check their account type and redirect accordingly
                                        dbRef.child("account_type").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                                                String account_type = dataSnapshot.getValue(String.class);

                                                if(account_type.equals("1")){ //Customer account
                                                    //Toast.makeText(MainActivity.this, "Customer Account", Toast.LENGTH_LONG).show();
                                                    Intent slideactivity = new Intent(MainActivity.this, MyAccountCustomer.class)
                                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    Bundle bndlanimation =
                                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                                    startActivity(slideactivity, bndlanimation);
                                                }

                                                else if (account_type.equals("2")){ //Provider Restaurant account
                                                    //Toast.makeText(MainActivity.this, "Provider Account", Toast.LENGTH_LONG).show();
                                                    Intent slideactivity = new Intent(MainActivity.this, MyAccountRestaurant.class)
                                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    Bundle bndlanimation =
                                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                                    startActivity(slideactivity, bndlanimation);
                                                }

                                                else if (account_type.equals("3")){ //Nduthi account
                                                    //Slide to new activity
                                                    //Toast.makeText(MainActivity.this, "Nduthi Account", Toast.LENGTH_LONG).show();
                                                    Intent slideactivity = new Intent(MainActivity.this, MyAccountNduthi.class)
                                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    Bundle bndlanimation =
                                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                                    startActivity(slideactivity, bndlanimation);
                                                }

                                                else { // Others
                                                    Toast.makeText(MainActivity.this, "'Others' account still in development", Toast.LENGTH_LONG).show();
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
                                        Intent slideactivity = new Intent(MainActivity.this, SetupProfile.class)
                                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        Bundle bndlanimation =
                                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                        startActivity(slideactivity, bndlanimation);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });


                        } else {
                            // Sign in failed, display a message and update the UI
                            progressDialog.dismiss();
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Snackbar snackbar = Snackbar
                                        .make((LinearLayout) findViewById(R.id.parentlayout), "Invalid OTP ! Please enter correct OTP", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        }
                    }
                });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

    }

    public void starttimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {

            int second = 60;

            @Override
            public void run() {
                if (second <= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("RESEND CODE");
                            timer.cancel();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("00:" + second--);
                        }
                    });
                }

            }
        }, 0, 1000);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

}
