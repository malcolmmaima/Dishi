package malcolmmaima.dishi.View.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import malcolmmaima.dishi.R;

public class NduthiDeliveriesFragment extends Fragment {
    public static NduthiDeliveriesFragment newInstance() {
        NduthiDeliveriesFragment fragment = new NduthiDeliveriesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nduthi_deliveries, container, false);
    }
}

