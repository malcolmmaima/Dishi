package malcolmmaima.dishi.View.Fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import malcolmmaima.dishi.Model.ReceivedOrders;
import malcolmmaima.dishi.Model.RestaurantDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.CustomerOrderAdapter;
import malcolmmaima.dishi.View.Adapters.RestaurantAdapter;

public class NearbyRestaurantsFragment extends Fragment {

    ProgressDialog progressDialog ;
    List<RestaurantDetails> list;
    RecyclerView recyclerview;
    String myPhone;
    TextView emptyTag,totalItems, totalFee;
    Button confirmBtn;

    DatabaseReference dbRef, restaurants;
    FirebaseDatabase db;
    FirebaseUser user;

    public static NearbyRestaurantsFragment newInstance() {
        NearbyRestaurantsFragment fragment = new NearbyRestaurantsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.fragment_nearby_restaurants, container, false);

        // Assigning Id to ProgressDialog.
        progressDialog = new ProgressDialog(getContext());
        // Setting progressDialog Title.
        progressDialog.setMessage("Loading...");
        // Showing progressDialog.
        progressDialog.show();

        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference(myPhone);
        restaurants = db.getReference();

        recyclerview = v.findViewById(R.id.rview);
        emptyTag = v.findViewById(R.id.empty_tag);

        final int[] initial_filter = new int[1];
        final int[] distanceThreshold = {0};
        final int[] location_filter = new int[1];

        dbRef.child("location-filter").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    location_filter[0] = dataSnapshot.getValue(Integer.class);

                    //Toast.makeText(context, "Fetch: " + location_filter, Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    location_filter[0] = 0;
                    //Toast.makeText(context, "Fetch: " + location_filter, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Search for restaurants (account type 2)
        restaurants.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list = new ArrayList<>();
                //Loop through all users
                for (DataSnapshot users : dataSnapshot.getChildren()){

                    //loop through each user and find out if they're a restaurant
                    for(DataSnapshot restaurant : users.getChildren()){
                        if(restaurant.getKey().equals("account_type")){
                            String accType = restaurant.getValue(String.class);
                            if(accType.equals("2")){
                                //Assign details to our Model
                                RestaurantDetails restaurantDetails = users.getValue(RestaurantDetails.class);
                                restaurantDetails.phone = users.getKey().toString();
                                //Toast.makeText(getContext(), "details: " + restaurantDetails.getName(), Toast.LENGTH_SHORT).show();
                                list.add(restaurantDetails);
                            }
                        }

                    }

                }

                try {
                    if (!list.isEmpty()) {
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                        recyclerview.setVisibility(View.VISIBLE);
                        RestaurantAdapter recycler = new RestaurantAdapter(getContext(), list);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                        recyclerview.setLayoutManager(layoutmanager);
                        recyclerview.setItemAnimator(new SlideInLeftAnimator());

                        recycler.notifyDataSetChanged();

                        recyclerview.getItemAnimator().setAddDuration(1000);
                        recyclerview.getItemAnimator().setRemoveDuration(1000);
                        recyclerview.getItemAnimator().setMoveDuration(1000);
                        recyclerview.getItemAnimator().setChangeDuration(1000);

                        recyclerview.setAdapter(recycler);
                        emptyTag.setVisibility(v.INVISIBLE);
                    } else {
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                        recyclerview.setVisibility(v.INVISIBLE);
                        emptyTag.setVisibility(v.VISIBLE);
                        emptyTag.setText("Try again");
                    }
                } catch (Exception e){
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    recyclerview.setVisibility(v.INVISIBLE);
                    emptyTag.setVisibility(v.VISIBLE);
                    emptyTag.setText("Try again");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }
}
