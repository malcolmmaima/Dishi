package malcolmmaima.dishi.View.Fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    int filter;
    TextView emptyTag,totalItems, totalFee;
    Button confirmBtn;
    boolean added;

    DatabaseReference dbRef, restaurants, providerRef;
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

        added = false;
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
                    filter = location_filter[0];

                    //Toast.makeText(context, "Fetch: " + location_filter, Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    location_filter[0] = 0;
                    filter = 0;
                    //Toast.makeText(context, "Fetch: " + location_filter, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final Double[] myLat = new Double[1];
        final Double[] myLong = new Double[1];

        //Lets create a Double[] array containing the restaurant's/provider's lat/lon
        final Double[] provlat = new Double[1];
        final Double[] provlon = new Double[1];

        dbRef.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                    if(dataSnapshot1.getKey().equals("latitude")){
                        myLat[0] = dataSnapshot1.getValue(Double.class) ;
                    }

                    if(dataSnapshot1.getKey().equals("longitude")){
                        myLong[0] = dataSnapshot1.getValue(Double.class);
                    }

                    //Toast.makeText(getContext(), "mylat: " + myLat[0] + " mylon: " + myLong[0], Toast.LENGTH_SHORT).show();
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
                                final RestaurantDetails restaurantDetails = users.getValue(RestaurantDetails.class);
                                restaurantDetails.phone = users.getKey().toString();
                                //Toast.makeText(getContext(), "details: " + restaurantDetails.getName(), Toast.LENGTH_SHORT).show();

                                //Compute restaurant's distance
                                providerRef = db.getReference(restaurantDetails.phone);
                                //Item provider latitude longitude coordinates
                                providerRef.child("location").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        for(DataSnapshot providerLoc : dataSnapshot.getChildren()){

                                            try {
                                                if(providerLoc.getKey().equals("longitude")){
                                                    provlon[0] = providerLoc.getValue(Double.class);
                                                    //Toast.makeText(getContext(), "(prov lat): " + provlon[0], Toast.LENGTH_SHORT).show();
                                                }

                                                if(providerLoc.getKey().equals("latitude")){
                                                    provlat[0] = providerLoc.getValue(Double.class);
                                                    //Toast.makeText(getContext(), "(prov lon): " + provlat[0], Toast.LENGTH_SHORT).show();
                                                }


                                            } catch (Exception e){
                                                Toast.makeText(getContext(), "" + e, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    /*
                                    Toast.makeText(getContext(), "Thresh: " + distanceThreshold[0] + "Km. You are " + distance(myLat[0], myLong[0], provlat[0], provlon[0], "K")
                                            + " Km away from " + orderDetails.getName(), Toast.LENGTH_SHORT).show();
                                    */


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                //If the distance between me and the provider of the product is above the distance threshold(filter), then
                                //dont add it to the recycler view list else add it
                                try {

                                    if(filter > distance(myLat[0], myLong[0], provlat[0], provlon[0], "K")){
                                        if(restaurantDetails.phone.equals(myPhone) == false){ //make sure my menus are not on my filter

                                            //filter duplicates from the list
                                            if(list.contains(restaurantDetails)){
                                                // is present ... :) so do nothing
                                                //list.remove(restaurantDetails);
                                            } else { list.add(restaurantDetails); }
                                        }

                                    } } catch (Exception e){
                                    //Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                }
                                //list.add(restaurantDetails); //wierd app behavior, works but once I comment this out list throws an exception

                            }
                        }

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                //Toast.makeText(getContext(), "seconds remaining: " + millisUntilFinished / 1000, Toast.LENGTH_SHORT).show();
            }

            public void onFinish() {
                //Toast.makeText(getContext(), "done!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
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
                        added = false;
                    } else {
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                        recyclerview.setVisibility(v.INVISIBLE);
                        emptyTag.setVisibility(v.VISIBLE);
                        emptyTag.setText("Try again");
                    }
                } catch (Exception e){
                    recyclerview.setVisibility(v.INVISIBLE);
                    emptyTag.setVisibility(v.VISIBLE);
                    emptyTag.setText("Try again");
                }
            }
        }.start();

        emptyTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                progressDialog.setMessage("loading...");
                progressDialog.show();

                recyclerview.setVisibility(v.INVISIBLE);
                emptyTag.setVisibility(v.VISIBLE);

                //Search for restaurants (account type 2)
                restaurants.addValueEventListener(new ValueEventListener() {
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
                                        final RestaurantDetails restaurantDetails = users.getValue(RestaurantDetails.class);
                                        restaurantDetails.phone = users.getKey().toString();
                                        //Toast.makeText(getContext(), "details: " + restaurantDetails.getName(), Toast.LENGTH_SHORT).show();

                                        //Compute restaurant's distance
                                        providerRef = db.getReference(restaurantDetails.phone);
                                        //Item provider latitude longitude coordinates
                                        providerRef.child("location").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                for(DataSnapshot providerLoc : dataSnapshot.getChildren()){

                                                    try {
                                                        if(providerLoc.getKey().equals("longitude")){
                                                            provlon[0] = providerLoc.getValue(Double.class);
                                                            //Toast.makeText(getContext(), "(prov lat): " + provlon[0], Toast.LENGTH_SHORT).show();
                                                        }

                                                        if(providerLoc.getKey().equals("latitude")){
                                                            provlat[0] = providerLoc.getValue(Double.class);
                                                            //Toast.makeText(getContext(), "(prov lon): " + provlat[0], Toast.LENGTH_SHORT).show();
                                                        }


                                                    } catch (Exception e){
                                                        Toast.makeText(getContext(), "" + e, Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                    /*
                                    Toast.makeText(getContext(), "Thresh: " + distanceThreshold[0] + "Km. You are " + distance(myLat[0], myLong[0], provlat[0], provlon[0], "K")
                                            + " Km away from " + orderDetails.getName(), Toast.LENGTH_SHORT).show();
                                    */
                                                //If the distance between me and the provider of the product is above the distance threshold(filter), then
                                                //dont add it to the recycler view list else add it
                                                try {

                                                    if(filter > distance(myLat[0], myLong[0], provlat[0], provlon[0], "K")){
                                                        if(restaurantDetails.phone.equals(myPhone) == false){ //make sure my menus are not on my filter
                                                            //filter duplicates from the list
                                                            if(list.contains(restaurantDetails)){
                                                                // is present ... :) so do nothing
                                                                //list.remove(restaurantDetails);
                                                            } else { list.add(restaurantDetails); }
                                                        }

                                                    } } catch (Exception e){
                                                    //Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        //filter duplicates from the list
                                        if(list.contains(restaurantDetails)){
                                            // is present ... :) so do nothing
                                            //list.remove(restaurantDetails);
                                        } else { list.add(restaurantDetails); }

                                        //list.add(restaurantDetails); //wierd app behavior, works but once I comment this out list throws an exception

                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                new CountDownTimer(3000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        //Toast.makeText(getContext(), "seconds remaining: " + millisUntilFinished / 1000, Toast.LENGTH_SHORT).show();
                    }

                    public void onFinish() {
                        //Toast.makeText(getContext(), "done!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
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
                                added = false;
                            } else {
                                if(progressDialog.isShowing()){
                                    progressDialog.dismiss();
                                }
                                recyclerview.setVisibility(v.INVISIBLE);
                                emptyTag.setVisibility(v.VISIBLE);
                                emptyTag.setText("Try again");
                            }
                        } catch (Exception e){
                            recyclerview.setVisibility(v.INVISIBLE);
                            emptyTag.setVisibility(v.VISIBLE);
                            emptyTag.setText("Try again");
                        }
                    }
                }.start();
            }
        });

        return v;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return round(dist, 2);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts decimal degrees to radians			:*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts radians to decimal degrees			:*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function rounds a double to N decimal places					 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
