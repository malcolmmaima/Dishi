package malcolmmaima.dishi.View.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import malcolmmaima.dishi.Model.RestaurantDetails;
import malcolmmaima.dishi.Model.RestaurantReview;
import malcolmmaima.dishi.R;

public class RestaurantReviewAdapter extends RecyclerView.Adapter<RestaurantReviewAdapter.MyHolder> {

    Context context;
    List<RestaurantReview> listdata;
    DatabaseReference customerNode;
    String profilePic, profileName;

    public RestaurantReviewAdapter(Context context, List<RestaurantReview> listdata) {
        this.listdata = listdata;
        this.context = context;
    }

    @NonNull
    @Override
    public RestaurantReviewAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_restaurant_review,parent,false);

        RestaurantReviewAdapter.MyHolder myHolder = new RestaurantReviewAdapter.MyHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantReviewAdapter.MyHolder holder, int position) {

        final RestaurantReview restaurantReview = listdata.get(position);
        customerNode = FirebaseDatabase.getInstance().getReference(restaurantReview.getPhone());
        holder.userReview.setText(restaurantReview.getReview());

        try {
            //Loading image from Glide library.
            customerNode.child("profilepic").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    profilePic = dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //Fetch name
            customerNode.child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    profileName = dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            Glide.with(context).load(profilePic).into(holder.profilePic);
            holder.customerName.setText(profileName);

        } catch (Exception e){

        }

    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        TextView customerName, distAway, userReview;
        ImageView profilePic;

        public MyHolder(View itemView) {
            super(itemView);

            customerName = itemView.findViewById(R.id.profileName);
            distAway = itemView.findViewById(R.id.distanceAway);
            userReview = itemView.findViewById(R.id.userReview);
            profilePic = itemView.findViewById(R.id.profilePic);


        }
    }
}
