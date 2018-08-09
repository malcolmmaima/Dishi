package malcolmmaima.dishi.View.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.CustomerOrderAdapter;

public class RestaurantMenu extends android.support.v4.app.Fragment {

    DatabaseReference menuItemsRef, restaurantRootRef;
    List<OrderDetails> list;
    RecyclerView recyclerview;
    TextView emptyTag;
    String restaurantName;

    public RestaurantMenu() {
        // Required empty public constructor
    }

    public static RestaurantMenu newInstance() {
        RestaurantMenu fragment = new RestaurantMenu();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.fragment_view_restaurants_menu, container, false);
        recyclerview = v.findViewById(R.id.rview);
        emptyTag = v.findViewById(R.id.empty_tag);
        restaurantName = "";

        try {

            final String getPhone = getArguments().getString("phone");//get phone value from parent activity
            menuItemsRef = FirebaseDatabase.getInstance().getReference(getPhone + "/mymenu");
            restaurantRootRef = FirebaseDatabase.getInstance().getReference(getPhone);

            restaurantRootRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        restaurantName = dataSnapshot.getValue(String.class);
                    } catch (Exception e){}
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            menuItemsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    list = new ArrayList<>();
                    for(DataSnapshot items : dataSnapshot.getChildren()){
                        //Toast.makeText(getContext(), "menu-node: " + items.getKey(), Toast.LENGTH_SHORT).show();
                        final OrderDetails mymenu = items.getValue(OrderDetails.class);
                        mymenu.providerNumber = getPhone;
                        mymenu.providerName = restaurantName;
                        list.add(mymenu);
                    }

                    try {
                        if (!list.isEmpty()) {
                            recyclerview.setVisibility(View.VISIBLE);
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
                            recyclerview.setVisibility(v.INVISIBLE);
                            emptyTag.setVisibility(v.VISIBLE);
                            emptyTag.setText("EMPTY");
                        }
                    }

                    catch (Exception e){
                        recyclerview.setVisibility(v.INVISIBLE);
                        emptyTag.setVisibility(v.VISIBLE);
                        emptyTag.setText("Error");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (Exception e){

        }

        return  v;
    }
}
