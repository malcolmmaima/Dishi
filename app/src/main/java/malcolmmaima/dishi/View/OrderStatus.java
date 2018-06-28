package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
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

public class OrderStatus extends AppCompatActivity {

    List<MyCartDetails> myBasket;
    RecyclerView recyclerview;
    String myPhone, paymentType;
    TextView emptyTag, totalItems, totalFee;
    Button trackBtn;
    Spinner payMethod;

    DatabaseReference myCartRef, myPendingOrders, myRef;
    FirebaseDatabase db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Delivery Status");

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
        recyclerview = findViewById(R.id.rview);
        emptyTag = findViewById(R.id.empty_tag);
        totalItems = findViewById(R.id.totalItems);
        totalFee = findViewById(R.id.totalFee);
        trackBtn = findViewById(R.id.trackOrder);


        //Check if theres anything in pendin node
        myPendingOrders.addValueEventListener(new ValueEventListener() {
            //If there is, loop through the items found and add to myBasket list
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    myBasket = new ArrayList<>();
                    int temp = 0;

                    for (DataSnapshot mycart : dataSnapshot.getChildren()) {
                        MyCartDetails myCartDetails = mycart.getValue(MyCartDetails.class);
                        myCartDetails.key = mycart.getKey();
                        //myCartDetails.providerNumber = mycart.;
                        String prices = myCartDetails.getPrice();
                        temp = Integer.parseInt(prices) + temp;
                        myBasket.add(myCartDetails);
                        Toast.makeText(OrderStatus.this, myCartDetails.getName() + " status: " + myCartDetails.status, Toast.LENGTH_SHORT).show();
                    }

                    //Toast.makeText(getContext(), "TOTAL: " + temp, Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getContext(), "Items: " + myBasket.size(), Toast.LENGTH_SHORT).show();
                    if(myBasket.size() == 0) {
                        trackBtn.setEnabled(false);
                    /*
                    Toast toast = Toast.makeText(getContext(),"Please add items to your cart!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    */
                    }
                    else {
                        trackBtn.setEnabled(true);
                    }
                    totalFee.setText("Ksh: " + temp);
                    totalItems.setText("Items: " + myBasket.size());

                    if (!myBasket.isEmpty()) {
                        MyCartAdapter recycler = new MyCartAdapter(OrderStatus.this, myBasket);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(OrderStatus.this);
                        recyclerview.setLayoutManager(layoutmanager);
                        recyclerview.setItemAnimator(new DefaultItemAnimator());
                        recyclerview.setAdapter(recycler);
                        emptyTag.setVisibility(INVISIBLE);
                    } else {
                        MyCartAdapter recycler = new MyCartAdapter(OrderStatus.this, myBasket);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(OrderStatus.this);
                        recyclerview.setLayoutManager(layoutmanager);
                        recyclerview.setItemAnimator(new DefaultItemAnimator());
                        recyclerview.setAdapter(recycler);
                        emptyTag.setVisibility(VISIBLE);

                    }

                } catch (Exception e){
                    emptyTag.setText("Failed");
                    emptyTag.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        trackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(OrderStatus.this, "Track order(Real-time)", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(OrderStatus.this, "Clicked!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

}

