package malcolmmaima.dishi.View.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import malcolmmaima.dishi.Model.ImageUploadInfo;

import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.RecyclerviewAdapter;

public class UserProfileFragment extends Fragment {

    String myPhone;
    DatabaseReference dbRef, menusRef;
    FirebaseDatabase db;
    FirebaseUser user;

    TextView userProfileName;
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

        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference(myPhone);

        /* Will work on this later. fetch all user data
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userdata = new ArrayList<>();
                // StringBuffer stringbuffer = new StringBuffer();
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){

                    DishiUser dishiUser = dataSnapshot1.getValue(DishiUser.class);
                    userdata.add(dishiUser);

                }
                if(!userdata.isEmpty()){
                    Toast.makeText(getContext(), "User data=> " + userdata.toString(), Toast.LENGTH_LONG).show();
                }

                else {

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());

                Toast.makeText(getActivity(), "Failed, refresh!", Toast.LENGTH_SHORT).show();
            }
        }); */

        //Works like a charm, in future remember to change, use dishiUer POJO and fetch all user data at once
        dbRef.child("profilepic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ppic_url = dataSnapshot.getValue(String.class);
                //Loading image from Glide library.
                Glide.with(getContext()).load(ppic_url).into(profilePic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dbRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profilename = dataSnapshot.getValue(String.class);
                userProfileName.setText(profilename);
                //Toast.makeText(getContext(), "Name: " + profilename, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }
}