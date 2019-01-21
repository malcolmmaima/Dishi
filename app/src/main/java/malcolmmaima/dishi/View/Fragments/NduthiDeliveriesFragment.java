package malcolmmaima.dishi.View.Fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Random;

import malcolmmaima.dishi.Model.RequestNduthi;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.DeliveryRequestsNduthi;

public class NduthiDeliveriesFragment extends Fragment {

    ProgressDialog progressDialog ;
    List<RequestNduthi> list;
    RecyclerView recyclerview;
    String myPhone;
    TextView emptyTag;

    int notifications, newNotifications;

    DatabaseReference dbRef, myRef;
    FirebaseDatabase db;

    FirebaseUser user;


    public static NduthiDeliveriesFragment newInstance() {
        NduthiDeliveriesFragment fragment = new NduthiDeliveriesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_nduthi_deliveries, container, false);
        // Assigning Id to ProgressDialog.
        //progressDialog = new ProgressDialog(getContext());
        // Setting progressDialog Title.
        //progressDialog.setTitle("Loading...");
        // Showing progressDialog.
        //progressDialog.show();
        //progressDialog.setCancelable(false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number
        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference(myPhone);
        recyclerview = v.findViewById(R.id.rview);
        emptyTag = v.findViewById(R.id.empty_tag);

        //Initialize notifications counter
        dbRef.child("request_ride").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notifications = (int) dataSnapshot.getChildrenCount();
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        dbRef.child("request_ride").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Check if there are new ride requests
                newNotifications = (int) dataSnapshot.getChildrenCount();

                //If there is a new ride request, send notification and update notifications count
                if(newNotifications > notifications){
                    sendNotification("new ride request");
                    notifications = newNotifications;
                }

                list = new ArrayList<>();
                int listSize = list.size(); //Bug fix, kept on refreshing menu on data change due to realtime location data.
                //Will use this to determine if the list of menu items has changed, only refresh then

                // StringBuffer stringbuffer = new StringBuffer();
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){
                    RequestNduthi requestNduthi = dataSnapshot1.getValue(RequestNduthi.class); //Assign values to model
                    requestNduthi.key = dataSnapshot1.getKey();
                    list.add(requestNduthi);
                    //progressDialog.dismiss();
                }

                if(!list.isEmpty() && list.size() > listSize){
                    DeliveryRequestsNduthi recycler = new DeliveryRequestsNduthi(getContext(),list);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator( new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);
                    emptyTag.setVisibility(v.INVISIBLE);
                }

                else {
                    DeliveryRequestsNduthi recycler = new DeliveryRequestsNduthi(getContext(),list);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator( new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);
                    emptyTag.setVisibility(v.VISIBLE);

                }

            }

            private void sendNotification(String s) {
                Notification.Builder builder = new Notification.Builder(getContext())
                        .setSmallIcon(R.drawable.logo_notification)
                        .setContentTitle("Dishi")
                        .setContentText(s);

                try {
                    NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent intent = new Intent(getContext(), getContext().getClass());
                    PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
                    builder.setContentIntent(contentIntent);
                    Notification notification = builder.build();
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    notification.defaults |= Notification.DEFAULT_SOUND;
                    notification.icon |= Notification.BADGE_ICON_LARGE;

                    manager.notify(new Random().nextInt(), notification);
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());

            }
        });

        return  v;
    }
}