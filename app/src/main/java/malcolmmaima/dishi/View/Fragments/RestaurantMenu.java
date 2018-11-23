package malcolmmaima.dishi.View.Fragments;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.CustomerOrderAdapter;
import malcolmmaima.dishi.View.MainActivity;
import malcolmmaima.dishi.View.MyAccountRestaurant;
import malcolmmaima.dishi.View.MyCart;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class RestaurantMenu extends android.support.v4.app.Fragment {

    DatabaseReference menuItemsRef, restaurantRootRef, myCart;
    List<OrderDetails> list;
    RecyclerView recyclerview;
    TextView emptyTag, cartSize;
    String restaurantName, myPhone;
    android.support.design.widget.FloatingActionButton myCartBtn;

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
        myCartBtn = v.findViewById(R.id.myCart);
        cartSize = v.findViewById(R.id.cartSize);
        restaurantName = "";

        myCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Slide to new activity
                Intent slideactivity = new Intent(getContext(), MyCart.class);
                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(getContext(), R.anim.animation,R.anim.animation2).toBundle();
                startActivity(slideactivity, bndlanimation);
            }
        });

        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            myPhone = user.getPhoneNumber(); //Current logged in user phone number

            final String getPhone = getArguments().getString("phone");//get phone value from parent activity
            menuItemsRef = FirebaseDatabase.getInstance().getReference(getPhone + "/mymenu");
            myCart = FirebaseDatabase.getInstance().getReference(myPhone + "/mycart");
            restaurantRootRef = FirebaseDatabase.getInstance().getReference(getPhone);

            myCart.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        int cartItemsSize = (int) dataSnapshot.getChildrenCount();
                        cartSize.setText(""+cartItemsSize);

                        } catch (Exception e){

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            restaurantRootRef.child("name").addValueEventListener(new ValueEventListener() {
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
                            Collections.reverse(list);
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
