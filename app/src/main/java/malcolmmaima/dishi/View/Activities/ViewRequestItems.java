package malcolmmaima.dishi.View.Activities;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.ShoppingListAdapter;
import malcolmmaima.dishi.View.Map.GeoFireActivity;

import static malcolmmaima.dishi.R.drawable.ic_delivered_order;
import static malcolmmaima.dishi.R.drawable.ic_order_in_transit;
import static malcolmmaima.dishi.R.drawable.ic_pending_order;

public class ViewRequestItems extends AppCompatActivity {

    RecyclerView recyclerview;
    TextView customername, itemcount, distanceAway, orderStatus, totalKsh;
    Button callCustomer, acceptOrder;
    ImageView profilepic, orderStat;
    DatabaseReference incomingRequestsRef, requestStatus, customerRef, customerDelRef, myRef;
    List<OrderDetails> nduthis;
    int totalPrice;
    String status;
    FirebaseAuth mAuth;
    String[] itemPhone;

    Double custlat, custlon, myLat, myLong, dist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request_items);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getInstance().getCurrentUser() == null){

            //User is not signed in, send them back to verification page
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ViewRequestItems.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));//Load Main Activity and clear activity stack
        }

        recyclerview = findViewById(R.id.rview);
        customername = findViewById(R.id.customerName);
        itemcount = findViewById(R.id.itemCount);
        profilepic = findViewById(R.id.customerPic);
        distanceAway = findViewById(R.id.distanceAway);
        orderStatus = findViewById(R.id.orderStatus);
        orderStat = findViewById(R.id.orderStat);
        callCustomer = findViewById(R.id.callCustomer);
        acceptOrder = findViewById(R.id.acceptBtn);
        totalKsh = findViewById(R.id.totalKsh);

        totalPrice = 0;

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Shopping list");

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Go back to previous activity
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber();


        itemPhone = new String[1];
        itemPhone = getIntent().getStringArrayExtra("customer_phone");
        final String customerName = getIntent().getStringExtra("customer_name");
        final String itemCount = getIntent().getStringExtra("item_count");
        final String key = getIntent().getStringExtra("key");
        String profilePic = getIntent().getStringExtra("profile_pic");

        customername.setText(customerName);
        itemcount.setText(itemCount+" items");
        try {
            Glide.with(this).load(profilePic).into(profilepic);
        } catch (Exception e){

        }

        requestStatus = FirebaseDatabase.getInstance().getReference(myPhone + "/request_ride/"+key);
        incomingRequestsRef = FirebaseDatabase.getInstance().getReference(myPhone + "/request_menus/request_"+itemPhone[0]);
        customerRef = FirebaseDatabase.getInstance().getReference(itemPhone[0] + "/confirmed_order");
        customerDelRef = FirebaseDatabase.getInstance().getReference(itemPhone[0]);
        myRef = FirebaseDatabase.getInstance().getReference(myPhone);

        requestStatus.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                status = dataSnapshot.getValue(String.class);
                try {
                    if (status.equals("pending")) {
                        //Default state is green meaning is active and ready to receive requests
                        Glide.with(ViewRequestItems.this).load(ic_pending_order).into(orderStat);
                        orderStatus.setText("Pending");
                        acceptOrder.setEnabled(true);
                        myRef.child("engaged").setValue("false");
                    }

                    //Once customer has confirmed delivery
                    if (status.equals("confirmed")) {
                        Glide.with(ViewRequestItems.this).load(ic_delivered_order).into(orderStat);
                        orderStatus.setText("confirmed");
                        acceptOrder.setEnabled(false);
                        myRef.child("engaged").setValue("false");
                    }

                    //You have accepted the order are in transit to the customer
                    if (status.equals("transit")) {
                        Glide.with(ViewRequestItems.this).load(ic_order_in_transit).into(orderStat);
                        orderStatus.setText("transit");
                        acceptOrder.setEnabled(true);
                        acceptOrder.setText("Track");
                        myRef.child("engaged").setValue("true");
                    }


                } catch (Exception e){ }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        callCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setTitle("Call Customer")
                        .setMessage("Call " + customerName + "?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String phone = itemPhone[0];
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                                startActivity(intent);
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //do nothing

                            }
                        })//setNegativeButton

                        .create();
                myQuittingDialogBox.show();
            }
        });


        acceptOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(status.equals("transit")){
                    Intent slideactivity = new Intent(ViewRequestItems.this, GeoFireActivity.class);
                    slideactivity.putExtra("nduthi_phone", itemPhone);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                    startActivity(slideactivity, bndlanimation);
                } else {

                    final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())

                            //set message, title, and icon
                            .setTitle("Accept order")
                            .setMessage("Accept " + customerName + "'s order of "+ itemCount + " items?")
                            //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                            //set three option buttons
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //Change request status to transit
                                    customerDelRef.child("request_ride").child(key).child("status").setValue("transit");
                                    requestStatus.child("status").setValue("transit").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            incomingRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    nduthis = new ArrayList<>();
                                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                        OrderDetails orderDetails = dataSnapshot1.getValue(OrderDetails.class); //Assign values to model
                                                        orderDetails.providerName = dataSnapshot1.child("provider").getValue(String.class);
                                                        totalPrice = totalPrice + Integer.parseInt(orderDetails.getPrice());

                                                        String key = customerRef.push().getKey();
                                                        //Take the order items customer sent to me and move them to his/her confirmed order node
                                                        customerRef.child("confirmed_"+myPhone).child(key).setValue(orderDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                //Empty the customers's cart since we've moved the items to a new node after confirmation
                                                                customerDelRef.child("mycart").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        //confirmation complete
                                                                        //Toast.makeText(ViewRequestItems.this, "Confirmation sent", Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        }
                                    });

                                    requestStatus.child("notification").setValue("false");


                                }
                            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Toast.makeText(ViewRequestItems.this, "Update respective fireB nodes", Toast.LENGTH_SHORT).show();

                                }
                            })//setNegativeButton

                            .create();
                    myQuittingDialogBox.show();
                }
            }
        });

        incomingRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nduthis = new ArrayList<>();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    OrderDetails orderDetails = dataSnapshot1.getValue(OrderDetails.class); //Assign values to model
                    orderDetails.providerName = dataSnapshot1.child("provider").getValue(String.class);
                    totalPrice = totalPrice + Integer.parseInt(orderDetails.getPrice());
                    nduthis.add(orderDetails);
                    //progressDialog.dismiss();
                }

                totalKsh.setText("Total: "+totalPrice+"/=");

                //Refresh list
                if (!nduthis.isEmpty()) {
                    ShoppingListAdapter recycler = new ShoppingListAdapter(ViewRequestItems.this, nduthis);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(ViewRequestItems.this);
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator(new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);
                } else {
                    Toast.makeText(ViewRequestItems.this, "Customer has confirmed delivery!", Toast.LENGTH_LONG).show();
                    finish();
                    /*
                    ShoppingListAdapter recycler = new ShoppingListAdapter(ViewRequestItems.this, nduthis);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(ViewRequestItems.this);
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator(new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);
                    */

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());
            }


        });

        customerDelRef.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot providerLoc : dataSnapshot.getChildren()){

                    try {
                        if(providerLoc.getKey().equals("longitude")){
                            custlon = providerLoc.getValue(Double.class);
                            //Toast.makeText(getContext(), "(prov lat): " + provlon[0], Toast.LENGTH_SHORT).show();
                        }

                        if(providerLoc.getKey().equals("latitude")){
                            custlat = providerLoc.getValue(Double.class);
                            //Toast.makeText(getContext(), "(prov lon): " + provlat[0], Toast.LENGTH_SHORT).show();
                        }

                        dist = distance(custlat, custlon, myLat, myLong, "K");
                        //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                        if(dist < 1.0){
                            distanceAway.setText(dist*1000 + " m away");
                        } else {
                            distanceAway.setText(dist + " km away");
                        }


                    } catch (Exception e){
                        //Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot providerLoc : dataSnapshot.getChildren()){

                    try {
                        if(providerLoc.getKey().equals("longitude")){
                            myLong = providerLoc.getValue(Double.class);
                            //Toast.makeText(getContext(), "(prov lat): " + provlon[0], Toast.LENGTH_SHORT).show();
                        }

                        if(providerLoc.getKey().equals("latitude")){
                            myLat = providerLoc.getValue(Double.class);
                            //Toast.makeText(getContext(), "(prov lon): " + provlat[0], Toast.LENGTH_SHORT).show();
                        }

                        dist = distance(custlat, custlon, myLat, myLong, "K");
                        //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                        if(dist < 1.0){
                            distanceAway.setText(dist*1000 + " m away");
                        } else {
                            distanceAway.setText(dist + " km away");
                        }


                    } catch (Exception e){
                        //Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
    /*::	This function converts decimal degrees to radians						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts radians to decimal degrees						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts a double to N places					 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
