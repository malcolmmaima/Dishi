package malcolmmaima.dishi.View.Map;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Random;

import malcolmmaima.dishi.R;

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
    DatabaseReference myRef;
    String myPhone, accType, nduthi_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fire);

        callNduthi = findViewById(R.id.callNduthi);
        confirmOrd = findViewById(R.id.confirmOrd);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number

        nduthi_phone = getIntent().getStringExtra("nduthi_phone");


        callNduthi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setMessage("Call Nduthi?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String phone = nduthi_phone;
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

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setTitle("Confirm Order Delivery")
                        .setMessage("Has nduthi delivered your order?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Update respective fireB nodes
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

        myRef.child("account_type").addListenerForSingleValueEvent(new ValueEventListener() {
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
                mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), 2000, null);
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
                    zoomLevel = dataSnapshot.getValue(Integer.class);

                    zoomMap.setProgress(zoomLevel);
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

        mMap = googleMap;
        final DatabaseReference mylocationRef, nduthiGuyRef;
        FirebaseDatabase db;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        db = FirebaseDatabase.getInstance();
        mylocationRef = db.getReference(myPhone + "/location"); //loggedin user location reference
        nduthiGuyRef = FirebaseDatabase.getInstance().getReference(nduthi_phone + "/location");


        nduthiGuyRef.child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nduthiLat = dataSnapshot.getValue(Double.class);
                //Toast.makeText(GeoFireActivity.this, "nduthiLat: " + nduthiLat, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        nduthiGuyRef.child("longitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nduthiLng = dataSnapshot.getValue(Double.class);
                //Toast.makeText(GeoFireActivity.this, "nduthiLng: " + nduthiLat, Toast.LENGTH_LONG).show();
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

                    loggedInUserLoc = new LatLng(myLat, myLong);
                    nduthiGuyLoc = new LatLng(nduthiLat, nduthiLng);

                    myCurrent.remove(); //Remove previous marker
                    myArea.remove(); //Remove previous circle

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

                    distance = distance(nduthiGuyLoc.latitude,nduthiGuyLoc.longitude, loggedInUserLoc.latitude, loggedInUserLoc.longitude, "K");
                    //Toast.makeText(GeoFireActivity.this, "Distance: " + distance, Toast.LENGTH_SHORT).show();
                    distance = distance * 1000; //Convert distance to meters
                    //If person making delivery is within 500m radius, send notification
                    if(distance < 200 && notifSent == false){
                        sendNotification("Order is "+distance+"m away");
                        notifSent = true;
                    }
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

                    if(accType.equals("1")){//Customer
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nduthiLat,nduthiLng), zoomLevel));
                    }

                    if(accType.equals("2")) {//provider
                        //track both nduthi and customer
                    }

                    if(accType.equals("3")){//nduthi
                        //track customer
                    }


                }catch (Exception e){
                    //Toast.makeText(GeoFireActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    Log.d("dish", "GeoFireActivity: " + e);
                    loggedInUserLoc = new LatLng(-1.281647, 36.822638); //Default Nairobi
                    myCurrent = mMap.addMarker(new MarkerOptions().position(loggedInUserLoc).title("Default Location").snippet("Error fetching your location"));
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

            private void sendNotification(String s) {
                Notification.Builder builder = new Notification.Builder(GeoFireActivity.this)
                        .setSmallIcon(R.drawable.logo)
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("dishi", "GeoFireActivity: "+ databaseError);
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
