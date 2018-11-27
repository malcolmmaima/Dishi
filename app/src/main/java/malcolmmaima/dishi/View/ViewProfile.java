package malcolmmaima.dishi.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import malcolmmaima.dishi.Model.StatusUpdateModel;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.StatusUpdateAdapter;

import static android.view.View.INVISIBLE;

public class ViewProfile extends AppCompatActivity {

    DatabaseReference providerRef, myRef, rootRef;

    TextView userProfileName, profileBio, followersCounter, followingCounter, emptyTag;
    ImageView profilePic, coverImg;
    Button followUserBtn;
    String myPhone, picUrl;
    RecyclerView recyclerView;
    EditText statusPost;
    Button postStatus;

    List<StatusUpdateModel> list;

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
        followingCounter = findViewById(R.id.following);
        coverImg = findViewById(R.id.header_cover_image);
        recyclerView = findViewById(R.id.rview);
        postStatus = findViewById(R.id.postStatus);
        statusPost = findViewById(R.id.inputStatus);
        emptyTag = findViewById(R.id.empty_tag);

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

        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("platform_admin").child("cover_pic").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String coverPic = dataSnapshot.getValue(String.class);
                    //Loading image from Glide library.
                    Glide.with(ViewProfile.this).load(coverPic).into(coverImg);
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                } catch (Exception e){
                    //Error
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                        try {
                            Toast.makeText(ViewProfile.this, "failed to load cover picture. try again", Toast.LENGTH_SHORT).show();
                        } catch (Exception ee){

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        providerRef.child("profilepic").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String ppic = dataSnapshot.getValue(String.class);
                    picUrl = ppic;
                    //Loading image from Glide library.
                    Glide.with(ViewProfile.this).load(ppic).into(profilePic);
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                } catch (Exception e){
                    picUrl = "";
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
                   // deliveriesCounter.setText(""+dataSnapshot.getChildrenCount());
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

        providerRef.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {

                    //If followers are in the thousands them set to this format (1k followers)
                    if((int)dataSnapshot.getChildrenCount() > 999){
                        int thousandFollowers = (int) dataSnapshot.getChildrenCount();
                        followingCounter.setText(thousandFollowers / 1000 + "K");
                    }

                    else {
                        followingCounter.setText("" + dataSnapshot.getChildrenCount());
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

        postStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimeZone timeZone = TimeZone.getTimeZone("GMT+03:00");
                Calendar calendar = Calendar.getInstance(timeZone);
                String time = String.format("%02d" , calendar.get(Calendar.HOUR_OF_DAY))+":"+
                        String.format("%02d" , calendar.get(Calendar.MINUTE))+":"+
                        String.format("%02d" , calendar.get(Calendar.SECOND))+":"+
                        String.format("%03d" , calendar.get(Calendar.MILLISECOND));

                final StatusUpdateModel statusUpdateModel = new StatusUpdateModel();
                statusUpdateModel.setStatus(statusPost.getText().toString());
                statusUpdateModel.setTimePosted(time);
                statusUpdateModel.setAuthor(myPhone);
                statusUpdateModel.setPostedTo(providerPhone);

                final String key = providerRef.push().getKey();
                if(statusPost.getText().toString().equals("")){
                    Toast.makeText(ViewProfile.this, "You must enter something!", Toast.LENGTH_SHORT).show();
                }

                else {
                    providerRef.child("status_updates").child(key).setValue(statusUpdateModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ViewProfile.this, "Posted!", Toast.LENGTH_SHORT).show();
                            statusUpdateModel.key = key;
                            statusUpdateModel.setAuthor(myPhone);
                            statusUpdateModel.setPostedTo(providerPhone);
                            statusPost.setText("");
                        }
                    });
                }

            }
        });

        //Fetch follow status
        myRef.child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for(DataSnapshot following : dataSnapshot.getChildren()){
                    //Toast.makeText(ViewProfile.this, "following: " + following.getKey(), Toast.LENGTH_SHORT).show();
                    //If user i clicked on is in my db (following node) then set button to unfollow
                    if(providerPhone.equals(following.getKey()) ){
                        followUserBtn.setText("UNFOLLOW");
                        followUserBtn.setTag("unfollow");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imgActivity = new Intent(ViewProfile.this, ViewPhoto.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                imgActivity.putExtra("link", picUrl);

                startActivity(imgActivity);
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

        //Fetch the updates from status_updates node
        providerRef.child("status_updates").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list = new ArrayList<>();
                for(DataSnapshot updates : dataSnapshot.getChildren()){
                    StatusUpdateModel statusUpdateModel = updates.getValue(StatusUpdateModel.class);
                    statusUpdateModel.key = updates.getKey();
                    statusUpdateModel.setCurrentWall(providerPhone);
                    list.add(statusUpdateModel);
                }

                try {
                    if (!list.isEmpty()) {
                        emptyTag.setVisibility(View.GONE);
                        Collections.reverse(list);
                        recyclerView.setVisibility(View.VISIBLE);
                        StatusUpdateAdapter recycler = new StatusUpdateAdapter(ViewProfile.this, list);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(ViewProfile.this);
                        recyclerView.setLayoutManager(layoutmanager);
                        recyclerView.setItemAnimator(new SlideInLeftAnimator());

                        recycler.notifyDataSetChanged();

                        recyclerView.getItemAnimator().setAddDuration(1000);
                        recyclerView.getItemAnimator().setRemoveDuration(1000);
                        recyclerView.getItemAnimator().setMoveDuration(1000);
                        recyclerView.getItemAnimator().setChangeDuration(1000);

                        recyclerView.setAdapter(recycler);
                    } else {
                        emptyTag.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(INVISIBLE);
                    }
                }

                catch (Exception e){
                    emptyTag.setVisibility(View.VISIBLE);
                    emptyTag.setText("ERROR");
                    recyclerView.setVisibility(INVISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
