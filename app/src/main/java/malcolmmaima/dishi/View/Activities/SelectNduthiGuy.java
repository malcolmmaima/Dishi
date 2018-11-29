package malcolmmaima.dishi.View.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

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

import malcolmmaima.dishi.Model.NduthiNearMe;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.NduthiAdapter;

public class SelectNduthiGuy extends AppCompatActivity {

    RecyclerView recyclerview;
    DatabaseReference nduthisNearmeRef, myRef, dbRef;
    List<NduthiNearMe> nduthis;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_nduthi_guy);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getInstance().getCurrentUser() == null){

            //User is not signed in, send them back to verification page
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(SelectNduthiGuy.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));//Load Main Activity and clear activity stack
        }

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Nearby Nduthis");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber();
        myRef = FirebaseDatabase.getInstance().getReference(myPhone);
        dbRef = FirebaseDatabase.getInstance().getReference();

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                nduthisNearmeRef = FirebaseDatabase.getInstance().getReference(myPhone + "/nearby_nduthis");

                nduthisNearmeRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });

                finish();
                /*
                Intent cartActivity = new Intent(SelectNduthiGuy.this, MyCart.class);
                cartActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//Load MyCart Activity and clear activity stack
                startActivity(cartActivity); */
            }
        });
        //topToolBar.setLogo(R.drawable.logo);
        //topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));

        recyclerview = findViewById(R.id.rview);

        //Check to see if nduthi has confirmed ride request
        myRef.child("confirmed_order").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot confirmed : dataSnapshot.getChildren()){
                        try {
                            String phone = confirmed.getKey();
                            phone = phone.replace("confirmed_", "");
                            final String[] name = new String[1];

                            //Get the name of the nduthi guy who has confirmed ride request
                            dbRef.child(phone).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    try {
                                        name[0] = dataSnapshot.getValue(String.class);
                                        final AlertDialog rideRequest = new AlertDialog.Builder(SelectNduthiGuy.this)
                                                //set message, title, and icon
                                                .setIcon(R.drawable.nduthi_guy)
                                                .setCancelable(false)
                                                .setMessage(name[0] +" has confirmed ride! Go check order status.")
                                                //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                                                //set three option buttons
                                                .setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                                finish();
                                                            }
                                                        }).create();
                                        rideRequest.show();

                                    } catch (Exception e){

                                        Toast toast = Toast.makeText(SelectNduthiGuy.this,name[0] +" has confirmed ride! Go check order status.", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();

                                        finish();

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

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

        nduthisNearmeRef = FirebaseDatabase.getInstance().getReference(myPhone + "/nearby_nduthis");

        nduthisNearmeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nduthis = new ArrayList<>();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    NduthiNearMe nduthiNearMe = dataSnapshot1.getValue(NduthiNearMe.class); //Assign values to model
                    nduthiNearMe.phone = dataSnapshot1.getKey();
                    nduthis.add(nduthiNearMe);
                    //progressDialog.dismiss();
                }

                //Refresh list
                if (!nduthis.isEmpty()) {
                    NduthiAdapter recycler = new NduthiAdapter(SelectNduthiGuy.this, nduthis);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(SelectNduthiGuy.this);
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator(new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);
                } else {
                    NduthiAdapter recycler = new NduthiAdapter(SelectNduthiGuy.this, nduthis);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(SelectNduthiGuy.this);
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator(new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());

                Toast.makeText(SelectNduthiGuy.this, "Failed, " + error, Toast.LENGTH_SHORT).show();
            }


        });
    }

}
