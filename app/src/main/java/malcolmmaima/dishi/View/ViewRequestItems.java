package malcolmmaima.dishi.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import malcolmmaima.dishi.Model.NduthiNearMe;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.Model.RequestNduthi;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.CustomerOrderAdapter;
import malcolmmaima.dishi.View.Adapters.NduthiAdapter;
import malcolmmaima.dishi.View.Adapters.ShoppingListAdapter;

import static malcolmmaima.dishi.R.drawable.ic_order_in_transit;

public class ViewRequestItems extends AppCompatActivity {

    RecyclerView recyclerview;
    TextView customername, itemcount, distanceAway, orderStatus, totalKsh;
    Button callCustomer, acceptOrder;
    ImageView profilepic;
    DatabaseReference incomingRequestsRef, requestStatus;
    List<OrderDetails> nduthis;
    int totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request_items);

        recyclerview = findViewById(R.id.rview);
        customername = findViewById(R.id.customerName);
        itemcount = findViewById(R.id.itemCount);
        profilepic = findViewById(R.id.customerPic);
        distanceAway = findViewById(R.id.distanceAway);
        orderStatus = findViewById(R.id.orderStatus);
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
        String myPhone = user.getPhoneNumber();

        final String itemPhone = getIntent().getStringExtra("customer_phone");
        final String customerName = getIntent().getStringExtra("customer_name");
        final String itemCount = getIntent().getStringExtra("item_count");
        String key = getIntent().getStringExtra("key");
        String profilePic = getIntent().getStringExtra("profile_pic");

        customername.setText(customerName);
        itemcount.setText(itemCount+" items");
        try {
            Glide.with(this).load(profilePic).into(profilepic);
        } catch (Exception e){

        }
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
                                String phone = itemPhone;
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

        requestStatus = FirebaseDatabase.getInstance().getReference(myPhone + "/request_ride/"+key);

        acceptOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())

                        //set message, title, and icon
                        .setTitle("Accept order")
                        .setMessage("Accept " + customerName + "'s order of "+ itemCount + " items?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(ViewRequestItems.this, "Update respective fireB nodes", Toast.LENGTH_SHORT).show();

                                //Change request status to transit
                                requestStatus.child("status").setValue("transit").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(ViewRequestItems.this, "Update respective fireB nodes", Toast.LENGTH_SHORT).show();

                            }
                        })//setNegativeButton

                        .create();
                myQuittingDialogBox.show();
            }
        });

        incomingRequestsRef = FirebaseDatabase.getInstance().getReference(myPhone + "/request_menus/request_"+itemPhone);

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
                    ShoppingListAdapter recycler = new ShoppingListAdapter(ViewRequestItems.this, nduthis);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(ViewRequestItems.this);
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator(new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());

                Toast.makeText(ViewRequestItems.this, "Failed, " + error, Toast.LENGTH_SHORT).show();
            }


        });
    }
}
