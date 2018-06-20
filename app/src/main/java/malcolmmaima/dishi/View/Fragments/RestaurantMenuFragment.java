package malcolmmaima.dishi.View.Fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.RestaurantMenuAdapter;

public class RestaurantMenuFragment extends Fragment {

    ProgressDialog progressDialog ;
    List<ProductDetails> list;
    RecyclerView recyclerview;
    String myPhone;
    TextView emptyTag;

    DatabaseReference dbRef, menusRef;
    FirebaseDatabase db;

    FirebaseUser user;


    public static RestaurantMenuFragment newInstance() {
        RestaurantMenuFragment fragment = new RestaurantMenuFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_restaurant_menu, container, false);
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
        menusRef = db.getReference(myPhone + "/mymenu");
        recyclerview = v.findViewById(R.id.rview);
        emptyTag = v.findViewById(R.id.empty_tag);

        //Loop through the mymenu child node and get menu items, assign values to our ProductDetails model
        menusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list = new ArrayList<>();
                int listSize = list.size(); //Bug fix, kept on refreshing menu on data change due to realtime location data.
                //Will use this to determine if the list of menu items has changed, only refresh then

                // StringBuffer stringbuffer = new StringBuffer();
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){
                    ProductDetails productDetails = dataSnapshot1.getValue(ProductDetails.class); //Assign values to model
                    productDetails.key = dataSnapshot1.getKey(); //Get item keys, useful when performing delete operations
                    list.add(productDetails);
                    //progressDialog.dismiss();
                }

                if(!list.isEmpty() && list.size() > listSize){
                    RestaurantMenuAdapter recycler = new RestaurantMenuAdapter(getContext(),list);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                    recyclerview.setLayoutManager(layoutmanager);
                    recyclerview.setItemAnimator( new DefaultItemAnimator());
                    recyclerview.setAdapter(recycler);
                    emptyTag.setVisibility(v.INVISIBLE);
                }

                else {
                    RestaurantMenuAdapter recycler = new RestaurantMenuAdapter(getContext(),list);
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

                progressDialog.dismiss();

                Toast.makeText(getActivity(), "Failed, " + error, Toast.LENGTH_SHORT).show();
            }
        });

        return  v;
    }
}