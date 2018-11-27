package malcolmmaima.dishi.View.Map;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.Model.ReceivedOrders;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.ReceivedOrdersAdapter;
import malcolmmaima.dishi.View.MainActivity;
import malcolmmaima.dishi.View.MyAccountCustomer;
import malcolmmaima.dishi.View.MyAccountNduthi;
import malcolmmaima.dishi.View.MyCart;
import malcolmmaima.dishi.View.OrderStatus;

public class GeoFireActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button callNduthi, confirmOrd;
    double myLat, myLong;
    LatLng loggedInUserLoc, nduthiGuyLoc;
    Marker myCurrent, providerCurrent;
    Circle myArea;
    Double distance;
    int zoomLevel;
    Double nduthiLat, nduthiLng;
    boolean notifSent = false;
    VerticalSeekBar zoomMap;
    DatabaseReference myRef, pendingOrders, providerRef, providerOrderHistory, ordersHistory;
    String myPhone, accType, message, callMsg, temp;
    ProgressDialog progressDialog;
    String[] phoneNumbers, phoneNames, nduthiNumber, nduthi_phone;
    List<String> names;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getInstance().getCurrentUser() == null){

            //User is not signed in, send them back to verification page
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(GeoFireActivity.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));//Load Main Activity and clear activity stack
        }

        try {
            nduthiNumber = getIntent().getStringArrayExtra("nduthi_phone");
            //Toast.makeText(this, "track number: " + nduthiNumber[0], Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            //Toast.makeText(GeoFireActivity.this, "no nduthi code", Toast.LENGTH_SHORT).show();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fire);
        progressDialog = new ProgressDialog(GeoFireActivity.this);

        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        callNduthi = findViewById(R.id.callNduthi);
        confirmOrd = findViewById(R.id.confirmOrd);

        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            myPhone = user.getPhoneNumber(); //Current logged in user phone number
        } catch (Exception e){

        }

        nduthi_phone = new String[1];
        nduthi_phone = getIntent().getStringArrayExtra("nduthi_phone");

        pendingOrders = FirebaseDatabase.getInstance().getReference(myPhone + "/pending");
        ordersHistory = FirebaseDatabase.getInstance().getReference(myPhone + "/history");

        message = "Order delivered?";
        callMsg = "Call?";

        callNduthi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setMessage(callMsg)
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String phone = nduthi_phone[0];
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

        confirmOrd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //final int totalDeliveryPeeps = phoneNames.length + nduthiNumber.length;
                final int totalDeliveryPeeps = nduthiNumber.length; //Bug fix, will revisit...
                final String [] nduthi = new String[1];
                final String [] allNames = new String[totalDeliveryPeeps];

                //Get nduthi guy name
                FirebaseDatabase.getInstance().getReference(nduthiNumber[0]).child("name").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        nduthi[0] = dataSnapshot.getValue(String.class);
                        //Toast.makeText(GeoFireActivity.this, "ndthi code: "+ nduthi[0], Toast.LENGTH_SHORT).show();
                        if(!nduthi[0].isEmpty()){

                            //Create a general list with all active delivery peeps names
                            for(int i = 0; i < totalDeliveryPeeps; i++ ){
                                if(i < phoneNames.length){
                                    allNames[i] = phoneNames[i];
                                }
                                if(i == phoneNames.length){
                                    allNames[i] = nduthi[0];
                                }

                                if(phoneNames.length == 0 && nduthiNumber.length != 0){
                                    allNames[i] = nduthi[0];
                                }

                                //Toast.makeText(GeoFireActivity.this, "Allnames: " + allNames[i], Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setTitle("Confirm Order Delivery")
                        .setMessage(message)
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                if(allNames.length == 0){
                                    Toast.makeText(GeoFireActivity.this, "Name list is empty!", Toast.LENGTH_SHORT).show();
                                } else {

                                    //Toast.makeText(GeoFireActivity.this, "Nduthi code: " + nduthiNumber[0], Toast.LENGTH_SHORT).show();
                                    final ArrayList mSelectedItems = new ArrayList();  // Where we track the selected items
                                    AlertDialog.Builder builder = new AlertDialog.Builder(GeoFireActivity.this);
                                    // Set the dialog title
                                    builder.setTitle("Delivered Successfully")
                                            .setCancelable(false)
                                            .setIcon(R.drawable.nduthi_guy)
                                            // Specify the list array, the items to be selected by default (null for none),
                                            // and the listener through which to receive callbacks when items are selected
                                            .setMultiChoiceItems(allNames, null,
                                                    new DialogInterface.OnMultiChoiceClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which,
                                                                            boolean isChecked) {
                                                            if (isChecked) {
                                                                // If the user checked the item, add it to the selected items
                                                                mSelectedItems.add(which);
                                                                //Toast.makeText(GeoFireActivity.this, "phone: "+ phoneNumbers[which], Toast.LENGTH_SHORT).show();

                                                            } else if (mSelectedItems.contains(which)) {
                                                                // Else, if the item is already in the array, remove it
                                                                mSelectedItems.remove(Integer.valueOf(which));
                                                            }
                                                        }
                                                    })
                                            // Set the action buttons
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id) {

                                                    if (mSelectedItems.size() == 0) {
                                                        Toast.makeText(GeoFireActivity.this, "You must select a name", Toast.LENGTH_LONG).show();
                                                    }
                                                    else {
                                                        final ProgressDialog progressDialog = new ProgressDialog(GeoFireActivity.this);
                                                        progressDialog.setMessage("Processing...");
                                                        progressDialog.setCancelable(false);
                                                        progressDialog.show();
                                                        //Loop through all the selected items list
                                                        for (int i = 0; i < mSelectedItems.size(); i++) {
                                                            //Toast.makeText(GeoFireActivity.this, phoneNames[(int)mSelectedItems.get(i)]+" success!", Toast.LENGTH_SHORT).show();
                                                            try {
                                                                providerRef = FirebaseDatabase.getInstance().getReference(phoneNumbers[(int) mSelectedItems.get(i)] + "/deliveries");
                                                            } catch (Exception e) {

                                                            }

                                                            try {
                                                                FirebaseDatabase.getInstance().getReference(myPhone).child("confirmed_order").child("confirmed_" + nduthiNumber[0]).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        for (DataSnapshot orders : dataSnapshot.getChildren()) {
                                                                            OrderDetails orderDetails = orders.getValue(OrderDetails.class); //Assign values to model
                                                                            orderDetails.providerName = orders.child("providerName").getValue(String.class);
                                                                            orderDetails.key = orders.getKey();
                                                                            //Move already delivered order to history db node
                                                                            ordersHistory.child(orderDetails.key).setValue(orderDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    //Then delete from active order node
                                                                                    FirebaseDatabase.getInstance().getReference(myPhone).child("confirmed_order").child("confirmed_" + nduthiNumber[0]).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            progressDialog.dismiss();
                                                                                            Toast.makeText(GeoFireActivity.this, "Enjoy your order fam!", Toast.LENGTH_LONG).show();
                                                                                            finish();
                                                                                        }
                                                                                    });
                                                                                    FirebaseDatabase.getInstance().getReference(nduthiNumber[0] + "/request_ride").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            for (DataSnapshot dt : dataSnapshot.getChildren()) {
                                                                                                if (dt.getKey().equals(myPhone)) {
                                                                                                    //Toast.makeText(GeoFireActivity.this, "Delete request nduthi node!", Toast.LENGTH_SHORT).show();
                                                                                                    FirebaseDatabase.getInstance().getReference(nduthiNumber[0] + "/request_ride").child(myPhone).removeValue();
                                                                                                    FirebaseDatabase.getInstance().getReference(nduthiNumber[0] + "/request_menus").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                        @Override
                                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                            for (DataSnapshot dt2 : dataSnapshot.getChildren()) {
                                                                                                                if (dt2.getKey().equals("request_" + myPhone)) {
                                                                                                                    //Toast.makeText(GeoFireActivity.this, "Delete request menu node!", Toast.LENGTH_SHORT).show();
                                                                                                                    FirebaseDatabase.getInstance().getReference(nduthiNumber[0] + "/request_menus").child("request_" + myPhone).removeValue();
                                                                                                                    finish();
                                                                                                                }
                                                                                                            }
                                                                                                        }

                                                                                                        @Override
                                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

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
                                                            } catch (Exception e) {

                                                            }

                                                            try {
                                                                providerOrderHistory = FirebaseDatabase.getInstance().getReference(phoneNumbers[(int) mSelectedItems.get(i)] + "/history_deliveries");
                                                            } catch (Exception e) {

                                                            }
                                                            //check in my pending node for items
                                                            pendingOrders.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                    for (final DataSnapshot orderStat : dataSnapshot.getChildren()) {
                                                                        final MyCartDetails myCartDetails = orderStat.getValue(MyCartDetails.class);

                                                                        //If item status is confirmed, means it is in transit
                                                                        if (myCartDetails.status.equals("confirmed")) {
                                                                            myCartDetails.key = orderStat.getKey();
                                                                            myCartDetails.status = "delivered";
                                                                            myCartDetails.sent = true;

                                                                            //Update provider's node
                                                                            providerRef.child(myCartDetails.key).setValue(myCartDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    //Create copy of order history for provider
                                                                                    providerOrderHistory.child(myCartDetails.key).setValue(myCartDetails);

                                                                                    //Move already delivered order to history db node
                                                                                    ordersHistory.child(myCartDetails.key).setValue(myCartDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            //then delete it from pending orders node
                                                                                            pendingOrders.child(myCartDetails.key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {

                                                                                                    progressDialog.dismiss();
                                                                                                    confirmOrd.setEnabled(false);
                                                                                                    Snackbar snackbar = Snackbar
                                                                                                            .make(findViewById(R.id.parentlayout), "Enjoy your order fam!", Snackbar.LENGTH_INDEFINITE)
                                                                                                            .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                                                                                                            .setAction("FINISH", new View.OnClickListener() {
                                                                                                                @Override
                                                                                                                public void onClick(View view) {
                                                                                                                    /*
                                                                                                                    Intent slideactivity = new Intent(GeoFireActivity.this, MyAccountCustomer.class)
                                                                                                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                                    Bundle bndlanimation =
                                                                                                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                                                                                                    startActivity(slideactivity, bndlanimation);
                                                                                                                    */

                                                                                                                    finish();
                                                                                                                }
                                                                                                            });

                                                                                                    snackbar.show();
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                        } else {//Order of the ticked delivery individual has not been confirmed
                                                                            //Toast.makeText(GeoFireActivity.this, myCartDetails.provider
                                                                            //        + " has not confirmed your order for " + myCartDetails.getName(), Toast.LENGTH_SHORT).show();

                                                                        }

                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                                        }

                                                }
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id) {
                                                }
                                            });

                                    builder.create();
                                    builder.show();
                                }


                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //update respective fireB nodes

                            }
                        })//setNegativeButton

                        .create();
                myQuittingDialogBox.show();

            }
        });

        //Get logged in user account type
        myRef = FirebaseDatabase.getInstance().getReference(myPhone);

        myRef.child("account_type").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                accType = dataSnapshot.getValue(String.class);
                //Toast.makeText(GeoFireActivity.this, "accType: " + accType, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Track Nduthi");

        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        zoomMap = findViewById(R.id.verticalSeekbar);
        zoomMap.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                //Synchronize the filter settings in realtime to firebase for a more personalized feel
                zoomLevel = progress;
                try {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), 2000, null);
                } catch (Exception e){

                }
                myRef.child("zoom_filter").setValue(progress).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Write failed
                                Toast.makeText(GeoFireActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                            }
                        });

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        try {
            myRef.child("zoom_filter").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        zoomLevel = dataSnapshot.getValue(Integer.class);

                        zoomMap.setProgress(zoomLevel);
                    } catch (Exception e){
                        zoomLevel = 15;
                        zoomMap.setProgress(zoomLevel);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            }); } catch (Exception e){
            Log.d("dishi", "GeoFireActivity: "+ e);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        confirmOrd.setEnabled(false);
        callNduthi.setEnabled(false);

        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        names = new ArrayList<String>();

        mMap = googleMap;
        final DatabaseReference mylocationRef;
        final DatabaseReference[] nduthiGuyRef = new DatabaseReference[1];
        FirebaseDatabase db;

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number


        db = FirebaseDatabase.getInstance();
        mylocationRef = db.getReference(myPhone + "/location"); //loggedin user location reference
        nduthiGuyRef[0] = FirebaseDatabase.getInstance().getReference(nduthi_phone[0] + "/location");


        nduthiGuyRef[0].child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nduthiLat = dataSnapshot.getValue(Double.class);
                //Toast.makeText(GeoFireActivity.this, "nduthiLat: " + nduthiLat, Toast.LENGTH_LONG).show();
                try {
                    track();
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        nduthiGuyRef[0].child("longitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nduthiLng = dataSnapshot.getValue(Double.class);
                //Toast.makeText(GeoFireActivity.this, "nduthiLng: " + nduthiLat, Toast.LENGTH_LONG).show();
                try {
                    track();
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("dishi", "GeoFireActivity: " + databaseError);
            }
        });

        //My latitude longitude coordinates
        mylocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot myCords : dataSnapshot.getChildren()){
                    if(myCords.getKey().equals("latitude")){
                        myLat = myCords.getValue(Double.class);
                    }

                    if(myCords.getKey().equals("longitude")){
                        myLong = myCords.getValue(Double.class);
                    }
                }

                //Toast.makeText(GeoFireActivity.this, "lat: "+ myLat + " long: " + myLong, Toast.LENGTH_SHORT).show();
                try {
                    track();
                } catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void track() {
        if(accType.equals("1")) {//Customer
            try {
                loggedInUserLoc = new LatLng(myLat, myLong);
                nduthiGuyLoc = new LatLng(nduthiLat, nduthiLng);

                distance = distance(nduthiGuyLoc.latitude, nduthiGuyLoc.longitude, loggedInUserLoc.latitude, loggedInUserLoc.longitude, "K");
                //Toast.makeText(GeoFireActivity.this, "Distance: " + distance, Toast.LENGTH_SHORT).show();
                distance = distance * 1000; //Convert distance to meters
            } catch(Exception e){

            }

            try{
                phoneNumbers = getIntent().getStringArrayExtra("phoneNumbers");

                if(phoneNumbers == null){
                    phoneNumbers = getIntent().getStringArrayExtra("nduthi_phone");
                }


                /*
                 * convert array to list and then add all
                 * elements to LinkedHashSet. LinkedHashSet
                 * will automatically remove all duplicate elements.
                 */
                LinkedHashSet<String> phones =
                        new LinkedHashSet<String>(Arrays.asList(phoneNumbers));

                //create array from the LinkedHashSet
                String[] newArray = phones.toArray(new String[ phones.size() ]);

                phoneNumbers = newArray;

                DatabaseReference phoneNamesRef;
                phoneNames = new String[phoneNumbers.length];

                for(int i = 0; i< phoneNumbers.length; i++){
                    phoneNamesRef = FirebaseDatabase.getInstance().getReference(phoneNumbers[i]);

                    phoneNamesRef.child("name").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.getValue(String.class);
                            if(!names.contains(name)){
                                names.add(name);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                phoneNames = names.toArray(new String[names.size()]);

            }catch (Exception e){}

            try {

                confirmOrd.setEnabled(true);
                callNduthi.setEnabled(true);
                //If person making delivery is within 500m radius, send notification
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                message = "Has nduthi or provider delivered your order?";
                callMsg = "Call delivery guy?";
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nduthiLat, nduthiLng), zoomLevel));
                if (distance == 10 || distance == 60 || distance == 120 || distance < 10) {
                    //sendNotification("Order is " + distance + "m away");

                    //FirebaseDatabase.getInstance().getReference(myPhone).child("active_notifications")
                    //        .child("active_order").child("message").setValue("Order is " + distance + "m away");
                    //FirebaseDatabase.getInstance().getReference(myPhone)
                    //        .child("active_notifications").child("active_order").child("phone").setValue(nduthiNumber[0]);
                }

                myCurrent.remove(); //Remove previous marker
                myArea.remove(); //Remove previous circle

                providerCurrent.remove();

                providerCurrent = mMap.addMarker(new MarkerOptions().position(nduthiGuyLoc).title("Nduthi")
                        .snippet("Extra info")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.nduthi_guy))
                        .flat(true));

                myCurrent = mMap.addMarker(new MarkerOptions().position(loggedInUserLoc).title("My Location"));

                //Radius around my area
                myArea = mMap.addCircle(new CircleOptions().center(loggedInUserLoc)
                        .radius(200)//in meters
                        .strokeColor(Color.BLUE)
                        .fillColor(0x220000FF)
                        .strokeWidth(5.0f));

            } catch (Exception e){
                //Toast.makeText(GeoFireActivity.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                confirmOrd.setEnabled(false);
                callNduthi.setEnabled(false);
                //Toast.makeText(GeoFireActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("dish", "GeoFireActivity: " + e);
                loggedInUserLoc = new LatLng(-1.281647, 36.822638); //Default Nairobi
                myCurrent = mMap.addMarker(new MarkerOptions().position(loggedInUserLoc).title("Default Location").snippet("Error fetching your location"));
                providerCurrent = mMap.addMarker(new MarkerOptions().position(loggedInUserLoc).title("Default Location").snippet("Error fetching your location"));
                //Radius around my area
                myArea = mMap.addCircle(new CircleOptions().center(loggedInUserLoc)
                        .radius(500)//in meters
                        .strokeColor(Color.BLUE)
                        .fillColor(0x220000FF)
                        .strokeWidth(5.0f));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-1.281647, 36.822638), zoomLevel));
            }
        }

        if(accType.equals("2")) {//provider
            try {
                loggedInUserLoc = new LatLng(myLat, myLong);
                nduthiGuyLoc = new LatLng(nduthiLat, nduthiLng);

                distance = distance(nduthiGuyLoc.latitude, nduthiGuyLoc.longitude, loggedInUserLoc.latitude, loggedInUserLoc.longitude, "K");
                //Toast.makeText(GeoFireActivity.this, "Distance: " + distance, Toast.LENGTH_SHORT).show();
                distance = distance * 1000; //Convert distance to meters
            } catch(Exception e){

            }

            FirebaseDatabase.getInstance().getReference(myPhone + "/deliveries").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // StringBuffer stringbuffer = new StringBuffer();
                    for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){
                        final ReceivedOrders receivedOrders = dataSnapshot1.getValue(ReceivedOrders.class); //Assign values to model
                        if(receivedOrders.status.equals("delivered")){
                            Toast.makeText(GeoFireActivity.this, "Customer has confirmed delivery!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    //  Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
            setTitle("Track Customer");
            confirmOrd.setVisibility(View.INVISIBLE);
            try {
                confirmOrd.setEnabled(true);
                callNduthi.setEnabled(true);
                message = "Have you successfully made the delivery?";
                callMsg = "Call customer?";
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nduthiLat, nduthiLng), zoomLevel));
                if (distance < 200 && notifSent == false) {
                    sendNotification("Customer is " + distance + "m away");
                    notifSent = true;
                }

                loggedInUserLoc = new LatLng(myLat, myLong);
                nduthiGuyLoc = new LatLng(nduthiLat, nduthiLng);

                myCurrent.remove(); //Remove previous marker
                myArea.remove(); //Remove previous circle

                providerCurrent.remove();

                providerCurrent = mMap.addMarker(new MarkerOptions().position(loggedInUserLoc).title("My Location")
                        .snippet("Extra info")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.nduthi_guy))
                        .flat(true));

                myCurrent = mMap.addMarker(new MarkerOptions().position(nduthiGuyLoc).title("Customer Location"));

                //Radius around my area
                myArea = mMap.addCircle(new CircleOptions().center(nduthiGuyLoc)
                        .radius(200)//in meters
                        .strokeColor(Color.BLUE)
                        .fillColor(0x220000FF)
                        .strokeWidth(5.0f));

            } catch (Exception e){
                confirmOrd.setEnabled(false);
                callNduthi.setEnabled(false);
                //Toast.makeText(GeoFireActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("dish", "GeoFireActivity: " + e);
                loggedInUserLoc = new LatLng(-1.281647, 36.822638); //Default Nairobi
                myCurrent = mMap.addMarker(new MarkerOptions().position(loggedInUserLoc).title("Default Location").snippet("Error fetching your location"));
                providerCurrent = mMap.addMarker(new MarkerOptions().position(loggedInUserLoc).title("Default Location").snippet("Error fetching your location"));

                //Radius around my area
                myArea = mMap.addCircle(new CircleOptions().center(loggedInUserLoc)
                        .radius(500)//in meters
                        .strokeColor(Color.BLUE)
                        .fillColor(0x220000FF)
                        .strokeWidth(5.0f));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-1.281647, 36.822638), zoomLevel));
            }
        }

        if(accType.equals("3")) {//nduthi

            try {
                loggedInUserLoc = new LatLng(myLat, myLong);
                nduthiGuyLoc = new LatLng(nduthiLat, nduthiLng);

                distance = distance(nduthiGuyLoc.latitude, nduthiGuyLoc.longitude, loggedInUserLoc.latitude, loggedInUserLoc.longitude, "K");
                //Toast.makeText(GeoFireActivity.this, "Distance: " + distance, Toast.LENGTH_SHORT).show();
                distance = distance * 1000; //Convert distance to meters
            } catch(Exception e){

            }
            setTitle("Track Customer");
            confirmOrd.setVisibility(View.INVISIBLE);
            try {
                confirmOrd.setEnabled(true);
                callNduthi.setEnabled(true);
                message = "Have you successfully made the delivery?";
                callMsg = "Call customer?";

                if (distance < 200 && notifSent == false) {
                    sendNotification("Customer is " + distance + "m away");
                    notifSent = true;
                }
                myCurrent.remove();
                providerCurrent.remove();
                myArea.remove();

                myCurrent = mMap.addMarker(new MarkerOptions().position(loggedInUserLoc).title("My Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.nduthi_guy))
                        .flat(true));

                providerCurrent = mMap.addMarker(new MarkerOptions().position(nduthiGuyLoc).title("Customer Location")
                        .snippet("Extra info"));

                //Radius around customer's area
                myArea = mMap.addCircle(new CircleOptions().center(nduthiGuyLoc)
                        .radius(200)//in meters
                        .strokeColor(Color.BLUE)
                        .fillColor(0x220000FF)
                        .strokeWidth(5.0f));
                //track customer
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nduthiLat, nduthiLng), zoomLevel));

            } catch (Exception e){
                confirmOrd.setEnabled(false);
                callNduthi.setEnabled(false);
                //Toast.makeText(GeoFireActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("dish", "GeoFireActivity: " + e);
                loggedInUserLoc = new LatLng(-1.281647, 36.822638); //Default Nairobi
                myCurrent = mMap.addMarker(new MarkerOptions().position(loggedInUserLoc).title("Default Location").snippet("Error fetching your location"));
                providerCurrent = mMap.addMarker(new MarkerOptions().position(loggedInUserLoc).title("Default Location").snippet("Error fetching your location"));

                //Radius around my area
                myArea = mMap.addCircle(new CircleOptions().center(loggedInUserLoc)
                        .radius(500)//in meters
                        .strokeColor(Color.BLUE)
                        .fillColor(0x220000FF)
                        .strokeWidth(5.0f));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-1.281647, 36.822638), zoomLevel));
            }
        }
    }

    private void sendNotification(String s) {
        Notification.Builder builder = new Notification.Builder(GeoFireActivity.this)
                .setSmallIcon(R.drawable.logo_notification)
                .setContentTitle("Dishi")
                .setContentText(s);

        NotificationManager manager = (NotificationManager)GeoFireActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(GeoFireActivity.this, GeoFireActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(GeoFireActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.icon |= Notification.BADGE_ICON_LARGE;

        manager.notify(new Random().nextInt(), notification);
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
