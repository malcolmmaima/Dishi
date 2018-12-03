package malcolmmaima.dishi.View.Fragments;

import android.app.ProgressDialog;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import malcolmmaima.dishi.Model.ReceivedOrders;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.ReceivedOrdersAdapter;

public class ConfirmedDeliveriesFragment extends Fragment {

    ProgressDialog progressDialog ;
    List<ReceivedOrders> list;
    RecyclerView recyclerview;
    String myPhone;
    TextView emptyTag,totalItems, totalFee;

    DatabaseReference dbRef, myDeliveries, deliveryHistory;
    FirebaseDatabase db;
    FirebaseUser user;

    public static ConfirmedDeliveriesFragment newInstance() {
        ConfirmedDeliveriesFragment fragment = new ConfirmedDeliveriesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.fragment_confirmed_deliveries, container, false);

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
        myDeliveries = db.getReference(myPhone + "/deliveries");
        deliveryHistory = db.getReference(myPhone + "/history");
        recyclerview = v.findViewById(R.id.rview);
        emptyTag = v.findViewById(R.id.empty_tag);
        totalFee = v.findViewById(R.id.totalFee);
        totalItems = v.findViewById(R.id.totalItems);

        //Loop through the deliveries child node and get menu items, assign values to our POJO model
        myDeliveries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list = new ArrayList<>();
                int listSize = list.size(); //Bug fix, kept on refreshing menu on data change due to realtime location data.
                //Will use this to determine if the list of menu items has changed, only refresh then

                // StringBuffer stringbuffer = new StringBuffer();
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){
                    final ReceivedOrders receivedOrders = dataSnapshot1.getValue(ReceivedOrders.class); //Assign values to model
                    if(receivedOrders.status.equals("delivered")){
                        //delete from deliveries node and append to history node
                        receivedOrders.key = dataSnapshot1.getKey(); //Get item keys, useful when performing delete operations
                        deliveryHistory.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //delete
                                myDeliveries.child(receivedOrders.key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //successful
                                    }
                                });
                            }
                        });
                    } else {
                        list.add(receivedOrders);
                    }
                    //progressDialog.dismiss();
                }

                //Refresh list
                if(!list.isEmpty() && list.size() > listSize){
                    if(progressDialog.isShowing()){
                        try {
                            progressDialog.dismiss();
                        } catch (Exception e){

                        }
                    }
                    Collections.reverse(list);
                    ReceivedOrdersAdapter recycler = new ReceivedOrdersAdapter(getContext(),list);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator( new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);
                    emptyTag.setVisibility(v.INVISIBLE);
                }

                else {
                    if(progressDialog.isShowing()){
                        try {
                            progressDialog.dismiss();
                        } catch (Exception e){

                        }
                    }
                    ReceivedOrdersAdapter recycler = new ReceivedOrdersAdapter(getContext(),list);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator( new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);
                    emptyTag.setVisibility(v.VISIBLE);

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());

                if(progressDialog.isShowing()){
                    try {
                        progressDialog.dismiss();
                    } catch (Exception e){

                    }
                }
            }
        });

        //Check if theres anything in my cart
        myDeliveries.addValueEventListener(new ValueEventListener() {
            //If there is, loop through the items found and add to myBasket list
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                list = new ArrayList<>();
                int temp = 0;

                for (DataSnapshot mydel : dataSnapshot.getChildren()) {
                    ReceivedOrders receivedOrders = mydel.getValue(ReceivedOrders.class);
                    String prices = receivedOrders.getPrice();
                    temp = Integer.parseInt(prices) + temp;
                    list.add(receivedOrders);
                }

                //Toast.makeText(getContext(), "TOTAL: " + temp, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), "Items: " + myBasket.size(), Toast.LENGTH_SHORT).show();
                totalFee.setText("Ksh: " + temp);
                totalItems.setText("Items: " + list.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }
}
