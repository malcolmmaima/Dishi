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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import java.util.ArrayList;
import java.util.List;

import malcolmmaima.dishi.Model.DishiUser;
import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.CustomerOrderAdapter;
import malcolmmaima.dishi.View.Adapters.MyCartAdapter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MyCart extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    List<MyCartDetails> myBasket;
    RecyclerView recyclerview;
    String myPhone, paymentType;
    TextView emptyTag, totalItems, totalFee;
    Button checkoutBtn;
    Spinner payMethod;

    DatabaseReference myCartRef, providerRef, myPendingOrders, myRef;
    FirebaseDatabase db;
    FirebaseUser user;

    boolean multiple_providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cart);

        multiple_providers = false;

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

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (paymentType.equals("empty")) {
                    Toast.makeText(MyCart.this, "You must select payment method", Toast.LENGTH_SHORT).show();

                } else {

                    if (multiple_providers == true) {
                        Toast.makeText(MyCart.this, "You're about to order from multiple providers!", Toast.LENGTH_SHORT).show();

                        final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(MyCart.this)
                                //set message, title, and icon
                                .setTitle("Search Nduthi")
                                .setMessage("You're about to order from multiple providers. Search Nduthi nearby to fulfil the orders")
                                //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                                //set three option buttons
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Toast.makeText(MyCart.this, "Search nduthi activity", Toast.LENGTH_SHORT).show();
                                    }
                                })//setPositiveButton


                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Toast.makeText(MyCart.this, "Orders from multiple providers must be filfilled by a nduthi", Toast.LENGTH_LONG).show();

                                    }
                                })//setNegativeButton

                                .create();
                        myQuittingDialogBox.show();


                    } else {
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
                                                                    //Redirect to track orders map
                                                                    Toast toast = Toast.makeText(MyCart.this, "Redirect to realtime track order map", Toast.LENGTH_LONG);
                                                                    toast.setGravity(Gravity.CENTER, 0, 1);
                                                                    toast.show();

                                                                    Snackbar snackbar = Snackbar
                                                                            .make(findViewById(R.id.parentlayout), "Orders sent", Snackbar.LENGTH_LONG);

                                                                    snackbar.show();
                                                                }

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
                                Toast.makeText(MyCart.this, "Error: " + databaseError, Toast.LENGTH_SHORT).show();
                            }
                        });

                }
            }

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
}

