package malcolmmaima.dishi.View;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import malcolmmaima.dishi.R;

public class ViewProfile extends AppCompatActivity {

    DatabaseReference providerRef, myRef;

    TextView userProfileName, profileBio, followersCounter, deliveriesCounter;
    ImageView profilePic;
    Button followUserBtn;
    String myPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        final ProgressDialog progressDialog = new ProgressDialog(ViewProfile.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number

        userProfileName = findViewById(R.id.user_profile_name);
        profilePic = findViewById(R.id.user_profile_photo);
        profileBio = findViewById(R.id.user_profile_short_bio);
        followUserBtn = findViewById(R.id.btnFollow);
        followersCounter = findViewById(R.id.followers);
        deliveriesCounter = findViewById(R.id.deliveries);

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Profile");

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Go back to previous activity
            }
        });
        //topToolBar.setLogo(R.drawable.logo);
        //topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));

        final String providerPhone = getIntent().getStringExtra("phone");

        if(providerPhone.equals(null) || providerPhone == null || providerPhone.equals("null")){
            Toast.makeText(this, "Error fetching provider details, try again!", Toast.LENGTH_LONG).show();
            finish();
        }

        //Set db reference to provider phone number then fetch user data (profile pic, name, bio)
        providerRef = FirebaseDatabase.getInstance().getReference(providerPhone);
        providerRef.child("profilepic").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String ppic = dataSnapshot.getValue(String.class);
                    //Loading image from Glide library.
                    Glide.with(ViewProfile.this).load(ppic).into(profilePic);
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                } catch (Exception e){
                    //Error
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                        try {
                            Toast.makeText(ViewProfile.this, "failed to load profile picture.", Toast.LENGTH_SHORT).show();
                        } catch (Exception ee){

                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Toast.makeText(ViewProfile.this, "Database Error. Load failed!", Toast.LENGTH_SHORT).show();
                Glide.with(ViewProfile.this).load(R.drawable.default_profile).into(profilePic);
            }
        });
        providerRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    String profilename = dataSnapshot.getValue(String.class);
                    userProfileName.setText(profilename);
                    //Toast.makeText(getContext(), "Name: " + profilename, Toast.LENGTH_SHORT).show();
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                } catch (Exception e){
                    Toast.makeText(ViewProfile.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewProfile.this, "Error: " + databaseError, Toast.LENGTH_SHORT).show();
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        });
        providerRef.child("bio").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    String bio = dataSnapshot.getValue(String.class);
                    profileBio.setText(bio);
                    //Toast.makeText(getContext(), "Name: " + profilename, Toast.LENGTH_SHORT).show();
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                } catch (Exception e){
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewProfile.this, "Error: " + databaseError, Toast.LENGTH_SHORT).show();
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        });

        providerRef.child("history_deliveries").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    deliveriesCounter.setText(""+dataSnapshot.getChildrenCount());
                } catch(Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        providerRef.child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {

                    //If followers are in the thousands them set to this format (1k followers)
                    if((int)dataSnapshot.getChildrenCount() > 999){
                        int thousandFollowers = (int) dataSnapshot.getChildrenCount();
                        followersCounter.setText(thousandFollowers / 1000 + "K");
                    }

                    else {
                        followersCounter.setText("" + dataSnapshot.getChildrenCount());
                    }
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //myref
        myRef = FirebaseDatabase.getInstance().getReference(myPhone);

        //Fetch follow status
        myRef.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot following : dataSnapshot.getChildren()){
                    //Toast.makeText(ViewProfile.this, "following: " + following.getKey(), Toast.LENGTH_SHORT).show();
                    //If user i clicked on is in my db (following node) then set button to unfollow
                    if(providerPhone.equals(following.getKey())){
                        followUserBtn.setText("UNFOLLOW");
                        followUserBtn.setTag("unfollow");
                    }
                    if(!providerPhone.equals(following.getKey())) {
                        followUserBtn.setText("FOLLOW");
                        followUserBtn.setTag("follow");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        followUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(followUserBtn.getTag().equals("follow")){
                    myRef.child("following").child(providerPhone).setValue("following").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //update provider's node as well
                            providerRef.child("followers").child(myPhone).setValue("follower").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    followUserBtn.setText("UNFOLLOW");
                                    followUserBtn.setTag("unfollow");
                                }
                            });
                        }
                    });
                }

                if(followUserBtn.getTag().equals("unfollow")){
                    myRef.child("following").child(providerPhone).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            providerRef.child("followers").child(myPhone).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    followUserBtn.setText("FOLLOW");
                                    followUserBtn.setTag("follow");
                                }
                            });
                        }
                    });
                }
            }
        });


    }
}
