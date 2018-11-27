package malcolmmaima.dishi.View.Adapters;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.ReceivedOrders;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.AddMenu;
import malcolmmaima.dishi.View.Map.GeoFireActivity;
import malcolmmaima.dishi.View.MyCart;
import malcolmmaima.dishi.View.OrderStatus;
import malcolmmaima.dishi.View.ViewProfile;

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

        final ProgressDialog progressDialog = new ProgressDialog(context);

        final ReceivedOrders receivedOrders = listdata.get(position);

        final DatabaseReference mylocationRef, myOrdersRef, customerOrder, customerLocationRef, orderStatus, deliveriesRef, customerName;
        FirebaseDatabase db;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        // Assign FirebaseStorage instance to storageReference.

        db = FirebaseDatabase.getInstance();
        mylocationRef = db.getReference(myPhone + "/location"); //loggedin user location reference
        myOrdersRef = db.getReference(myPhone + "/orders"); //food item provider location reference
        customerLocationRef = db.getReference(receivedOrders.getCustomerNumber() + "/location");
        customerOrder = db.getReference(receivedOrders.getCustomerNumber() + "/pending");
        orderStatus = db.getReference(receivedOrders.getCustomerNumber() + "/pending/" + receivedOrders.key);
        deliveriesRef = db.getReference(myPhone + "/deliveries");
        customerName = db.getReference(receivedOrders.getCustomerNumber());


        final Double[] dist = new Double[listdata.size()];

        //Lets create a Double[] array containing my lat/lon
        final Double[] mylat = new Double[listdata.size()];
        final Double[] mylon = new Double[listdata.size()];

        //Lets create a Double[] array containing the customer lat/lon
        final Double[] custlat = new Double[listdata.size()];
        final Double[] custlon = new Double[listdata.size()];

        final int[] hrsAgo = new int[listdata.size()];
        final int[] minsAgo = new int[listdata.size()];

        //My latitude longitude coordinates
        mylocationRef.child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mylat[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(custlat[position], custlon[position], mylat[position], mylon[position], "K");
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();
                    if(dist[position] < 1.0){
                        holder.distAway.setText(dist[position]*1000 + " m away");
                    } else {
                        holder.distAway.setText(dist[position] + " km away");
                    }
                } catch (Exception e){

                }

                //Split time details
                String[] parts = receivedOrders.getOrderedOn().split(":");
                final String date = parts[0];
                final String hours = parts[1];
                final String minutes = parts[2];
                final String seconds = parts[3];

                //get current time details and compare
                final String todaydate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                TimeZone timeZone = TimeZone.getTimeZone("GMT+03:00");
                final Calendar calendar = Calendar.getInstance(timeZone);
                final String currentHr = String.format("%02d" , calendar.get(Calendar.HOUR_OF_DAY));
                final String currentMin = String.format("%02d" , calendar.get(Calendar.MINUTE));
                //final String currentSec = String.format("%02d" , calendar.get(Calendar.SECOND));

                //Toast.makeText(context, millisUntilFinished + "", Toast.LENGTH_SHORT).show();
                String currentSec = String.format("%02d" , calendar.get(Calendar.SECOND));

                //First find out if we're dealing with today
                if(!date.equals(todaydate)){ //Not today
                    holder.time.setText("Too long...");

                } else { // Today
                    hrsAgo[position] = Integer.parseInt(currentHr) - Integer.parseInt(hours);
                    minsAgo[position] = Integer.parseInt(minutes) - Integer.parseInt(currentMin);

                    if(hrsAgo[position] == 1){
                        holder.time.setText("1hr ago");
                    }

                    else if(hrsAgo[position] > 1){
                        holder.time.setText(Math.abs(hrsAgo[position]) + "hrs ago");
                    }
                    else {//hasn't reached 1 hr so is in minutes

                        int minsAgo = Integer.parseInt(currentMin) - Integer.parseInt(minutes);
                        if(minsAgo < 1){
                            int secsAGo = Integer.parseInt(currentSec) - Integer.parseInt(seconds);
                            holder.time.setText(Math.abs(secsAGo) + "s ago");
                        } else {
                            holder.time.setText(Math.abs(minsAgo) + "m ago");
                        }
                    }
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
                    if(dist[position] < 1.0){
                        holder.distAway.setText(dist[position]*1000 + " m away");
                    } else {
                        holder.distAway.setText(dist[position] + " km away");
                    }
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
                    if(dist[position] < 1.0){
                        holder.distAway.setText(dist[position]*1000 + " m away");
                    } else {
                        holder.distAway.setText(dist[position] + " km away");
                    }
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
                    if(dist[position] < 1.0){
                        holder.distAway.setText(dist[position]*1000 + " m away");
                    } else {
                        holder.distAway.setText(dist[position] + " km away");
                    }
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String[] customername = new String[listdata.size()];
        customerName.child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    customername[position] = dataSnapshot.getValue(String.class);
                    holder.providerName.setText(customername[position]);

                } catch (Exception e){
                    //Toast.makeText(context, "Error: " + e, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.foodPrice.setText("Ksh "+receivedOrders.getPrice());
        holder.foodName.setText(receivedOrders.getName());
        holder.foodDescription.setText(receivedOrders.getDescription());


        try {
            //Loading image from Glide library.
            Glide.with(context).load(receivedOrders.getImageURL()).into(holder.foodPic);
            //Log.d("glide", "onBindViewHolder: imageUrl: " + receivedOrders.getImageURL());
        } catch (Exception e){

        }

        final String[] status = new String[listdata.size()];

        //Check order status of each item
        myOrdersRef.child(receivedOrders.key).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                status[position] = dataSnapshot.getValue(String.class); //Order status
                //Toast.makeText(context, "Order status: " + status, Toast.LENGTH_SHORT).show();
                if(status[position].equals("confirmed")){
                    //holder.acceptBtn.setText("Cancel");
                    holder.acceptBtn.setBackgroundResource(R.drawable.ic_close_black_48dp);
                }

                if(status[position].equals("cancelled")){
                    holder.acceptBtn.setBackgroundResource(R.drawable.ic_check_black_48dp);
                    //holder.acceptBtn.setText("Confirm");
                }

                if(status[position].equals("pending")){
                    //holder.acceptBtn.setText("Confirm");
                    holder.acceptBtn.setBackgroundResource(R.drawable.ic_check_black_48dp);
                }

                if(status[position].equals("abort")){
                    //holder.acceptBtn.setText("Aborted");
                    holder.acceptBtn.setBackgroundResource(R.drawable.ic_sentiment_very_dissatisfied_black_48dp);
                    holder.acceptBtn.setEnabled(false);
                    holder.deleteItem.setVisibility(View.VISIBLE);
                    holder.trackCustomer.setVisibility(View.INVISIBLE);
                    // after this will put a timer below, max 2 mins then delete aborted order from list
                }
                } catch (Exception e){
                    try {
                    //Toast.makeText(context, "Error: " + e, Toast.LENGTH_SHORT).show();
                    deliveriesRef.child(receivedOrders.key).child("status").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                    status[position] = dataSnapshot.getValue(String.class); //Order status
                                    //Toast.makeText(context, "Order status: " + status, Toast.LENGTH_SHORT).show();
                                    if(status[position].equals("confirmed")){
                                        //holder.acceptBtn.setText("Cancel");
                                        holder.acceptBtn.setBackgroundResource(R.drawable.ic_close_black_48dp);
                                        holder.trackCustomer.setVisibility(View.VISIBLE);
                                    }

                                    if(status[position].equals("cancelled")){
                                        //holder.acceptBtn.setText("Confirm");
                                        holder.acceptBtn.setBackgroundResource(R.drawable.ic_check_black_48dp);
                                        holder.trackCustomer.setVisibility(View.INVISIBLE);
                                    }

                                    if(status[position].equals("pending")){
                                        //holder.acceptBtn.setText("Confirm");
                                        holder.acceptBtn.setBackgroundResource(R.drawable.ic_check_black_48dp);
                                        holder.trackCustomer.setVisibility(View.VISIBLE);
                                    }

                                    if(status[position].equals("abort")){
                                        //holder.acceptBtn.setText("Aborted");
                                        holder.acceptBtn.setBackgroundResource(R.drawable.ic_sentiment_very_dissatisfied_black_48dp);
                                        holder.acceptBtn.setEnabled(false);
                                        holder.deleteItem.setVisibility(View.VISIBLE);
                                        holder.trackCustomer.setVisibility(View.INVISIBLE);
                                        // after this will put a timer, max 2 mins then delete aborted order from list
                                    }
                            } catch (Exception e){

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    } catch (Exception ee){

                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Error: " + databaseError.toString() + ". Try again!", Toast.LENGTH_LONG).show();
            }
        });

        holder.deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                myOrdersRef.child(receivedOrders.key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                        Snackbar snackbar = Snackbar
                                .make(v, "Deleted!", Snackbar.LENGTH_LONG);

                        snackbar.show();
                    }
                });
            }
        });

        final String [] nduthiPhone = new String[1];
        holder.trackCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                nduthiPhone[0] = receivedOrders.getCustomerNumber();

                Intent slideactivity = new Intent(context, GeoFireActivity.class);
                slideactivity.putExtra("nduthi_phone", nduthiPhone);
                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(context, R.anim.animation,R.anim.animation2).toBundle();
                context.startActivity(slideactivity, bndlanimation);
            }
        });

        holder.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setTitle("Call Customer")
                        .setMessage("Call " + customername[position] + "?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String phone = receivedOrders.getCustomerNumber();
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                                context.startActivity(intent);
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //do nothing

                            }
                        })//setNegativeButton

                        .create();
                myQuittingDialogBox.show();
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
                                    progressDialog.setMessage("cancelling...");
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();

                                    receivedOrders.status = "cancelled";
                                    receivedOrders.sent = false;
                                    receivedOrders.orderedOn = receivedOrders.getOrderedOn();
                                    myOrdersRef.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Notify customer of cancelled order

                                            customerOrder.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Write was successful!
                                                    Toast.makeText(context, "Cancellation sent to: " + customername[position], Toast.LENGTH_LONG).show();

                                                    //Then delete the order item from my deliveries node

                                                    deliveriesRef.child(receivedOrders.key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            if(progressDialog.isShowing()){
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception exception) {
                                                            // Uh-oh, an error occurred!
                                                            Toast.makeText(context, "Error: " + exception, Toast.LENGTH_SHORT)
                                                                    .show();
                                                            if(progressDialog.isShowing()){
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    });
                                                }
                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            // Write failed
                                                            Toast.makeText(context, "Failed: " + e.toString() + ". Try again!", Toast.LENGTH_LONG).show();
                                                            if(progressDialog.isShowing()){
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    });
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Write failed
                                                    Toast.makeText(context, "Failed: " + e.toString() + ". Try again!", Toast.LENGTH_LONG).show();
                                                    if(progressDialog.isShowing()){
                                                        progressDialog.dismiss();
                                                    }
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

                                progressDialog.setMessage("accepting...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();

                                receivedOrders.status = "confirmed";
                                receivedOrders.sent = false;
                                //Change my order status to confirmed
                                myOrdersRef.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //Then update customer's order status as well
                                        customerOrder.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Write was successful!
                                                Toast.makeText(context, "Confirmation sent to: " + customername[position], Toast.LENGTH_LONG).show();

                                                //Add it to deliveries node
                                                deliveriesRef.child(receivedOrders.key).setValue(receivedOrders).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //then delete it from orders node

                                                        try {
                                                        myOrdersRef.child(receivedOrders.key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                try {
                                                                    if (progressDialog.isShowing()) {
                                                                        progressDialog.dismiss();
                                                                    }
                                                                } catch (Exception e){

                                                                }
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                // Uh-oh, an error occurred!
                                                                Toast.makeText(context, "Error: " + exception, Toast.LENGTH_SHORT)
                                                                        .show();
                                                                if(progressDialog.isShowing()){
                                                                    progressDialog.dismiss();
                                                                }
                                                            }
                                                        }); } catch (Exception e){

                                                        }

                                                    }
                                                });
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Write failed
                                                        Toast.makeText(context, "Failed: " + e.toString() + ". Try again!", Toast.LENGTH_LONG).show();

                                                        if(progressDialog.isShowing()){
                                                            progressDialog.dismiss();
                                                        }
                                                    }
                                                });
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Write failed
                                                Toast.makeText(context, "Failed: " + e.toString() + ". Try again!", Toast.LENGTH_LONG).show();
                                                if(progressDialog.isShowing()){
                                                    progressDialog.dismiss();
                                                }
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
            }
            }
        });

        holder.providerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!myPhone.equals(receivedOrders.getCustomerNumber())){
                    Intent slideactivity = new Intent(context, ViewProfile.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    slideactivity.putExtra("phone", receivedOrders.getCustomerNumber());
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(context, R.anim.animation,R.anim.animation2).toBundle();
                    context.startActivity(slideactivity, bndlanimation);
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

        return round(dist, 2);
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

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts a double to N places					 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        TextView foodPrice , foodDescription, foodName, providerName, distAway, time;
        ImageView foodPic;
        Button acceptBtn, callBtn;
        ImageButton deleteItem, trackCustomer;

        public MyHolder(View itemView) {
            super(itemView);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodName = itemView.findViewById(R.id.foodName);
            foodDescription = itemView.findViewById(R.id.foodDescription);
            foodPic = itemView.findViewById(R.id.foodPic);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            callBtn = itemView.findViewById(R.id.callBtn);
            providerName = itemView.findViewById(R.id.providerName);
            distAway = itemView.findViewById(R.id.distanceAway);
            deleteItem = itemView.findViewById(R.id.deleteBtn);
            trackCustomer = itemView.findViewById(R.id.trackCustomer);
            time = itemView.findViewById(R.id.timeOrdered);

        }
    }




}