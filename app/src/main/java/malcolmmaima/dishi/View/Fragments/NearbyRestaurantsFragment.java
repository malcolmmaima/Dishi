package malcolmmaima.dishi.View.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import malcolmmaima.dishi.R;

public class NearbyRestaurantsFragment extends Fragment {
    public static NearbyRestaurantsFragment newInstance() {
        NearbyRestaurantsFragment fragment = new NearbyRestaurantsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nearby_restaurants, container, false);
    }
}
