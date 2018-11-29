package malcolmmaima.dishi.View.Activities;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.OrderStatAdapter;
import malcolmmaima.dishi.View.Adapters.ShoppingListAdapter;
import malcolmmaima.dishi.View.Map.GeoFireActivity;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class OrderStatus extends AppCompatActivity {

    List<MyCartDetails> myBasket;
    List<OrderDetails> nduthiConfirmed;
    RecyclerView recyclerview, recyclerView2;
    String myPhone, str, trackRestaurant;
    TextView emptyTag, totalItems, totalFee;
    Button trackBtn;

    DatabaseReference myPendingOrders, myRef, confirmedNduthi, getConfirmedNduthi;
    FirebaseDatabase db;
    FirebaseUser user;
    String order_status;
    FirebaseAuth mAuth;
    String[] phoneNumbers, trackNduthi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        trackNduthi = new String[1];

        if (mAuth.getInstance().getCurrentUser() == null) {

            //User is not signed in, send them back to verification page
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(OrderStatus.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));//Load Main Activity and clear activity stack
        } else {

            setContentView(R.layout.activity_order_status);

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Order Status");
        order_status = "pending";

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Go back to previous activity
            }
        });
        //topToolBar.setLogo(R.drawable.logo);
        //topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));

        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number
        db = FirebaseDatabase.getInstance();

        myRef = db.getReference(myPhone);
        myPendingOrders = db.getReference(myPhone + "/pending");
        confirmedNduthi = db.getReference(myPhone + "/confirmed_order");
        getConfirmedNduthi = db.getReference(myPhone + "/confirmed_order");

        recyclerview = findViewById(R.id.rview);
        recyclerView2 = findViewById(R.id.rview2);
        emptyTag = findViewById(R.id.empty_tag);
        totalItems = findViewById(R.id.totalItems);
        totalFee = findViewById(R.id.totalFee);
        trackBtn = findViewById(R.id.trackOrder);
        trackBtn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(OrderStatus.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        //Check if theres anything in pendin node
        myPendingOrders.addValueEventListener(new ValueEventListener() {
            //If there is, loop through the items found and add to myBasket list
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    myBasket = new ArrayList<>();
                    phoneNumbers = new String[(int) dataSnapshot.getChildrenCount()];
                    int temp = 0;
                    int counter = 0;

                    for (DataSnapshot mycart : dataSnapshot.getChildren()) {
                        try {
                            MyCartDetails myCartDetails = mycart.getValue(MyCartDetails.class);
                            myCartDetails.key = mycart.getKey();
                            String prices = myCartDetails.getPrice();
                            temp = Integer.parseInt(prices) + temp;

                            phoneNumbers[counter] = myCartDetails.providerNumber;
                            counter++;

                            if (myCartDetails.status.equals("confirmed")) {
                                order_status = "confirmed";
                                trackBtn.setEnabled(true);
                            }
                            myBasket.add(myCartDetails);
                            trackRestaurant = myCartDetails.providerNumber;
                            myRef.child("active_notifications").child("active_track").setValue(trackRestaurant);
                            //Toast.makeText(OrderStatus.this, myCartDetails.getName() + " status: " + myCartDetails.status, Toast.LENGTH_SHORT).show();

                        } catch (Exception e){

                        }
                    }
                    //Toast.makeText(getContext(), "TOTAL: " + temp, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getContext(), "Items: " + myBasket.size(), Toast.LENGTH_SHORT).show();
                    if (myBasket.size() == 0) {
                        trackBtn.setEnabled(false);
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                    totalFee.setText("Ksh: " + temp);
                    totalItems.setText("Items: " + myBasket.size());

                    if (!myBasket.isEmpty()) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Collections.reverse(myBasket);
                        OrderStatAdapter recycler = new OrderStatAdapter(OrderStatus.this, myBasket);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(OrderStatus.this);
                        recyclerview.setLayoutManager(layoutmanager);
                        recyclerview.setItemAnimator(new DefaultItemAnimator());
                        recyclerview.setAdapter(recycler);
                        emptyTag.setVisibility(INVISIBLE);

                    } else {
                        OrderStatAdapter recycler = new OrderStatAdapter(OrderStatus.this, myBasket);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(OrderStatus.this);
                        recyclerview.setLayoutManager(layoutmanager);
                        recyclerview.setItemAnimator(new DefaultItemAnimator());
                        recyclerview.setAdapter(recycler);
                        emptyTag.setVisibility(VISIBLE);
                    }

                } catch (Exception e) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    emptyTag.setText("Failed");
                    emptyTag.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });

        confirmedNduthi.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    //Clean the nduthi phone number which we will use for tracking purposes in GeoFireActivity
                    str = dataSnapshot1.getKey();
                    trackNduthi[0] = str.replace("confirmed_", "");

                    myRef.child("active_notifications").child("active_track").setValue(trackNduthi[0]);

                    //Get the menu items nduthi has confirmed will deliver
                    getConfirmedNduthi.child(dataSnapshot1.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            nduthiConfirmed = new ArrayList<>();

                            int temp = 0;
                            for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                //Toast.makeText(OrderStatus.this, "dtSnap: " + dataSnapshot2.getKey(), Toast.LENGTH_LONG).show();
                                OrderDetails orderDetails = dataSnapshot2.getValue(OrderDetails.class); //Assign values to model
                                orderDetails.providerName = dataSnapshot2.child("providerName").getValue(String.class);

                                String prices = orderDetails.getPrice();
                                temp = Integer.parseInt(prices) + temp;
                                nduthiConfirmed.add(orderDetails);
                            }

                            if (nduthiConfirmed.size() == 0) {
                                trackBtn.setEnabled(false);
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                ShoppingListAdapter recycler = new ShoppingListAdapter(OrderStatus.this, nduthiConfirmed);
                                RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(OrderStatus.this);
                                recyclerView2.setLayoutManager(layoutmanager);
                                recyclerView2.setItemAnimator(new DefaultItemAnimator());
                                recyclerView2.setAdapter(recycler);

                                RecyclerView.LayoutManager layoutmanager2 = new LinearLayoutManager(OrderStatus.this);
                                recyclerview.setLayoutManager(layoutmanager2);
                                recyclerview.setItemAnimator(new DefaultItemAnimator());
                                recyclerview.setAdapter(recycler);

                                emptyTag.setVisibility(VISIBLE);
                            } else {
                                trackBtn.setEnabled(true);
                            }
                            totalFee.setText("Ksh: " + temp);
                            totalItems.setText("Items: " + nduthiConfirmed.size());


                            if (!nduthiConfirmed.isEmpty()) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                ShoppingListAdapter recycler = new ShoppingListAdapter(OrderStatus.this, nduthiConfirmed);
                                RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(OrderStatus.this);
                                recyclerView2.setLayoutManager(layoutmanager);
                                recyclerView2.setItemAnimator(new DefaultItemAnimator());
                                recyclerView2.setAdapter(recycler);
                                emptyTag.setVisibility(INVISIBLE);
                            } else {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Collections.reverse(nduthiConfirmed);
                                ShoppingListAdapter recycler = new ShoppingListAdapter(OrderStatus.this, nduthiConfirmed);
                                RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(OrderStatus.this);
                                recyclerView2.setLayoutManager(layoutmanager);
                                recyclerView2.setItemAnimator(new DefaultItemAnimator());
                                recyclerView2.setAdapter(recycler);

                                RecyclerView.LayoutManager layoutmanager2 = new LinearLayoutManager(OrderStatus.this);
                                recyclerview.setLayoutManager(layoutmanager2);
                                recyclerview.setItemAnimator(new DefaultItemAnimator());
                                recyclerview.setAdapter(recycler);

                                emptyTag.setVisibility(VISIBLE);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });


        trackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (trackNduthi.length != 0) {
                    if (trackRestaurant == null) {
                        trackRestaurant = trackNduthi[0];
                        //unable to fetch tracking codes or doesn't exist
                        Toast.makeText(OrderStatus.this, "tracking code set, try again!", Toast.LENGTH_LONG).show();
                    } else {
                        trackNduthi[0] = trackRestaurant;
                        Intent slideactivity = new Intent(OrderStatus.this, GeoFireActivity.class);
                        slideactivity.putExtra("nduthi_phone", trackNduthi);
                        slideactivity.putExtra("phoneNumbers", phoneNumbers);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation, R.anim.animation2).toBundle();
                        startActivity(slideactivity, bndlanimation);
                    }

                } else {
                    trackNduthi[0] = trackRestaurant;

                    Intent slideactivity = new Intent(OrderStatus.this, GeoFireActivity.class);
                    slideactivity.putExtra("nduthi_phone", trackNduthi);
                    slideactivity.putExtra("phoneNumbers", phoneNumbers);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation, R.anim.animation2).toBundle();
                    startActivity(slideactivity, bndlanimation);

                }

            }
        });

    }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

            if (id == R.id.cancel_order) {
               //Toast.makeText(this, "order status: "+ order_status, Toast.LENGTH_LONG).show();
                if(order_status.equals("confirmed")){
                    final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(OrderStatus.this)
                            //set message, title, and icon
                            .setTitle("Active order")
                            .setMessage("You have an active order! call the provider to cancel the order.")
                            //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                            //set three option buttons
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            })//setPositiveButton

                            .create();
                    myQuittingDialogBox.show();
                } else {

                    final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(OrderStatus.this)
                            //set message, title, and icon
                            .setTitle("Cancel Order")
                            .setMessage("Are you sure you want to cancel your entire order?")
                            //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                            //set three option buttons
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    final ProgressDialog progressDialog = new ProgressDialog(OrderStatus.this);
                                    progressDialog.setMessage("Cancelling...");
                                    progressDialog.show();

                                    myPendingOrders.addListenerForSingleValueEvent(new ValueEventListener() {
                                        //update my providers of the order cancellation
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            try {
                                                DatabaseReference provider;
                                                for (DataSnapshot mycart : dataSnapshot.getChildren()) {
                                                    final MyCartDetails myCartDetails = mycart.getValue(MyCartDetails.class);
                                                    myCartDetails.key = mycart.getKey();
                                                    myCartDetails.status = "abort";

                                                    provider = db.getReference(myCartDetails.getProviderNumber() + "/orders");
                                                    provider.child(myCartDetails.key).setValue(myCartDetails).addOnSuccessListener(new OnSuccessListener<Void>() {

                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            //Toast.makeText(OrderStatus.this, "Cancellation for " + myCartDetails.getName() + " sent to " + myCartDetails.providerNumber, Toast.LENGTH_LONG).show();

                                                            myPendingOrders.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    ShoppingListAdapter recycler = new ShoppingListAdapter(OrderStatus.this, nduthiConfirmed);
                                                                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(OrderStatus.this);
                                                                    recyclerView2.setLayoutManager(layoutmanager);
                                                                    recyclerView2.setItemAnimator(new DefaultItemAnimator());
                                                                    recyclerView2.setAdapter(recycler);

                                                                    RecyclerView.LayoutManager layoutmanager2 = new LinearLayoutManager(OrderStatus.this);
                                                                    recyclerview.setLayoutManager(layoutmanager2);
                                                                    recyclerview.setItemAnimator(new DefaultItemAnimator());
                                                                    recyclerview.setAdapter(recycler);

                                                                    emptyTag.setVisibility(VISIBLE);
                                                                    if(progressDialog.isShowing()){
                                                                        progressDialog.dismiss();
                                                                    }
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception exception) {
                                                                    if(progressDialog.isShowing()){
                                                                        progressDialog.dismiss();
                                                                    }
                                                                    // Uh-oh, an error occurred!
                                                                    Toast.makeText(OrderStatus.this, "error: " + exception, Toast.LENGTH_SHORT)
                                                                            .show();
                                                                }
                                                            });
                                                        }
                                                    });

                                                }
                                            } catch (Exception e){
                                                if(progressDialog.isShowing()){
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            if(progressDialog.isShowing()){
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });


                                    try {
                                        //Update nduthi that user has cancelled order
                                        confirmedNduthi.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot items : dataSnapshot.getChildren()){
                                                    Toast.makeText(OrderStatus.this, "items: " + items.getKey(), Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });


                                        confirmedNduthi.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                ShoppingListAdapter recycler = new ShoppingListAdapter(OrderStatus.this, nduthiConfirmed);
                                                RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(OrderStatus.this);
                                                recyclerView2.setLayoutManager(layoutmanager);
                                                recyclerView2.setItemAnimator(new DefaultItemAnimator());
                                                recyclerView2.setAdapter(recycler);

                                                RecyclerView.LayoutManager layoutmanager2 = new LinearLayoutManager(OrderStatus.this);
                                                recyclerview.setLayoutManager(layoutmanager2);
                                                recyclerview.setItemAnimator(new DefaultItemAnimator());
                                                recyclerview.setAdapter(recycler);

                                                emptyTag.setVisibility(VISIBLE);
                                                if (progressDialog.isShowing()) {
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });
                                    } catch (Exception e){

                                    }
                                }
                            })//setPositiveButton


                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //Do not delete
                                    //Toast.makeText(OrderStatus.this, "No", Toast.LENGTH_SHORT).show();

                                }
                            })//setNegativeButton

                            .create();
                    myQuittingDialogBox.show();
                }
            }

        return super.onOptionsItemSelected(item);
    }

}

