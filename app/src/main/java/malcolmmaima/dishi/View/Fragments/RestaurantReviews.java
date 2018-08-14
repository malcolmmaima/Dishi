package malcolmmaima.dishi.View.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import malcolmmaima.dishi.Model.RestaurantReview;
import malcolmmaima.dishi.R;

public class RestaurantReviews extends android.support.v4.app.Fragment {
    EditText review;
    Button postReview;
    DatabaseReference reviewsRef;
    String myPhone;

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
        } catch (Exception e){

        }

        return v;
    }
}