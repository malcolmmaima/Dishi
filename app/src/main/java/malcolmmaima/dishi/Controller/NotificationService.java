package malcolmmaima.dishi.Controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;
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

import java.util.Arrays;
import java.util.Random;

import malcolmmaima.dishi.Model.ReceivedOrders;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Map.GeoFireActivity;
import malcolmmaima.dishi.View.OrderStatus;
import malcolmmaima.dishi.View.SplashActivity;

public class NotificationService extends Service {
    private static final String TAG = "NotifService";
    private boolean isRunning  = false;
    private Looper looper;
    private NotificationServiceHandler notificationServiceHandler;
    int counter;
    boolean sent;
    FirebaseAuth mAuth;
    @Override
    public void onCreate() {
        HandlerThread handlerthread = new HandlerThread("MyThread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerthread.start();
        looper = handlerthread.getLooper();
        notificationServiceHandler = new NotificationServiceHandler(looper);
        isRunning = true;
        sent = false;
        counter = 0;

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getInstance().getCurrentUser() == null || mAuth.getInstance().getCurrentUser().getPhoneNumber() == null) {
            NotificationManager nManager = ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE));
            nManager.cancelAll();
        }

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = notificationServiceHandler.obtainMessage();
        msg.arg1 = startId;
        notificationServiceHandler.sendMessage(msg);
        //Toast.makeText(this, "Notification service Started.", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Notification service Started...");
        //If service is killed while starting, it restarts.

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
            synchronized (this) {
                while(isRunning) {
                    try {

                        isOnline();

                        Log.d(TAG, "Notification service running...");

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String myPhone = user.getPhoneNumber(); //Current logged in user phone number

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
                                                        sendNotification("Order " + receivedOrders.name + " confirmed");
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
                                                    sendNotification("Order " + receivedOrders.name + " cancelled");
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
                                                    + " has confirmed nduthi ride!");
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
                        //Log.d(TAG, e.getMessage());
                    }
                    if(!isRunning){
                        break;
                    }
                }
            }
            //stops the service for the start id.
            stopSelfResult(msg.arg1);
        }

        private void sendNotification(String s) {
            Notification.Builder builder = new Notification.Builder(NotificationService.this)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("Dishi")
                    .setContentText(s);

            NotificationManager manager = (NotificationManager)NotificationService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(getApplicationContext(), OrderStatus.class);
            PendingIntent contentIntent = PendingIntent.getActivity(NotificationService.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(contentIntent);
            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.icon |= Notification.BADGE_ICON_LARGE;

            manager.notify(new Random().nextInt(), notification);
        }
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

}