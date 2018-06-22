package malcolmmaima.dishi.View.Adapters;

import android.app.AlertDialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.AddMenu;
import malcolmmaima.dishi.View.MyCart;

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


    public void onBindViewHolder(final MyHolder holder, final int position) {
        final OrderDetails orderDetails = listdata.get(position);

        final DatabaseReference mylocationRef, providerRef, myCartRef;
        FirebaseDatabase db;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        // Assign FirebaseStorage instance to storageReference.

        db = FirebaseDatabase.getInstance();
        mylocationRef = db.getReference(myPhone + "/location"); //loggedin user location reference
        providerRef = db.getReference(orderDetails.providerNumber + "/location"); //food item provider location reference
        myCartRef = db.getReference(myPhone + "/mycart");

        final Double[] dist = new Double[listdata.size()];

        //Lets create a Double[] array containing my lat/lon
        final Double[] mylat = new Double[listdata.size()];
        final Double[] mylon = new Double[listdata.size()];

        //Lets create a Double[] array containing the provider lat/lon
        final Double[] provlat = new Double[listdata.size()];
        final Double[] provlon = new Double[listdata.size()];


        //My latitude longitude coordinates
        mylocationRef.child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mylat[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(provlat[position], provlon[position], mylat[position], mylon[position], "K");
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                    holder.distAway.setText(Math.floor(dist[position]) + " km away");
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mylocationRef.child("longitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mylon[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(provlat[position], provlon[position], mylat[position], mylon[position], "K");
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                    holder.distAway.setText(Math.floor(dist[position]) + " km away");
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Item provider latitude longitude coordinates
        providerRef.child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                provlat[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(provlat[position], provlon[position], mylat[position], mylon[position], "K");
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                    holder.distAway.setText(Math.floor(dist[position]) + " km away");
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        providerRef.child("longitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                provlon[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(provlat[position], provlon[position], mylat[position], mylon[position], "K");
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                    holder.distAway.setText(Math.floor(dist[position]) + " km away");
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Toast.makeText(context, "provider" + (" x:" + provlat[0] +" y:"+ provlon[0]) , Toast.LENGTH_SHORT).show();

        holder.foodPrice.setText("Ksh "+orderDetails.getPrice());
        holder.foodName.setText(orderDetails.getName());
        holder.foodDescription.setText(orderDetails.getDescription());
        holder.providerName.setText("Provider: " + orderDetails.providerName);


        //Loading image from Glide library.
        Glide.with(context).load(orderDetails.getImageURL()).into(holder.foodPic);
        Log.d("glide", "onBindViewHolder: imageUrl: " + orderDetails.getImageURL());

        holder.orderBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public  void onClick(final View view){
                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(view.getContext())
                        //set message, title, and icon
                        .setTitle("Add to cart")
                        .setMessage("Add "+ orderDetails.getName() + " to cart?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String key = myCartRef.push().getKey(); //The child node in mycart for storing menu items
                                final MyCartDetails myCart = new MyCartDetails();

                                myCart.setName(orderDetails.getName());
                                myCart.setPrice(orderDetails.getPrice());
                                myCart.setDescription(orderDetails.getDescription());
                                myCart.setImageURL(orderDetails.getImageURL());
                                myCart.setProvider(orderDetails.providerName);
                                myCart.setProviderNumber((orderDetails.providerNumber));

                                myCartRef.child(key).setValue(myCart).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Write was successful!

                                        Snackbar snackbar = Snackbar
                                                .make(view, "Added!", Snackbar.LENGTH_LONG);

                                        snackbar.show();

                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Write failed
                                                Toast.makeText(context, "Failed: " + e.toString() + ". Try again!", Toast.LENGTH_LONG).show();
                                            }
                                        });

                            }
                        })//setPositiveButton


                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Do not delete
                                //Toast.makeText(context, "No", Toast.LENGTH_SHORT).show();

                            }
                        })//setNegativeButton

                        .create();
                myQuittingDialogBox.show();
                }
        });
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts decimal degrees to radians						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts radians to decimal degrees						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        TextView foodPrice , foodDescription, foodName, providerName, distAway;
        ImageView foodPic;
        ImageButton orderBtn;

        public MyHolder(View itemView) {
            super(itemView);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodName = itemView.findViewById(R.id.foodName);
            foodDescription = itemView.findViewById(R.id.foodDescription);
            foodPic = itemView.findViewById(R.id.foodPic);
            orderBtn = itemView.findViewById(R.id.orderBtn);
            providerName = itemView.findViewById(R.id.providerName);
            distAway = itemView.findViewById(R.id.distanceAway);

        }
    }


}