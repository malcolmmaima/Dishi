package malcolmmaima.dishi.View.Fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.Model.ReceivedOrders;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.ReceivedOrdersAdapter;
import malcolmmaima.dishi.View.Adapters.RestaurantMenuAdapter;

public class ReceivedOrdersFragment extends Fragment {

    ProgressDialog progressDialog ;
    List<ReceivedOrders> list;
    RecyclerView recyclerview;
    String myPhone;
    TextView emptyTag;

    int orders, newOrders;

    DatabaseReference dbRef, myOrdersRef;
    FirebaseDatabase db;
    FirebaseUser user;

    public static ReceivedOrdersFragment newInstance() {
        ReceivedOrdersFragment fragment = new ReceivedOrdersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.fragment_received_orders, container, false);

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
        myOrdersRef = db.getReference(myPhone + "/orders");
        recyclerview = v.findViewById(R.id.rview);
        emptyTag = v.findViewById(R.id.empty_tag);

        //Initialize orders count
        myOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orders = (int)dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Loop through the mymenu child node and get menu items, assign values to our ProductDetails model
        myOrdersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                newOrders = (int)dataSnapshot.getChildrenCount(); //Assign new orders count here

                if(newOrders > orders){
                    sendNotification("New order request");
                    orders = newOrders;
                }

                list = new ArrayList<>();
                int listSize = list.size(); //Bug fix, kept on refreshing menu on data change due to realtime location data.
                //Will use this to determine if the list of menu items has changed, only refresh then

                // StringBuffer stringbuffer = new StringBuffer();
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){
                    ReceivedOrders receivedOrders = dataSnapshot1.getValue(ReceivedOrders.class); //Assign values to model
                    receivedOrders.key = dataSnapshot1.getKey(); //Get item keys, useful when performing delete operations
                    list.add(receivedOrders);
                    //progressDialog.dismiss();
                }

                //Refresh list
                if(!list.isEmpty() && list.size() > listSize){
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    ReceivedOrdersAdapter recycler = new ReceivedOrdersAdapter(getContext(),list);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator(new SlideInLeftAnimator());
                    recyclerview.setAdapter(recycler);
                    emptyTag.setVisibility(v.INVISIBLE);
                }

                else {
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    ReceivedOrdersAdapter recycler = new ReceivedOrdersAdapter(getContext(),list);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator( new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);
                    emptyTag.setVisibility(v.VISIBLE);

                }

            }

            private void sendNotification(String s) {
                Notification.Builder builder = new Notification.Builder(getContext())
                        .setSmallIcon(R.drawable.logo)
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

                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }

                Toast.makeText(getActivity(), "Failed, " + error, Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }
}
