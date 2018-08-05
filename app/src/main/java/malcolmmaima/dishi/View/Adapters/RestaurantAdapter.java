package malcolmmaima.dishi.View.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import java.util.List;
import malcolmmaima.dishi.Model.RestaurantDetails;
import malcolmmaima.dishi.R;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.MyHolder> {

    Context context;
    List<RestaurantDetails> listdata;

    public RestaurantAdapter(Context context, List<RestaurantDetails> listdata) {
        this.listdata = listdata;
        this.context = context;
    }

    @Override
    public RestaurantAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_restaurant,parent,false);

        RestaurantAdapter.MyHolder myHolder = new RestaurantAdapter.MyHolder(view);
        return myHolder;
    }


    public void onBindViewHolder(final RestaurantAdapter.MyHolder holder, final int position) {
        final RestaurantDetails restaurantDetails = listdata.get(position);
        holder.restaurantName.setText(restaurantDetails.getName());

        holder.likeImageView.setTag(R.drawable.ic_like);

        try {
            //Loading image from Glide library.
            Glide.with(context).load(restaurantDetails.getProfilepic()).into(holder.profilePic);
            Log.d("glide", "onBindViewHolder: imageUrl: " + restaurantDetails.getProfilepic());
        } catch (Exception e){

        }

        holder.likeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int id = (int)holder.likeImageView.getTag();
                if( id == R.drawable.ic_like){

                    holder.likeImageView.setTag(R.drawable.ic_liked);
                    holder.likeImageView.setImageResource(R.drawable.ic_liked);

                    Toast.makeText(context,restaurantDetails.getName()+" added to favourites",Toast.LENGTH_SHORT).show();

                } else{

                    holder.likeImageView.setTag(R.drawable.ic_like);
                    holder.likeImageView.setImageResource(R.drawable.ic_like);
                    Toast.makeText(context,restaurantDetails.getName()+" removed from favourites",Toast.LENGTH_SHORT).show();
                }

            }
        });



        holder.shareImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Share!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        TextView restaurantName, distAway;
        ImageView profilePic, likeImageView, shareImageView;
        Button call;

        public MyHolder(View itemView) {
            super(itemView);

            likeImageView = itemView.findViewById(R.id.likeImageView);
            shareImageView = itemView.findViewById(R.id.shareImageView);
            profilePic = itemView.findViewById(R.id.coverImageView);
            restaurantName = itemView.findViewById(R.id.titleTextView);
            //distAway = itemView.findViewById(R.id.distanceAway);

        }
    }
}
