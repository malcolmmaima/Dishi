package malcolmmaima.dishi.View.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import malcolmmaima.dishi.R;

public class ReceivedOrdersFragment extends Fragment {
    public static ReceivedOrdersFragment newInstance() {
        ReceivedOrdersFragment fragment = new ReceivedOrdersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_received_orders, container, false);
    }
}
