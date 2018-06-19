package malcolmmaima.dishi.View.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;

public class CustomerOrderAdapter extends RecyclerView.Adapter<CustomerOrderAdapter.MyHolder>{

    Context context;
    List<OrderDetails> listdata;

    public CustomerOrderAdapter(Context context, List<OrderDetails> listdata) {
        this.listdata = listdata;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_order_card,parent,false);

        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }


    public void onBindViewHolder(final MyHolder holder, int position) {
        final OrderDetails orderDetails = listdata.get(position);

        final DatabaseReference menusRef;
        FirebaseDatabase db;
        StorageReference storageReference;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myPhone = user.getPhoneNumber(); //Current logged in user phone number
        // Assign FirebaseStorage instance to storageReference.

        storageReference = FirebaseStorage.getInstance().getReference();

        db = FirebaseDatabase.getInstance();
        menusRef = db.getReference(myPhone + "/mymenu"); //Under the user's node, place their menu items

        holder.foodPrice.setText("Ksh "+orderDetails.getPrice());
        holder.foodName.setText(orderDetails.getName());
        holder.foodDescription.setText(orderDetails.getDescription());

        //Loading image from Glide library.
        Glide.with(context).load(orderDetails.getImageURL()).into(holder.foodPic);
        Log.d("glide", "onBindViewHolder: imageUrl: " + orderDetails.getImageURL());

        holder.orderBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(final View view){
                Toast.makeText(context, "Order " + orderDetails.getName(), Toast.LENGTH_LONG).show();
                //Toast.makeText(context, "(key): "+productDetails.key, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        TextView foodPrice , foodDescription, foodName;
        ImageView foodPic;
        ImageButton orderBtn;

        public MyHolder(View itemView) {
            super(itemView);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodName = itemView.findViewById(R.id.foodName);
            foodDescription = itemView.findViewById(R.id.foodDescription);
            foodPic = itemView.findViewById(R.id.foodPic);
            orderBtn = itemView.findViewById(R.id.orderBtn);

        }
    }


}