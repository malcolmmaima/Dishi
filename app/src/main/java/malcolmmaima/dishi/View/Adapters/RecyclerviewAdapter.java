package malcolmmaima.dishi.View.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import malcolmmaima.dishi.Model.Listdata;
import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.MyHolder>{

    Context context;
    List<ProductDetails> listdata;

    public RecyclerviewAdapter(Context context, List<ProductDetails> listdata) {
        this.listdata = listdata;

        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.myview,parent,false);

        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }


    public void onBindViewHolder(MyHolder holder, int position) {
        ProductDetails productDetails = listdata.get(position);
        holder.foodPrice.setText(productDetails.getPrice());
        holder.foodName.setText(productDetails.getName());
        holder.foodDescription.setText(productDetails.getDescription());

        //Loading image from Glide library.
        Glide.with(context).load(productDetails.getImageURL()).into(holder.foodPic);
        Log.d("glide", "onBindViewHolder: imageUrl: " + productDetails.getImageURL());
    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        TextView foodPrice , foodDescription, foodName;
        ImageView foodPic;

        public MyHolder(View itemView) {
            super(itemView);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodName = itemView.findViewById(R.id.foodName);
            foodDescription = itemView.findViewById(R.id.foodDescription);
            foodPic = itemView.findViewById(R.id.foodPic);

        }
    }


}