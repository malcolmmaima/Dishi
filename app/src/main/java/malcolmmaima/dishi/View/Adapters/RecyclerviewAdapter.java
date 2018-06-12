package malcolmmaima.dishi.View.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;

public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.MyHolder>{

    Context context;
    List<ProductDetails> listdata;
    ProgressDialog progressDialog;

    public RecyclerviewAdapter(Context context, List<ProductDetails> listdata) {
        this.listdata = listdata;

        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_card,parent,false);

        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }


    public void onBindViewHolder(MyHolder holder, int position) {
        final ProductDetails productDetails = listdata.get(position);
        holder.foodPrice.setText("Ksh "+productDetails.getPrice());
        holder.foodName.setText(productDetails.getName());
        holder.foodDescription.setText(productDetails.getDescription());

        //Loading image from Glide library.
        Glide.with(context).load(productDetails.getImageURL()).into(holder.foodPic);
        Log.d("glide", "onBindViewHolder: imageUrl: " + productDetails.getImageURL());

        holder.editBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(final View view){
                Toast.makeText(context, "Edit " + productDetails.getName(), Toast.LENGTH_LONG).show();
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(final View view){
                //Order Button click event
                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(view.getContext())
                        //set message, title, and icon
                        .setTitle("Delete item")
                        .setMessage("Are you sure you want to delete "+ productDetails.getName() + "?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(context, "Yes", Toast.LENGTH_SHORT).show();
                                /*
                                progressDialog = new ProgressDialog(context);
                                progressDialog.setCancelable(false);
                                progressDialog.setTitle("Processing...");
                                progressDialog.setMessage("Please wait...");
                                progressDialog.show();
                                */
                            }
                        })//setPositiveButton


                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Do not delete
                                Toast.makeText(context, "No", Toast.LENGTH_SHORT).show();

                            }
                        })//setNegativeButton

                        .create();
                myQuittingDialogBox.show();
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
        Button editBtn, deleteBtn;

        public MyHolder(View itemView) {
            super(itemView);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodName = itemView.findViewById(R.id.foodName);
            foodDescription = itemView.findViewById(R.id.foodDescription);
            foodPic = itemView.findViewById(R.id.foodPic);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);

        }
    }


}