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
import android.widget.Button;
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
import malcolmmaima.dishi.Model.ReceivedOrders;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.AddMenu;
import malcolmmaima.dishi.View.MyCart;

public class ReceivedOrdersAdapter extends RecyclerView.Adapter<ReceivedOrdersAdapter.MyHolder>{

    Context context;
    List<ReceivedOrders> listdata;

    public ReceivedOrdersAdapter(Context context, List<ReceivedOrders> listdata) {
        this.listdata = listdata;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.confirm_order_card,parent,false);

        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }


    public void onBindViewHolder(final MyHolder holder, final int position) {
        final ReceivedOrders receivedOrders = listdata.get(position);

        final DatabaseReference mylocationRef, myOrdersRef, customerOrder, customerLocationRef, orderStatus;
        FirebaseDatabase db;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        // Assign FirebaseStorage instance to storageReference.

        db = FirebaseDatabase.getInstance();
        mylocationRef = db.getReference(myPhone + "/location"); //loggedin user location reference
        myOrdersRef = db.getReference(myPhone + "/orders"); //food item provider location reference
        customerLocationRef = db.getReference(receivedOrders.getCustomerNumber() + "/location");
        customerOrder = db.getReference(receivedOrders.getCustomerNumber() + "/pending");
        orderStatus = db.getReference(receivedOrders.getCustomerNumber() + "/pending/" + receivedOrders.key);


        final Double[] dist = new Double[listdata.size()];

        //Lets create a Double[] array containing my lat/lon
        final Double[] mylat = new Double[listdata.size()];
        final Double[] mylon = new Double[listdata.size()];

        //Lets create a Double[] array containing the customer lat/lon
        final Double[] custlat = new Double[listdata.size()];
        final Double[] custlon = new Double[listdata.size()];


        //My latitude longitude coordinates
        mylocationRef.child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mylat[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(custlat[position], custlon[position], mylat[position], mylon[position], "K");
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
                    dist[position] = distance(custlat[position], custlon[position], mylat[position], mylon[position], "K");
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
        customerLocationRef.child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                custlat[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(custlat[position], custlon[position], mylat[position], mylon[position], "K");
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                    holder.distAway.setText(Math.floor(dist[position]) + " km away");
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        customerLocationRef.child("longitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                custlon[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(custlat[position], custlon[position], mylat[position], mylon[position], "K");
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

        holder.foodPrice.setText("Ksh "+receivedOrders.getPrice());
        holder.foodName.setText(receivedOrders.getName());
        holder.foodDescription.setText(receivedOrders.getDescription());
        holder.providerName.setText("Customer: " + receivedOrders.getCustomerNumber());


        //Loading image from Glide library.
        Glide.with(context).load(receivedOrders.getImageURL()).into(holder.foodPic);
        //Log.d("glide", "onBindViewHolder: imageUrl: " + receivedOrders.getImageURL());

        final String[] status = new String[listdata.size()];

        //Check order status of each item
        orderStatus.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                status[position] = dataSnapshot.getValue(String.class); //Order status
                //Toast.makeText(context, "Order status: " + status, Toast.LENGTH_SHORT).show();
                if(status[position].equals("confirmed")){
                    holder.acceptBtn.setText("Cancel");
                }

                if(status[position].equals("cancelled")){
                    holder.acceptBtn.setText("Confirm");
                }

                if(status[position].equals("pending")){
                    holder.acceptBtn.setText("Confirm");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error: " + databaseError.toString() + ". Try again!", Toast.LENGTH_LONG).show();
            }
        });

        //accept or cancel order
        holder.acceptBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public  void onClick(final View view) {

                if (status[position].equals("confirmed")) {
                    //Do something if order cancelled
                    final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(view.getContext())
                            //set message, title, and icon
                            .setTitle("Cancel Order")
                            .setMessage("Cancel order of " + receivedOrders.getName() + "?")
                            //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                            //set three option buttons
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    receivedOrders.status = "cancelled";
                                    myOrdersRef.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Write was successful!
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Write failed
                                                    Toast.makeText(context, "Failed: " + e.toString() + ". Try again!", Toast.LENGTH_LONG).show();
                                                }
                                            });

                                    customerOrder.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Write was successful!
                                            Toast.makeText(context, "Cancellation sent to: " + receivedOrders.getCustomerNumber(), Toast.LENGTH_LONG).show();

                                            //Then delete the menu item from my orders (implement below)
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
                                    //Toast.makeText(context, "reject order", Toast.LENGTH_SHORT).show();

                                }
                            })//setNegativeButton

                            .create();
                    myQuittingDialogBox.show();
                } else {

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(view.getContext())
                        //set message, title, and icon
                        .setTitle("Confirm Order")
                        .setMessage("Accept order of " + receivedOrders.getName() + "?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                receivedOrders.status = "confirmed";
                                myOrdersRef.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Write was successful!
                                        //Toast.makeText(context, "Confirmation sent to: " + receivedOrders.getCustomerNumber(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Write failed
                                                Toast.makeText(context, "Failed: " + e.toString() + ". Try again!", Toast.LENGTH_LONG).show();
                                            }
                                        });

                                customerOrder.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Write was successful!
                                        Toast.makeText(context, "Confirmation sent to: " + receivedOrders.getCustomerNumber(), Toast.LENGTH_LONG).show();
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
                                Toast.makeText(context, "reject order", Toast.LENGTH_SHORT).show();

                            }
                        })//setNegativeButton

                        .create();
                myQuittingDialogBox.show();
            }
            }
        });
    }

    //Needs a bit more tuning to factor in elevation of the two points
    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") { //Kilometers
            dist = dist * 1.609344;
        }

        else if (unit == "N") { //Nautical miles
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
        Button acceptBtn;

        public MyHolder(View itemView) {
            super(itemView);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodName = itemView.findViewById(R.id.foodName);
            foodDescription = itemView.findViewById(R.id.foodDescription);
            foodPic = itemView.findViewById(R.id.foodPic);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            providerName = itemView.findViewById(R.id.providerName);
            distAway = itemView.findViewById(R.id.distanceAway);

        }
    }


}