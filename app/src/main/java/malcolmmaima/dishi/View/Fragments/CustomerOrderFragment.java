package malcolmmaima.dishi.View.Fragments;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import malcolmmaima.dishi.Model.DishiUser;
import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.CustomerOrderAdapter;
import malcolmmaima.dishi.View.AddMenu;
import malcolmmaima.dishi.View.MyCart;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;


import static android.content.Context.MODE_PRIVATE;

public class CustomerOrderFragment extends Fragment {

    private String simpleFileName = "appdata.txt";

    ProgressDialog progressDialog ;
    List<OrderDetails> list;
    List<MyCartDetails> myBasket;
    List<DishiUser> users;
    RecyclerView recyclerview;
    String myPhone;
    TextView emptyTag, totalItems, totalFee, filterDistance;
    Button checkoutBtn;
    SeekBar seekBar;

    DatabaseReference dbRef, menusRef;
    FirebaseDatabase db;

    FirebaseUser user;


    public static CustomerOrderFragment newInstance() {
        CustomerOrderFragment fragment = new CustomerOrderFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_customer_order, container, false);
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
        menusRef = db.getReference();

        recyclerview = v.findViewById(R.id.rview);
        emptyTag = v.findViewById(R.id.empty_tag);
        totalItems = v.findViewById(R.id.totalItems);
        totalFee = v.findViewById(R.id.totalFee);
        checkoutBtn = v.findViewById(R.id.checkoutBtn);
        seekBar = v.findViewById(R.id.seekBar);
        filterDistance = v.findViewById(R.id.filterDistance);

        checkoutBtn.setEnabled(false);

        final int[] initial_filter = new int[1];

        dbRef.child("location-filter").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int location_filter = dataSnapshot.getValue(Integer.class);
                initial_filter[0] = location_filter;
                //Toast.makeText(context, "Fetch: " + location_filter, Toast.LENGTH_SHORT).show();
                seekBar.setProgress(location_filter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Toast.makeText(getContext(), progress + " km", Toast.LENGTH_SHORT).show();
                filterDistance.setText("Filter distance: " + progress + " km");

                dbRef.child("location-filter").setValue(progress).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(getContext(), "filter posted", Toast.LENGTH_SHORT).show();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Write failed
                                Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Loop through the mymenu child node and get menu items, assign values to our ProductDetails model
        menusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    list = new ArrayList<>();
                    users = new ArrayList<>();

                    // StringBuffer stringbuffer = new StringBuffer();

                    //So first we loop through the users in the firebase db
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        //DishiUser dishiUser = dataSnapshot1.getValue(DishiUser.class); //Assign values to model
                        //Toast.makeText(getContext(), "User: " + dishiUser.getName(), Toast.LENGTH_SHORT).show();

                        //afterwards we check if that user has a 'mymenu' child node, if so loop through it and show the products
                        //NOTE: only restaurant/provider accounts have the 'mymenu', so essentially we are fetching restaurant menus into our customers fragment via the adapter
                        for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("mymenu").getChildren()) {
                            OrderDetails orderDetails = dataSnapshot2.getValue(OrderDetails.class);
                            //Toast.makeText(getContext(), "mymenu: " + dataSnapshot2.getKey(), Toast.LENGTH_SHORT).show();
                            orderDetails.providerNumber = dataSnapshot1.getKey();
                            orderDetails.providerName = dataSnapshot1.child("name").getValue().toString();
                            list.add(orderDetails);
                        }
                        //Toast.makeText(getContext(), "Phone: " + dataSnapshot1.getKey(), Toast.LENGTH_SHORT).show(); //Phone numbers

                    }

                    final int[] location_filter = new int[1];
                    dbRef.child("location-filter").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            location_filter[0] = dataSnapshot.getValue(Integer.class);

                            //if(initial_filter[0] != location_filter[0]){
                              //  Toast.makeText(getContext(), "initial: "+initial_filter[0] + " current: "+ location_filter[0], Toast.LENGTH_SHORT).show();
                            //}

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    if (!list.isEmpty() && initial_filter[0] != location_filter[0]) {
                        CustomerOrderAdapter recycler = new CustomerOrderAdapter(getContext(), list);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                        recyclerview.setLayoutManager(layoutmanager);
                        recyclerview.setItemAnimator(new SlideInLeftAnimator());

                        recycler.notifyDataSetChanged();

                        recyclerview.getItemAnimator().setAddDuration(1000);
                        recyclerview.getItemAnimator().setRemoveDuration(1000);
                        recyclerview.getItemAnimator().setMoveDuration(1000);
                        recyclerview.getItemAnimator().setChangeDuration(1000);

                        recyclerview.setAdapter(recycler);
                        emptyTag.setVisibility(v.INVISIBLE);

                    } else {
                        CustomerOrderAdapter recycler = new CustomerOrderAdapter(getContext(), list);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                        recyclerview.setLayoutManager(layoutmanager);
                        recyclerview.setItemAnimator(new DefaultItemAnimator());
                        recyclerview.setAdapter(recycler);
                        emptyTag.setVisibility(v.VISIBLE);

                    }
                } catch (Exception e){
                    emptyTag.setText("Failed");
                    emptyTag.setVisibility(v.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());

                progressDialog.dismiss();

                Toast.makeText(getActivity(), "Failed, " + error, Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference myCartRef;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myPhone = user.getPhoneNumber(); //Current logged in user phone number
        myCartRef = db.getReference(myPhone + "/mycart");

        //Check if theres anything in my cart
        myCartRef.addValueEventListener(new ValueEventListener() {
            //If there is, loop through the items found and add to myBasket list
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                myBasket = new ArrayList<>();
                int temp = 0;

                for (DataSnapshot mycart : dataSnapshot.getChildren()) {
                    MyCartDetails myCartDetails = mycart.getValue(MyCartDetails.class);
                    String prices = myCartDetails.getPrice();
                    temp = Integer.parseInt(prices) + temp;
                    myBasket.add(myCartDetails);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent slideactivity = new Intent(getContext(), MyCart.class);
                Bundle bndlanimation =
                    ActivityOptions.makeCustomAnimation(getContext(), R.anim.animation,R.anim.animation2).toBundle();
                startActivity(slideactivity, bndlanimation);

            }
        });

        return  v;
    }


    private void saveData(String key, String value) {
        String data = key + ":" + value;
        try {
            // Open Stream to write file.
            FileOutputStream out = getContext().openFileOutput(simpleFileName, MODE_PRIVATE);

            out.write(data.getBytes());
            out.close();
            Toast.makeText(getContext(),"File saved!",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(),"Error:"+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void readData() {
        try {
            // Open stream to read file.
            FileInputStream in = getContext().openFileInput(simpleFileName);

            BufferedReader br= new BufferedReader(new InputStreamReader(in));

            StringBuilder sb= new StringBuilder();
            String s= null;
            while((s= br.readLine())!= null)  {
                sb.append(s).append("\n");
            }
            Toast.makeText(getContext(), "Saved Data: " + sb.toString(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(getContext(),"Error: "+ e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }
}