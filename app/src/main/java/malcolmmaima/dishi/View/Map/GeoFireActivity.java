package malcolmmaima.dishi.View.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
    double myLat, myLong;
    LatLng myLocation;
    LatLng orderLocation;
    Marker myCurrent, providerCurrent;
    Circle myArea;
    Double distance;
    int zoomLevel;
    VerticalSeekBar zoomMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fire);

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
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                zoomLevel = progress;
                mMap.animateCamera(CameraUpdateFactory.zoomTo(progress), 2000, null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        final DatabaseReference mylocationRef, providerRef, myCartRef, dbRef;
        FirebaseDatabase db;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        db = FirebaseDatabase.getInstance();
        mylocationRef = db.getReference(myPhone + "/location"); //loggedin user location reference

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

                    myLocation = new LatLng(myLat, myLong);
                    orderLocation = new LatLng(-1.391996,36.8186076);

                    myCurrent.remove();
                    myArea.remove();

                    providerCurrent = mMap.addMarker(new MarkerOptions().position(orderLocation).title("Nduthi"));
                    myCurrent = mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));

                    //Radius around my area
                    myArea = mMap.addCircle(new CircleOptions().center(myLocation)
                            .radius(500)//in meters
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(5.0f));

                    distance = distance(orderLocation.latitude,orderLocation.longitude, myLocation.latitude, myLocation.longitude, "K");
                    //Toast.makeText(GeoFireActivity.this, "Distance: " + distance, Toast.LENGTH_SHORT).show();
                    distance = distance * 1000; //Convert distance to meters
                    //If person making delivery is within 500m radius, send notification
                    if(distance < 500){
                        sendNotification("Order is "+distance+"m away");
                    }
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLat,myLong), zoomLevel));

                }catch (Exception e){
                    //Toast.makeText(GeoFireActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    myLocation = new LatLng(-34, 151);
                    myCurrent = mMap.addMarker(new MarkerOptions().position(myLocation).title("Default Location"));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLat,myLong), zoomLevel));
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
