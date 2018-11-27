package malcolmmaima.dishi.Controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.util.Arrays;
import java.util.Random;

import malcolmmaima.dishi.Model.ReceivedOrders;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Map.GeoFireActivity;
import malcolmmaima.dishi.View.MyAccountNduthi;
import malcolmmaima.dishi.View.MyAccountRestaurant;
import malcolmmaima.dishi.View.OrderStatus;
import malcolmmaima.dishi.View.SplashActivity;

public class NotificationService extends Service {
    private static final String TAG = "NotifService";
    private boolean isRunning  = false;
    private Looper looper;
    private NotificationServiceHandler notificationServiceHandler;
    int counter;
    boolean sent = false;
    boolean arrived = false;
    FirebaseAuth mAuth;
    String trackPhone;

    Double nduthiLat, nduthiLng, myLat, myLong, distance;
    Double current;

    String myPhone;

    int orders, newOrders;

    DatabaseReference dbRef, myOrdersRef;
    FirebaseDatabase db;
    FirebaseUser user;

    @Override
    public void onCreate() {
        HandlerThread handlerthread = new HandlerThread("MyThread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerthread.start();
        looper = handlerthread.getLooper();
        notificationServiceHandler = new NotificationServiceHandler(looper);
        isRunning = true;

        counter = 0;

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getInstance().getCurrentUser() == null || mAuth.getInstance().getCurrentUser().getPhoneNumber() == null) {
            NotificationManager nManager = ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE));
            nManager.cancelAll();
        }

        current = 0.0;

        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference(myPhone);
        myOrdersRef = db.getReference(myPhone + "/orders");


    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = notificationServiceHandler.obtainMessage();
        msg.arg1 = startId;
        notificationServiceHandler.sendMessage(msg);
        //Toast.makeText(this, "Notification service Started.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Notification service Started...");

        //Initialize orders count
        myOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    orders = (int) dataSnapshot.getChildrenCount();
                    //Toast.makeText(NotificationService.this, "Orders: " + orders, Toast.LENGTH_SHORT).show();
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //If service is killed while starting, it restarts.

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number
        final DatabaseReference mylocationRef;
        final DatabaseReference[] nduthiGuyRef = new DatabaseReference[1];


        FirebaseDatabase.getInstance().getReference(myPhone).child("active_notifications")
                .child("active_order").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String message = "";
                String[] phone = new String[1];
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        if(dataSnapshot1.getKey().equals("message")){
                            message = dataSnapshot1.getValue(String.class);
                        }

                        if(dataSnapshot1.getKey().equals("phone")){
                            phone[0] = dataSnapshot1.getValue(String.class);
                        }
                    }

                    if(arrived != true){
                        sendActiveOrderNotification(message, phone);
                        arrived = true;
                        trackPhone = ""; //Empty the tracking code and stop tracking
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mylocationRef = FirebaseDatabase.getInstance().getReference(myPhone + "/location"); //loggedin user location reference

        FirebaseDatabase.getInstance().getReference(myPhone).child("active_notifications")
                .child("active_track").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trackPhone = dataSnapshot.getValue(String.class);
                //Toast.makeText(NotificationService.this, "Track: " + trackPhone, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        nduthiGuyRef[0] = FirebaseDatabase.getInstance().getReference(trackPhone + "/location");

        nduthiGuyRef[0].child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nduthiLat = dataSnapshot.getValue(Double.class);
                //Toast.makeText(NotificationService.this, "nduthiLat: " + nduthiLat, Toast.LENGTH_LONG).show();

                try {

                    distance = distance(nduthiLat, nduthiLng, myLat, myLong, "K");
                    //Toast.makeText(GeoFireActivity.this, "Distance: " + distance, Toast.LENGTH_SHORT).show();
                    distance = distance * 1000; //Convert distance to meters


                    if (distance < 60 && current != distance && arrived != true) {
                        //sendNotification("Order is " + distance + "m away");
                        Toast.makeText(NotificationService.this, "Order is: " + distance + "m away!", Toast.LENGTH_SHORT).show();

                        current = distance;
                    }
                    if(distance == 20.0 && arrived != true){
                        FirebaseDatabase.getInstance().getReference(myPhone).child("active_notifications")
                                .child("active_order").child("message").setValue("Your order has arrived!");
                        FirebaseDatabase.getInstance().getReference(myPhone)
                                .child("active_notifications").child("active_order").child("phone").setValue(trackPhone);
                    }
                } catch(Exception e){

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
                //Toast.makeText(NotificationService.this, "nduthiLng: " + nduthiLat, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("dishi", "GeoFireActivity: " + databaseError);
            }
        });

        FirebaseDatabase.getInstance().getReference(myPhone + "/pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren()){
                    FirebaseDatabase.getInstance().getReference(myPhone)
                            .child("active_notifications").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Toast.makeText(NotificationService.this, "Tracking code deleted!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //My latitude longitude coordinates
        mylocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot myCords : dataSnapshot.getChildren()) {
                    if (myCords.getKey().equals("latitude")) {
                        myLat = myCords.getValue(Double.class);
                    }

                    if (myCords.getKey().equals("longitude")) {
                        myLong = myCords.getValue(Double.class);
                    }

                    //Toast.makeText(NotificationService.this, "myLat: " + myLat + " mylng: " + myLong, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        isRunning = true; //for testing purposes, revert to false in production
        //Toast.makeText(this, "Notification service Completed or Stopped.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Notification service Stopped...");
    }


    private final class NotificationServiceHandler extends Handler {
        public NotificationServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final String myPhone = user.getPhoneNumber(); //Current logged in user phone number
            synchronized (this) {
                while(isRunning) {

                    dbRef.child("account_type").addValueEventListener(new ValueEventListener() {
                        @Override public void onDataChange(DataSnapshot dataSnapshot) {
                            String account_type = dataSnapshot.getValue(String.class);
                            //String account_type = Integer.toString(acc_type);

                            if (account_type.equals("1")) { //Customer account

                            }

                            else if(account_type.equals("2")){
                                myOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        try {
                                            newOrders = (int) dataSnapshot.getChildrenCount(); //Assign new orders count here

                                            if (newOrders > orders) {
                                                int count = orders + 1;
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                                    //Implement notifications for android version O
                                                    Toast.makeText(NotificationService.this, "New order Request", Toast.LENGTH_LONG).show();
                                                else
                                                    sendNotification("New order request(" + count + ")", "MyAccountRestaurant");
                                                orders = newOrders;
                                                //Toast.makeText(NotificationService.this, "New order Request("+orders +")", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                orders = newOrders;
                                            }
                                        } catch (Exception e){
                                            Toast.makeText(NotificationService.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            else if(account_type.equals("3")){

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    try {

                        isOnline();

                        Log.d(TAG, "Notification service running...");


                        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(myPhone + "/pending");
                        final DatabaseReference reqRide = FirebaseDatabase.getInstance().getReference(myPhone + "/request_ride");

                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                sent = false;
                                for(DataSnapshot orders : dataSnapshot.getChildren()){
                                    final ReceivedOrders receivedOrders = orders.getValue(ReceivedOrders.class);
                                        if(receivedOrders.status.equals("confirmed") && receivedOrders.sent == false ) {
                                            try {

                                                receivedOrders.sent = true;
                                                ref.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        sendNotification("Order " + receivedOrders.name + " confirmed", "MyAccountCustomer");
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {

                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Write failed
                                                    }
                                                });

                                            } catch (Exception e){

                                            }


                                        }

                                    else if(receivedOrders.status.equals("cancelled") && receivedOrders.sent == false ) {
                                        try {

                                            receivedOrders.sent = true;
                                            ref.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    sendNotification("Order " + receivedOrders.name + " cancelled", "MyAccountCustomer");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {

                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Write failed
                                                }
                                            });

                                        } catch (Exception e){

                                        }


                                    }
                                    }
                                }



                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        //Check to see if nduthi has confirmed order request
                        reqRide.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                try {
                                    for(DataSnapshot rideStat : dataSnapshot.getChildren()){
                                        if(rideStat.child("status").getValue().equals("transit") && rideStat.child("notification").getValue().equals("false")){
                                            sendNotification(rideStat.child("name").getValue()
                                                    + " has confirmed nduthi ride!", "MyAccountNduthi");
                                            reqRide.child(rideStat.getKey()).child("notification").setValue("true");
                                        }
                                    }
                                } catch (Exception e){

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        Thread.sleep(1000);
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                    }
                    if(!isRunning){
                        break;
                    }
                }
            }
            //stops the service for the start id.
            stopSelfResult(msg.arg1);
        }

        private void sendNotification(String s, String activity) {
            Notification.Builder builder = new Notification.Builder(NotificationService.this)
                    .setSmallIcon(R.drawable.logo_notification)
                    .setContentTitle("Dishi")
                    .setContentText(s);

            if(activity.equals("MyAccountRestaurant")){
                NotificationManager manager = (NotificationManager)NotificationService.this.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent = new Intent(getApplicationContext(), MyAccountRestaurant.class);
                PendingIntent contentIntent = PendingIntent.getActivity(NotificationService.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                builder.setContentIntent(contentIntent);
                Notification notification = builder.build();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.defaults |= Notification.DEFAULT_SOUND;
                notification.icon |= Notification.BADGE_ICON_LARGE;

                manager.notify(new Random().nextInt(), notification);
            }

            else if(activity.equals("MyAccountNduthi")){
                NotificationManager manager = (NotificationManager)NotificationService.this.getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent = new Intent(getApplicationContext(), MyAccountNduthi.class);
                PendingIntent contentIntent = PendingIntent.getActivity(NotificationService.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                builder.setContentIntent(contentIntent);
                Notification notification = builder.build();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.defaults |= Notification.DEFAULT_SOUND;
                notification.icon |= Notification.BADGE_ICON_LARGE;

                manager.notify(new Random().nextInt(), notification);
            }



        }

    }

    private void sendActiveOrderNotification(String s, String[] phone){
        Notification.Builder builder = new Notification.Builder(NotificationService.this)
                .setSmallIcon(R.drawable.logo_notification)
                .setContentTitle("Dishi")
                .setContentText(s);

        NotificationManager manager = (NotificationManager)NotificationService.this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(getApplicationContext(), GeoFireActivity.class);
        intent.putExtra("nduthi_phone", phone);

        PendingIntent contentIntent = PendingIntent.getActivity(NotificationService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.icon |= Notification.BADGE_ICON_LARGE;

        manager.notify(new Random().nextInt(), notification);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        FirebaseDatabase.getInstance().getReference(myPhone).child("active_notifications").removeValue();


    }

    private void isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            //is connected
        } else {
            Toast.makeText(NotificationService.this, "You are not connected to the internet", Toast.LENGTH_SHORT).show();
        }
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