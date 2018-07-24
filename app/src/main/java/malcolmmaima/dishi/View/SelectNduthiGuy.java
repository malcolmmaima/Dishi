package malcolmmaima.dishi.View;

import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import malcolmmaima.dishi.Model.ReceivedOrders;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.NduthiAdapter;
import malcolmmaima.dishi.View.Adapters.ReceivedOrdersAdapter;
import malcolmmaima.dishi.View.Fragments.ConfirmedDeliveriesFragment;

public class SelectNduthiGuy extends AppCompatActivity {

    RecyclerView recyclerview;
    DatabaseReference nduthisNearmeRef;
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

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String myPhone = user.getPhoneNumber();
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myPhone = user.getPhoneNumber();

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
