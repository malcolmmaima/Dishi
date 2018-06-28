package malcolmmaima.dishi.View.Fragments;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
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

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import malcolmmaima.dishi.Model.DishiUser;
import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.CustomerOrderAdapter;
import malcolmmaima.dishi.View.AddMenu;
import malcolmmaima.dishi.View.MyCart;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;


import static android.content.Context.MODE_PRIVATE;

public class CustomerOrderFragment extends Fragment {

    private String simpleFileName = "appdata.txt";

    ProgressDialog progressDialog ;
    List<OrderDetails> list;
    List<MyCartDetails> myBasket;
    List<DishiUser> users;
    RecyclerView recyclerview;
    String myPhone;
    TextView emptyTag, totalItems, totalFee, filterDistance;
    Button checkoutBtn;
    SeekBar seekBar;

    DatabaseReference dbRef, menusRef, providerRef;
    FirebaseDatabase db;

    FirebaseUser user;

    boolean loaded;


    public static CustomerOrderFragment newInstance() {
        CustomerOrderFragment fragment = new CustomerOrderFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_customer_order, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number
        db = FirebaseDatabase.getInstance();

        dbRef = db.getReference(myPhone);
        menusRef = db.getReference();

        recyclerview = v.findViewById(R.id.rview);
        emptyTag = v.findViewById(R.id.empty_tag);
        totalItems = v.findViewById(R.id.totalItems);
        totalFee = v.findViewById(R.id.totalFee);
        checkoutBtn = v.findViewById(R.id.checkoutBtn);
        seekBar = v.findViewById(R.id.seekBar);
        filterDistance = v.findViewById(R.id.filterDistance);

        checkoutBtn.setEnabled(false);
        loaded = false;

        final int[] initial_filter = new int[1];
        final int[] distanceThreshold = {0};
        final int[] location_filter = new int[1];

        dbRef.child("location-filter").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                location_filter[0] = dataSnapshot.getValue(Integer.class);

                distanceThreshold[0] = location_filter[0];
                initial_filter[0] = location_filter[0];
                //Toast.makeText(context, "Fetch: " + location_filter, Toast.LENGTH_SHORT).show();
                seekBar.setProgress(location_filter[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        final Double[] myLat = new Double[1];
        final Double[] myLong = new Double[1];

        //Lets create a Double[] array containing the provider lat/lon
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


        //Loop through the mymenu child node and get menu items, assign values to our ProductDetails model
        menusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    list = new ArrayList<>();
                    users = new ArrayList<>();

                    // StringBuffer stringbuffer = new StringBuffer();

                    //So first we loop through the users in the firebase db
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        //DishiUser dishiUser = dataSnapshot1.getValue(DishiUser.class); //Assign values to model
                        //Toast.makeText(getContext(), "User: " + dishiUser.getName(), Toast.LENGTH_SHORT).show();

                        //afterwards we check if that user has a 'mymenu' child node, if so loop through it and show the products
                        //NOTE: only restaurant/provider accounts have the 'mymenu', so essentially we are fetching restaurant menus into our customers fragment via the adapter
                        for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("mymenu").getChildren()) {
                            final OrderDetails orderDetails = dataSnapshot2.getValue(OrderDetails.class);
                            //Toast.makeText(getContext(), "mymenu: " + dataSnapshot2.getKey(), Toast.LENGTH_SHORT).show();
                            orderDetails.providerNumber = dataSnapshot1.getKey();
                            orderDetails.providerName = dataSnapshot1.child("name").getValue().toString();



                            providerRef = db.getReference(orderDetails.providerNumber);
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
                                    if(distanceThreshold[0] > distance(myLat[0], myLong[0], provlat[0], provlon[0], "K")){

                                        if(orderDetails.providerNumber != myPhone){
                                            //Don't add my menu to order list (if user switches account type)
                                            list.add(orderDetails);

                                            if(list.size() < 1){
                                                loaded = false;
                                                //Toast.makeText(getContext(), "Loaded" + loaded, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    } } catch (Exception e){
                                        //Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                        //Toast.makeText(getContext(), "Phone: " + dataSnapshot1.getKey(), Toast.LENGTH_SHORT).show(); //Phone numbers

                    }


                    try {

                        if (loaded == false) {

                                Toast.makeText(getContext(), "Loaded = " + loaded, Toast.LENGTH_SHORT).show();
                                CustomerOrderAdapter recycler = new CustomerOrderAdapter(getContext(), list);
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

                                loaded = true;

                        }

                    } catch (Exception exception){
                        Toast.makeText(getContext(), "Error: " + exception, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e){
                    emptyTag.setText("Failed");
                    emptyTag.setVisibility(v.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());

                progressDialog.dismiss();

                Toast.makeText(getActivity(), "Failed, " + error, Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference myCartRef;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myPhone = user.getPhoneNumber(); //Current logged in user phone number
        myCartRef = db.getReference(myPhone + "/mycart");

        //Check if theres anything in my cart
        myCartRef.addValueEventListener(new ValueEventListener() {
            //If there is, loop through the items found and add to myBasket list
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                myBasket = new ArrayList<>();
                int temp = 0;

                for (DataSnapshot mycart : dataSnapshot.getChildren()) {
                    MyCartDetails myCartDetails = mycart.getValue(MyCartDetails.class);
                    String prices = myCartDetails.getPrice();
                    temp = Integer.parseInt(prices) + temp;
                    myBasket.add(myCartDetails);
                }

                //Toast.makeText(getContext(), "TOTAL: " + temp, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), "Items: " + myBasket.size(), Toast.LENGTH_SHORT).show();
                if(myBasket.size() == 0) {
                    checkoutBtn.setEnabled(false);
                    /*
                    Toast toast = Toast.makeText(getContext(),"Please add items to your cart!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    */
                }
                else {
                    checkoutBtn.setEnabled(true);
                }
                totalFee.setText("Ksh: " + temp);
                totalItems.setText("Items: " + myBasket.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Toast.makeText(getContext(), progress + " km", Toast.LENGTH_SHORT).show();
                filterDistance.setText("Filter distance: " + progress + " km");

                //Synchronize the filter settings in realtime to firebase for a more personalized feel
                dbRef.child("location-filter").setValue(progress).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(getContext(), "filter posted", Toast.LENGTH_SHORT).show();


                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Write failed
                                Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //On active press of filter slider, hide recycler view and set text to loading
                recyclerview.setVisibility(v.INVISIBLE);
                emptyTag.setVisibility(v.VISIBLE);
                emptyTag.setText("Loading");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                recyclerview.setVisibility(v.VISIBLE);
                emptyTag.setText("EMPTY");

                try {
                    if (list.isEmpty()) {
                        emptyTag.setVisibility(v.VISIBLE);
                    } else {
                        emptyTag.setVisibility(v.INVISIBLE);
                    }

                    CustomerOrderAdapter recycler = new CustomerOrderAdapter(getContext(), list);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator(new SlideInLeftAnimator());

                    recycler.notifyDataSetChanged();

                    recyclerview.getItemAnimator().setAddDuration(1000);
                    recyclerview.getItemAnimator().setRemoveDuration(1000);
                    recyclerview.getItemAnimator().setMoveDuration(1000);
                    recyclerview.getItemAnimator().setChangeDuration(1000);

                    recyclerview.setAdapter(recycler);

                } catch (Exception e){

                }
            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent slideactivity = new Intent(getContext(), MyCart.class);
                Bundle bndlanimation =
                    ActivityOptions.makeCustomAnimation(getContext(), R.anim.animation,R.anim.animation2).toBundle();
                startActivity(slideactivity, bndlanimation);

            }
        });

        return  v;
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


    private void saveData(String key, String value) {
        String data = key + ":" + value;
        try {
            // Open Stream to write file.
            FileOutputStream out = getContext().openFileOutput(simpleFileName, MODE_PRIVATE);

            out.write(data.getBytes());
            out.close();
            Toast.makeText(getContext(),"File saved!",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(),"Error:"+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void readData() {
        try {
            // Open stream to read file.
            FileInputStream in = getContext().openFileInput(simpleFileName);

            BufferedReader br= new BufferedReader(new InputStreamReader(in));

            StringBuilder sb= new StringBuilder();
            String s= null;
            while((s= br.readLine())!= null)  {
                sb.append(s).append("\n");
            }
            Toast.makeText(getContext(), "Saved Data: " + sb.toString(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(getContext(),"Error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }
}