package malcolmmaima.dishi.View.Fragments;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

import malcolmmaima.dishi.Model.StatusUpdateModel;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Activities.Followers;
import malcolmmaima.dishi.View.Activities.Following;
import malcolmmaima.dishi.View.Activities.ViewProfile;
import malcolmmaima.dishi.View.Adapters.StatusUpdateAdapter;

public class UserProfileFragment extends Fragment {

    String myPhone;
    DatabaseReference dbRef, rootRef;
    FirebaseDatabase db;
    FirebaseUser user;

    TextView userProfileName, profileBio, followersCounter, followingCounter, emptyTag, following, followingTitle, followers, followersTitle;
    ImageView profilePic, coverImg;
    Button reviews, stats;
    EditText statusPost;
    Button postStatus;
    CardView restaurantExtra;
    RecyclerView recyclerView;

    List<StatusUpdateModel> list;

    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_user_profile, container, false);
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        userProfileName = v.findViewById(R.id.user_profile_name);
        profilePic = v.findViewById(R.id.user_profile_photo);
        profileBio = v.findViewById(R.id.user_profile_short_bio);
        followersCounter = v.findViewById(R.id.followers);
        followingCounter = v.findViewById(R.id.following);
        coverImg = v.findViewById(R.id.header_cover_image);
        restaurantExtra = v.findViewById(R.id.extrainfo);
        reviews = v.findViewById(R.id.reviews);
        stats = v.findViewById(R.id.stats);
        postStatus = v.findViewById(R.id.postStatus);
        statusPost = v.findViewById(R.id.inputStatus);
        recyclerView = v.findViewById(R.id.rview);
        emptyTag = v.findViewById(R.id.empty_tag);
        following = v.findViewById(R.id.following);
        followingTitle = v.findViewById(R.id.followingTitle);
        followers = v.findViewById(R.id.followers);
        followersTitle = v.findViewById(R.id.followersTitle);

        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference(myPhone);
        rootRef = db.getReference();

        rootRef.child("platform_admin").child("cover_pic").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String coverPic = dataSnapshot.getValue(String.class);
                    //Loading image from Glide library.
                    Glide.with(getContext()).load(coverPic).into(coverImg);
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                } catch (Exception e){
                    //Error
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                        try {
                            Toast.makeText(getContext(), "failed to load cover picture. try again", Toast.LENGTH_SHORT).show();
                        } catch (Exception ee){

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dbRef.child("account_type").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String accType = dataSnapshot.getValue(String.class);

                    if(accType.equals("2")){
                        restaurantExtra.setVisibility(View.VISIBLE);
                        reviews.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getContext(), "In development!", Toast.LENGTH_SHORT).show();
                            }
                        });

                        stats.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getContext(), "In development!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        restaurantExtra.setVisibility(View.INVISIBLE);
                    }

                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dbRef.child("profilepic").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String ppic = dataSnapshot.getValue(String.class);
                    //Loading image from Glide library.
                    Glide.with(getContext()).load(ppic).into(profilePic);
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                } catch (Exception e){
                    //Error
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                        try {
                            Toast.makeText(getContext(), "failed to load profile picture. try again", Toast.LENGTH_SHORT).show();
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
            }
        });

        dbRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
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
                    Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error: " + databaseError, Toast.LENGTH_SHORT).show();
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        });

        dbRef.child("bio").addListenerForSingleValueEvent(new ValueEventListener() {
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
                Toast.makeText(getContext(), "Error: " + databaseError, Toast.LENGTH_SHORT).show();
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
            }
        });

        dbRef.child("followers").addValueEventListener(new ValueEventListener() {
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

        dbRef.child("following").addValueEventListener(new ValueEventListener() {
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

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myPhone != null) {
                    Intent slideactivity = new Intent(getContext(), Following.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    slideactivity.putExtra("phone", myPhone);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getContext(), R.anim.animation, R.anim.animation2).toBundle();
                    getContext().startActivity(slideactivity, bndlanimation);
                }
            }
        });

        followingTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myPhone != null) {
                    Intent slideactivity = new Intent(getContext(), Following.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    slideactivity.putExtra("phone", myPhone);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getContext(), R.anim.animation, R.anim.animation2).toBundle();
                    getContext().startActivity(slideactivity, bndlanimation);
                }
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myPhone != null) {
                    Intent slideactivity = new Intent(getContext(), Followers.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    slideactivity.putExtra("phone", myPhone);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getContext(), R.anim.animation, R.anim.animation2).toBundle();
                    getContext().startActivity(slideactivity, bndlanimation);
                }
            }
        });

        followersTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myPhone != null){
                    Intent slideactivity = new Intent(getContext(), Followers.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    slideactivity.putExtra("phone", myPhone);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getContext(), R.anim.animation,R.anim.animation2).toBundle();
                    getContext().startActivity(slideactivity, bndlanimation);
                }
            }
        });
        postStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                TimeZone timeZone = TimeZone.getTimeZone("GMT+03:00");
                Calendar calendar = Calendar.getInstance(timeZone);
                String time = date+ ":" +
                        String.format("%02d" , calendar.get(Calendar.HOUR_OF_DAY))+":"+
                        String.format("%02d" , calendar.get(Calendar.MINUTE))+":"+
                        String.format("%02d" , calendar.get(Calendar.SECOND)); //+":"+
                        //String.format("%03d" , calendar.get(Calendar.MILLISECOND));

                final StatusUpdateModel statusUpdateModel = new StatusUpdateModel();
                statusUpdateModel.setStatus(statusPost.getText().toString());
                statusUpdateModel.setTimePosted(time);
                statusUpdateModel.setAuthor(myPhone);

                final String key = dbRef.push().getKey();
                if(statusPost.getText().toString().equals("")){
                    Toast.makeText(getContext(), "You must enter something!", Toast.LENGTH_SHORT).show();
                }

                else {
                    dbRef.child("status_updates").child(key).setValue(statusUpdateModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Posted!", Toast.LENGTH_SHORT).show();
                            statusUpdateModel.key = key;
                            statusUpdateModel.setAuthor(myPhone);
                            statusPost.setText("");
                        }
                    });
                }

            }
        });

        //Fetch the updates from status_updates node
        dbRef.child("status_updates").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list = new ArrayList<>();
                for(DataSnapshot updates : dataSnapshot.getChildren()){
                    StatusUpdateModel statusUpdateModel = updates.getValue(StatusUpdateModel.class);
                    statusUpdateModel.key = updates.getKey();
                    statusUpdateModel.setCurrentWall(myPhone);
                    list.add(statusUpdateModel);
                    }

                try {
                    if (!list.isEmpty()) {
                        emptyTag.setVisibility(View.GONE);
                        Collections.reverse(list);
                        recyclerView.setVisibility(View.VISIBLE);
                        StatusUpdateAdapter recycler = new StatusUpdateAdapter(getContext(), list);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                        recyclerView.setLayoutManager(layoutmanager);
                        recyclerView.setItemAnimator(new SlideInLeftAnimator());

                        recycler.notifyDataSetChanged();

                        recyclerView.getItemAnimator().setAddDuration(1000);
                        recyclerView.getItemAnimator().setRemoveDuration(1000);
                        recyclerView.getItemAnimator().setMoveDuration(1000);
                        recyclerView.getItemAnimator().setChangeDuration(1000);

                        recyclerView.setAdapter(recycler);
                    } else {
                        emptyTag.setText("NO POSTS");
                        emptyTag.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(v.INVISIBLE);
                    }
                }

                catch (Exception e){
                    emptyTag.setVisibility(View.VISIBLE);
                    emptyTag.setText("ERROR");
                    recyclerView.setVisibility(v.INVISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }
}