package malcolmmaima.dishi.View.Fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import malcolmmaima.dishi.Model.DishiUser;

import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.MyAccountCustomer;
import malcolmmaima.dishi.View.SetupProfile;
import malcolmmaima.dishi.View.SplashActivity;

public class UserProfileFragment extends Fragment {

    String myPhone;
    DatabaseReference dbRef, menusRef;
    FirebaseDatabase db;
    FirebaseUser user;

    TextView userProfileName, profileBio;
    ImageView profilePic;

    List<DishiUser> userdata;

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
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);

        userProfileName = v.findViewById(R.id.user_profile_name);
        profilePic = v.findViewById(R.id.user_profile_photo);
        profileBio = v.findViewById(R.id.user_profile_short_bio);

        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference(myPhone);

        //Works like a charm, in future remember to change, use dishiUer POJO and fetch all user data at once
        dbRef.child("profilepic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ppic = dataSnapshot.getValue(String.class);

                try {
                    //Loading image from Glide library.
                    Glide.with(getContext()).load(ppic).into(profilePic);
                } catch (Exception e){
                    Intent slideactivity = new Intent(getContext(), SplashActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getContext(), R.anim.animation,R.anim.animation2).toBundle();
                    startActivity(slideactivity, bndlanimation);

                    Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Database Error. Load failed!", Toast.LENGTH_SHORT).show();
                Glide.with(getContext()).load(R.drawable.default_profile).into(profilePic);
            }
        });

        dbRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    String profilename = dataSnapshot.getValue(String.class);
                    userProfileName.setText(profilename);
                    //Toast.makeText(getContext(), "Name: " + profilename, Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error: " + databaseError, Toast.LENGTH_SHORT).show();
            }
        });

        dbRef.child("bio").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    String bio = dataSnapshot.getValue(String.class);
                    profileBio.setText(bio);
                    //Toast.makeText(getContext(), "Name: " + profilename, Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error: " + databaseError, Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }
}