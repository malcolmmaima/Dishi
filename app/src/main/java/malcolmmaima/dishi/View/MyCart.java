package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.NduthiNearMe;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.MyCartAdapter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MyCart extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    List<MyCartDetails> myBasket;
    List<NduthiNearMe> nduthiNearMeList;
    RecyclerView recyclerview;
    String myPhone, paymentType;
    TextView emptyTag, totalItems, totalFee;
    Button checkoutBtn;
    Spinner payMethod;

    DatabaseReference myCartRef, providerRef, myPendingOrders, myRef, nduthisRef, currentOrderRef;
    FirebaseDatabase db;
    FirebaseUser user;

    boolean multiple_providers, completeOrder, avail_nduthi;
    Double distance, myLat, myLong, nduthiLat, nduthiLong;
    ProgressDialog progressDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cart);

        // Assigning Id to ProgressDialog.
        progressDialog = new ProgressDialog(MyCart.this);

        multiple_providers = false;
        completeOrder = false;

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("My Cart");

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Go back to previous activity
                /*
                Intent cartActivity = new Intent(MyCart.this, MyAccountCustomer.class);
                cartActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//Load MyCart Activity and clear activity stack
                startActivity(cartActivity); */
            }
        });
        //topToolBar.setLogo(R.drawable.logo);
        //topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));

        user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number
        db = FirebaseDatabase.getInstance();

        myCartRef = db.getReference(myPhone + "/mycart");
        myRef = db.getReference(myPhone);
        myPendingOrders = db.getReference(myPhone + "/pending");
        nduthisRef = db.getReference();
        currentOrderRef = db.getReference(myPhone + "/confirmed_order");

        recyclerview = findViewById(R.id.rview);
        emptyTag = findViewById(R.id.empty_tag);
        totalItems = findViewById(R.id.totalItems);
        totalFee = findViewById(R.id.totalFee);
        checkoutBtn = findViewById(R.id.checkoutBtn);
        payMethod = findViewById(R.id.payType);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.pay_via, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        payMethod.setAdapter(adapter);
        payMethod.setOnItemSelectedListener(this);

        //Check if theres anything in my cart
        myCartRef.addValueEventListener(new ValueEventListener() {
            //If there is, loop through the items found and add to myBasket list
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                multiple_providers = false;

                try {
                myBasket = new ArrayList<>();
                int temp = 0;
                int counter = 0;
                String[] phoneNumbers = new String[(int) dataSnapshot.getChildrenCount()];


                for (DataSnapshot mycart : dataSnapshot.getChildren()) {
                    MyCartDetails myCartDetails = mycart.getValue(MyCartDetails.class);
                    myCartDetails.key = mycart.getKey();
                    //Toast.makeText(MyCart.this, "Number: " + myCartDetails.providerNumber, Toast.LENGTH_SHORT).show();
                    String prices = myCartDetails.getPrice();
                    temp = Integer.parseInt(prices) + temp;
                    myBasket.add(myCartDetails);

                    //Need to capture the phone numbers of the providers in the cart and store in string array, if user has ordered from
                    //Multiple providers, then will be prompted to search for nearby nduthi to fulfil the order
                    for(DataSnapshot mycart2 : mycart.getChildren()){

                        //Toast.makeText(MyCart.this, "mycart2: "+mycart2.getValue(), Toast.LENGTH_SHORT).show();

                        if(mycart2.getKey().equals("providerNumber")){

                            phoneNumbers[counter] = myCartDetails.providerNumber;

                            //Toast.makeText(MyCart.this, "phoneNumbers["+counter+"] = "+phoneNumbers[counter], Toast.LENGTH_SHORT).show();

                            counter = counter + 1;

                        }
                    }

                }

                //compare phone numbers, immediately pattern of providers in cart is broken, then prompt user to use nduthi service
                for(int i = 0; i<phoneNumbers.length; i++){
                    if(i > 0){
                        if(phoneNumbers[i].equals(phoneNumbers[i-1])){

                            //current phone number and previous are the same, keep checking
                            //Toast.makeText(MyCart.this, "phoneNumbers["+i+"] = "
                            //        +phoneNumbers[i]+" == phoneNumbers["+(i-1)+"] = " +phoneNumbers[i-1], Toast.LENGTH_SHORT).show();
                        }
                        else {//Pattern broken, menu items in cart are from different providers

                            //Toast.makeText(MyCart.this, "phoneNumbers["+i+"] = "
                            //        +phoneNumbers[i]+" != phoneNumbers["+(i-1)+"] = " +phoneNumbers[i-1], Toast.LENGTH_SHORT).show();
                            multiple_providers = true;
                        }
                    }
                }

                //Toast.makeText(getContext(), "TOTAL: " + temp, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), "Items: " + myBasket.size(), Toast.LENGTH_SHORT).show();
                if(myBasket.size() == 0) {
                    checkoutBtn.setEnabled(false);
                    /*
                    Toast toast = Toast.makeText(getContext(),"Please add items to your cart!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    */
                }
                else {
                    checkoutBtn.setEnabled(true);
                }
                totalFee.setText("Ksh: " + temp);
                totalItems.setText("Items: " + myBasket.size());

                    if (!myBasket.isEmpty()) {
                        MyCartAdapter recycler = new MyCartAdapter(MyCart.this, myBasket);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(MyCart.this);
                        recyclerview.setLayoutManager(layoutmanager);
                        recyclerview.setItemAnimator(new DefaultItemAnimator());
                        recyclerview.setAdapter(recycler);
                        emptyTag.setVisibility(INVISIBLE);
                    } else {
                        MyCartAdapter recycler = new MyCartAdapter(MyCart.this, myBasket);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(MyCart.this);
                        recyclerview.setLayoutManager(layoutmanager);
                        recyclerview.setItemAnimator(new DefaultItemAnimator());
                        recyclerview.setAdapter(recycler);
                        emptyTag.setVisibility(VISIBLE);

                    }

            } catch (Exception e){
                emptyTag.setText("Failed");
                emptyTag.setVisibility(VISIBLE);
                    Toast.makeText(MyCart.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final FirebaseDatabase db;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        db = FirebaseDatabase.getInstance();
        final DatabaseReference mylocationRef = db.getReference(myPhone + "/location"); //loggedin user location reference

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(MyCart.this);
                progressDialog.setMessage("processing...");
                progressDialog.setCancelable(true);
                progressDialog.show();
                //First check if there's an ongoing delivery confirmed by nduthi guy taking place
                currentOrderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChildren()){
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                            final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(MyCart.this)
                                    //set message, title, and icon
                                    .setTitle("Active order")
                                    .setMessage("You have an active order being delivered. Check delivery status.")
                                    //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                                    //set three option buttons
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {

                                        }
                                    })//setPositiveButton

                                    .create();
                            myQuittingDialogBox.show();
                        }
                        else {
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }

                            if (paymentType.equals("empty")) {
                                Toast.makeText(MyCart.this, "You must select payment method", Toast.LENGTH_SHORT).show();

                            } else {

                                if (multiple_providers == true) {
                                    myPendingOrders.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChildren()){
                                                //There exists pending orders
                                                //Toast.makeText(MyCart.this, "You're about to order from multiple providers!", Toast.LENGTH_SHORT).show();
                                                nduthisNearby(); //Initialize nduthisNearby() search
                                                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(MyCart.this)
                                                        //set message, title, and icon
                                                        .setTitle("Active order")
                                                        .setMessage("You have pending orders!")
                                                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                                                        //set three option buttons
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {


                                                            }
                                                        })//setPositiveButton

                                                        .create();
                                                myQuittingDialogBox.show();
                                            } else {
                                                //No pending orders

                                                //Toast.makeText(MyCart.this, "You're about to order from multiple providers!", Toast.LENGTH_SHORT).show();
                                                nduthisNearby(); //Initialize nduthisNearby() search
                                                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(MyCart.this)
                                                        //set message, title, and icon
                                                        .setTitle("Search Nduthi")
                                                        .setMessage("You're about to order from multiple providers. Search Nduthi nearby to fulfil the orders")
                                                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                                                        //set three option buttons
                                                        .setCancelable(false)
                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {

                                                                nduthisNearby();
                                                                // Setting progressDialog Title.
                                                                progressDialog.setMessage("Searching...");
                                                                // Showing progressDialog.
                                                                progressDialog.setCancelable(false);
                                                                progressDialog.show();

                                                                completeOrder = true;

                                                                if(nduthisNearby() == false){
                                                                    progressDialog.dismiss();
                                                                }

                                                            }
                                                        })//setPositiveButton


                                                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                                Toast.makeText(MyCart.this, "Orders from multiple providers must be fulfilled by a nduthi", Toast.LENGTH_LONG).show();

                                                            }
                                                        })//setNegativeButton

                                                        .create();
                                                myQuittingDialogBox.show();


                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                } else {
                                    //First check if there's an ongoing delivery confirmed by nduthi guy taking place
                                    currentOrderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChildren()){
                                                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(MyCart.this)
                                                        //set message, title, and icon
                                                        .setTitle("Active order")
                                                        .setMessage("You have an active order being delivered. Check delivery status.")
                                                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                                                        //set three option buttons
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {

                                                            }
                                                        })//setPositiveButton

                                                        .create();
                                                myQuittingDialogBox.show();
                                            }
                                            else { //No active nduthi delivery, allow new order
                                                //Toast.makeText(MyCart.this, "no children", Toast.LENGTH_SHORT).show();

                                                progressDialog.setMessage("Please wait...");
                                                progressDialog.setCancelable(false);
                                                progressDialog.show();

                                                //Check if theres anything in my cart
                                                myCartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    //If there is, loop through the items found and start sending the orders
                                                    @Override
                                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                                        try {

                                                            final String[] customerName = {""};

                                                            final int[] remainingOrders = {(int) dataSnapshot.getChildrenCount()}; //Need to keep track of each order successfully sent
                                                            //Toast.makeText(MyCart.this, "Items: " + remainingOrders[0], Toast.LENGTH_SHORT).show();

                                                            myRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    customerName[0] = dataSnapshot.getValue(String.class); //My name will be sent to provider with my order
                                                                    //Toast.makeText(MyAccountRestaurant.this, "Welcome " + account_name, Toast.LENGTH_LONG).show();
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {
                                                                    if(progressDialog.isShowing()){
                                                                        progressDialog.dismiss();
                                                                    }
                                                                    Toast.makeText(MyCart.this, "Error: " + databaseError.toString() + ". Try again!", Toast.LENGTH_LONG).show();
                                                                }
                                                            });


                                                            for (final DataSnapshot mycart : dataSnapshot.getChildren()) {
                                                                final MyCartDetails myCartDetails = mycart.getValue(MyCartDetails.class);
                                                                myCartDetails.key = mycart.getKey();
                                                                myCartDetails.customerNumber = myPhone;
                                                                myCartDetails.status = "pending";
                                                                myCartDetails.payType = paymentType;

                                                                //Post the orders to the respective providers and have them confirm orders
                                                                providerRef = db.getReference(myCartDetails.getProviderNumber() + "/orders");

                                                                //add order to respective provider nodes
                                                                providerRef.child(myCartDetails.key).setValue(myCartDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        remainingOrders[0] = remainingOrders[0] - 1;

                                                                        //After successfully sending the order to each provider, add it to my pending orders node
                                                                        myPendingOrders.child(myCartDetails.key).setValue(myCartDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                // After successfully appending my orders to the pending node, remove it from mycart
                                                                                myCartRef.child(myCartDetails.key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {

                                                                                        //Toast.makeText(MyCart.this, "Items: " + remainingOrders[0], Toast.LENGTH_SHORT).show();
                                                                                        if (remainingOrders[0] == 0) {
                                                                                            Snackbar snackbar = Snackbar
                                                                                                    .make(findViewById(R.id.parentlayout), "Orders sent! Check Order status", Snackbar.LENGTH_LONG);

                                                                                            snackbar.show();
                                                                                            if(snackbar.isShown()){
                                                                                                if(progressDialog.isShowing()){
                                                                                                    progressDialog.dismiss();
                                                                                                    Intent slideactivity = new Intent(MyCart.this, OrderStatus.class);
                                                                                                    Bundle bndlanimation =
                                                                                                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                                                                                                    startActivity(slideactivity, bndlanimation);
                                                                                                }
                                                                                            }

                                                                                        }

                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception exception) {
                                                                                        if(progressDialog.isShowing()){
                                                                                            progressDialog.dismiss();
                                                                                        }
                                                                                        // Uh-oh, an error occurred!
                                                                                        Toast.makeText(MyCart.this, "Error: " + exception, Toast.LENGTH_SHORT)
                                                                                                .show();
                                                                                    }
                                                                                });
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception exception) {
                                                                                // Uh-oh, an error occurred!
                                                                                Toast.makeText(MyCart.this, "Error: " + exception, Toast.LENGTH_SHORT)
                                                                                        .show();
                                                                            }
                                                                        });

                                                                    }
                                                                })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                // Write failed
                                                                                Toast.makeText(MyCart.this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                                                            }
                                                                        });
                                                            }


                                                        } catch (Exception e) {

                                                            emptyTag.setText("Failed");
                                                            emptyTag.setVisibility(VISIBLE);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        if(progressDialog.isShowing()){
                                                            progressDialog.dismiss();
                                                        }
                                                        Toast.makeText(MyCart.this, "Error: " + databaseError, Toast.LENGTH_SHORT).show();
                                                    }
                                                } );
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                        Toast.makeText(MyCart.this, "Error: " + databaseError, Toast.LENGTH_LONG).show();
                    }
                });

        }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_save){
            Toast.makeText(MyCart.this, "Save Cart", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean nduthisNearby(){
        progressDialog.setMessage("Searching...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        ///////
        //Loop through all the users
        nduthisRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    nduthiNearMeList = new ArrayList<>();

                    // StringBuffer stringbuffer = new StringBuffer();

                    //So first we loop through the users in the firebase db
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        //Toast.makeText(MyCart.this, "User: " + dataSnapshot1.getKey()
                        //+ " is of account type: " + dataSnapshot1.child("account_type").getValue(), Toast.LENGTH_SHORT).show();

                        //And get users of account type = 3 (Nduthi account)
                        if(dataSnapshot1.child("account_type").getValue().equals("3")){
                            //Toast.makeText(MyCart.this, "User: " + dataSnapshot1.getKey()
                            //       + " is nduthi", Toast.LENGTH_SHORT).show();

                            NduthiNearMe nduthiNearMe = new NduthiNearMe();

                            nduthiNearMe.name = dataSnapshot1.child("name").getValue(String.class);
                            nduthiNearMe.profilepic = dataSnapshot1.child("profilepic").getValue(String.class);
                            nduthiNearMe.bio = dataSnapshot1.child("bio").getValue(String.class);
                            nduthiNearMe.email = dataSnapshot1.child("email").getValue(String.class);
                            nduthiNearMe.gender = dataSnapshot1.child("gender").getValue(String.class);

                            DatabaseReference nduthiRef = db.getReference(dataSnapshot1.getKey().toString() + "/location");

                            //Nduthi latitude longitude coordinates
                            nduthiRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot nduthiCords : dataSnapshot.getChildren()) {
                                        if (nduthiCords.getKey().equals("latitude")) {
                                            nduthiLat = nduthiCords.getValue(Double.class);
                                        }

                                        if (nduthiCords.getKey().equals("longitude")) {
                                            nduthiLong = nduthiCords.getValue(Double.class);
                                        }
                                    }

                                    try {
                                        //Calculate distance between nduthi and customer
                                        distance = distance(myLat, myLong, nduthiLat, nduthiLong, "K");
                                        distance = distance * 1000; //Convert to meters
                                    } catch (Exception e){

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    if(progressDialog.isShowing()){
                                        progressDialog.dismiss();
                                    }
                                }
                            });

                            int nduthiSize = nduthiNearMeList.size();
                            if(nduthiSize == 0){
                                if(progressDialog.isShowing()){
                                    progressDialog.dismiss();
                                }
                            }
                            String key = myRef.push().getKey();
                            //Search within a 1km radius for nduthis, if you're from USIU it's motorbike... my bad
                            if(distance <= 1000){
                                nduthiNearMeList.add(nduthiNearMe);
                                myRef.child("nearby_nduthis").child(dataSnapshot1.getKey().toString()).setValue(nduthiNearMe).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Toast.makeText(MyCart.this, "Added: " + nduthiNearMeList.size() + " nduthis", Toast.LENGTH_SHORT).show();
                                        avail_nduthi = true;
                                    }
                                });
                            }

                            else {
                                if(progressDialog.isShowing()){
                                    progressDialog.dismiss();
                                }

                                //Toast.makeText(MyCart.this, "No nduthi near you!", Toast.LENGTH_LONG).show();
                                avail_nduthi = false;

                                Toast.makeText(MyCart.this, "No nduthi near you!", Toast.LENGTH_SHORT).show();

                            }
                            //Toast.makeText(MyCart.this, "nduthiNearMeList size: " + nduthiNearMeList.size(), Toast.LENGTH_SHORT).show();
                        }

                    }


                } catch (Exception e){
                    //Toast.makeText(MyCart.this, e.toString(), Toast.LENGTH_SHORT).show();
                }

                if(nduthiNearMeList.size() != 0 && completeOrder == true){
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }

                    Intent slideactivity = new Intent(MyCart.this, SelectNduthiGuy.class);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.animation,R.anim.animation2).toBundle();
                    startActivity(slideactivity, bndlanimation);
                    completeOrder = false;
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());

                progressDialog.dismiss();

                Toast.makeText(MyCart.this, "Failed, " + error, Toast.LENGTH_SHORT).show();
            }


        });
        //////
        return avail_nduthi;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0){
            paymentType = "empty";
        }
        if(position == 1){
            paymentType = "mpesa";
        }

        if(position == 2){
            paymentType = "cash";
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

