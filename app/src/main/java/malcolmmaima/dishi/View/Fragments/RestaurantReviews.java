package malcolmmaima.dishi.View.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import malcolmmaima.dishi.R;

public class RestaurantReviews extends android.support.v4.app.Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restaurant_reviews, container, false);
    }
}