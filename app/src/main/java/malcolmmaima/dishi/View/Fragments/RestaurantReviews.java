package malcolmmaima.dishi.View.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.Model.RestaurantReview;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.CustomerOrderAdapter;
import malcolmmaima.dishi.View.Adapters.RestaurantReviewAdapter;

public class RestaurantReviews extends android.support.v4.app.Fragment {
    EditText review;
    TextView emptyTag;
    Button postReview;
    DatabaseReference reviewsRef;
    String myPhone;
    RecyclerView recyclerview;
    List<RestaurantReview> list;

    public RestaurantReviews() {
        // Required empty public constructor
    }

    public static RestaurantReviews newInstance() {
        RestaurantReviews fragment = new RestaurantReviews();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.fragment_restaurant_reviews, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number
        review = v.findViewById(R.id.inputReview);
        postReview = v.findViewById(R.id.postReview);

        recyclerview = v.findViewById(R.id.rview);
        emptyTag = v.findViewById(R.id.empty_tag);

        try {
            final String getPhone = getArguments().getString("phone");//restaurant phone from parent activity
            reviewsRef = FirebaseDatabase.getInstance().getReference(getPhone + "/customer_reviews");

            postReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String key = reviewsRef.push().getKey();
                    RestaurantReview restaurantReview = new RestaurantReview();
                    restaurantReview.setPhone(myPhone);
                    restaurantReview.setReview(review.getText().toString());

                    if(review.getText().toString().equals("")){
                        Toast.makeText(getContext(), "You must enter something!", Toast.LENGTH_SHORT).show();
                    }

                    else {
                        reviewsRef.child(key).setValue(restaurantReview).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Review posted!", Toast.LENGTH_SHORT).show();
                                review.setText("");
                            }
                        });
                    }

                }
            });

            //Fetch the reviews from customer reviews node of the restaurant
            reviewsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    list = new ArrayList<>();
                    for(DataSnapshot reviews : dataSnapshot.getChildren()){
                        RestaurantReview restaurantReview = reviews.getValue(RestaurantReview.class);
                        restaurantReview.setRestaurantphone(getPhone);
                        list.add(restaurantReview);
                        //Toast.makeText(getContext(), restaurantReview.getPhone() + ": " + restaurantReview.getReview(), Toast.LENGTH_SHORT).show();
                    }

                    try {
                        if (!list.isEmpty()) {
                            recyclerview.setVisibility(View.VISIBLE);
                            RestaurantReviewAdapter recycler = new RestaurantReviewAdapter(getContext(), list);
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
                            emptyTag.setText("NO REVIEWS");
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

        return v;
    }
}