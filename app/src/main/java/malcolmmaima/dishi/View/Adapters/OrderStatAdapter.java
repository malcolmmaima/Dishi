package malcolmmaima.dishi.View.Adapters;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.AddMenu;
import malcolmmaima.dishi.View.Map.GeoFireActivity;
import malcolmmaima.dishi.View.MyCart;
import malcolmmaima.dishi.View.OrderStatus;
import malcolmmaima.dishi.View.ViewPhoto;
import malcolmmaima.dishi.View.ViewProfile;

import static malcolmmaima.dishi.R.drawable.ic_check_circle_black_48dp;
import static malcolmmaima.dishi.R.drawable.ic_delivered_order;
import static malcolmmaima.dishi.R.drawable.ic_highlight_off_white_48dp;
import static malcolmmaima.dishi.R.drawable.ic_order_in_transit;
import static malcolmmaima.dishi.R.drawable.ic_pending_order;

public class OrderStatAdapter extends RecyclerView.Adapter<OrderStatAdapter.MyHolder>{

    Context context;
    List<MyCartDetails> listdata;

    public OrderStatAdapter(Context context, List<MyCartDetails> listdata) {
        this.listdata = listdata;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_stat_card,parent,false);

        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }


    public void onBindViewHolder(final MyHolder holder, final int position) {
        final MyCartDetails myCartDetails = listdata.get(position);
        final DatabaseReference mylocationRef, providerRef, pending, ordersHistory, provider;
        FirebaseDatabase db;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        // Assign FirebaseStorage instance to storageReference.

        db = FirebaseDatabase.getInstance();
        mylocationRef = db.getReference(myPhone + "/location"); //loggedin user location reference
        providerRef = db.getReference(myCartDetails.getProviderNumber() + "/location"); //food item provider location reference
        pending = db.getReference(myPhone + "/pending");
        ordersHistory = FirebaseDatabase.getInstance().getReference(myPhone + "/history");
        provider = db.getReference(myCartDetails.getProviderNumber());

        final String [] providerName = new String[listdata.size()];
        final Double[] dist = new Double[listdata.size()];

        //Lets create a Double[] array containing my lat/lon
        final Double[] mylat = new Double[listdata.size()];
        final Double[] mylon = new Double[listdata.size()];

        //Lets create a Double[] array containing the provider lat/lon
        final Double[] provlat = new Double[listdata.size()];
        final Double[] provlon = new Double[listdata.size()];
        final int[] hrsAgo = new int[listdata.size()];
        final int[] minsAgo = new int[listdata.size()];

        provider.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                providerName[position] = dataSnapshot.getValue(String.class);
                holder.providerName.setText(providerName[position]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //My latitude longitude coordinates
        mylocationRef.child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mylat[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(provlat[position], provlon[position], mylat[position], mylon[position], "K");
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();
                    if(dist[position] < 1.0){
                        holder.distAway.setText(dist[position]*1000 + " m away");
                    } else {
                        holder.distAway.setText(dist[position] + " km away");
                    }
                } catch (Exception e){

                }

                //Split time details
                String[] parts = myCartDetails.getOrderedOn().split(":");
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
                    dist[position] = distance(provlat[position], provlon[position], mylat[position], mylon[position], "K");
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();
                    if(dist[position] < 1.0){
                        holder.distAway.setText(dist[position]*1000 + " m away");
                    } else {
                        holder.distAway.setText(dist[position] + " km away");
                    }
                } catch (Exception e){

                }

                //Split time details
                String[] parts = myCartDetails.getOrderedOn().split(":");
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

        //Item provider latitude longitude coordinates
        providerRef.child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                provlat[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(provlat[position], provlon[position], mylat[position], mylon[position], "K");
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();
                    if(dist[position] < 1.0){
                        holder.distAway.setText(dist[position]*1000 + " m away");
                    } else {
                        holder.distAway.setText(dist[position] + " km away");
                    }
                } catch (Exception e){

                }

                //Split time details
                String[] parts = myCartDetails.getOrderedOn().split(":");
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

        providerRef.child("longitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                provlon[position] = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(provlat[position], provlon[position], mylat[position], mylon[position], "K");
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                    if(dist[position] < 1.0){
                        holder.distAway.setText(dist[position]*1000 + " m away");
                    } else {
                        holder.distAway.setText(dist[position] + " km away");
                    }
                } catch (Exception e){

                }

                //Split time details
                String[] parts = myCartDetails.getOrderedOn().split(":");
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

        //Toast.makeText(context, "provider" + (" x:" + provlat[0] +" y:"+ provlon[0]) , Toast.LENGTH_SHORT).show();

        if(myCartDetails.status.equals("pending")){
            Glide.with(context).load(ic_pending_order).into(holder.orderStat);
            holder.confirmOrd.setVisibility(View.GONE);
            holder.trackProvider.setVisibility(View.GONE);
        }

        if(myCartDetails.status.equals("confirmed")){
            Glide.with(context).load(ic_delivered_order).into(holder.orderStat);
            holder.confirmOrd.setVisibility(View.VISIBLE);
            holder.trackProvider.setVisibility(View.VISIBLE);
        }
        if(myCartDetails.status.equals("delivered")){
            Glide.with(context).load(ic_check_circle_black_48dp).into(holder.orderStat);
            holder.confirmOrd.setVisibility(View.VISIBLE);
            holder.trackProvider.setVisibility(View.VISIBLE);
        }
        if(myCartDetails.status.equals("transit")) {
            Glide.with(context).load(ic_order_in_transit).into(holder.orderStat);
            holder.confirmOrd.setVisibility(View.VISIBLE);
            holder.trackProvider.setVisibility(View.VISIBLE);
        }

        holder.confirmOrd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Processing...");
                progressDialog.setCancelable(false);

                final AlertDialog callBox = new AlertDialog.Builder(view.getContext())
                        //set message, title, and icon
                        .setTitle("Confirm Delivery")
                        .setMessage("Has " + providerName[position] + " delivered " + myCartDetails.getName() + "?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                progressDialog.show();
                                //Move order to orders history node
                                ordersHistory.child(myCartDetails.key).setValue(myCartDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Then remove from pending node
                                        pending.child(myCartDetails.key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //Update provider's node
                                                provider.child("history_deliveries").setValue(myCartDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //Then remove from provider's active deliveries node
                                                        provider.child("deliveries").child(myCartDetails.key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(context, myCartDetails.getName() + " confirmed!", Toast.LENGTH_LONG).show();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(context, "Something wrong occured!", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(context, "Something wrong occured!", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //do nothing

                            }
                        })//setNegativeButton

                        .create();
                callBox.show();
            }
        });
        holder.callProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog callBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setTitle("Call Provider")
                        .setMessage("Call " + providerName[position] + "?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String phone = myCartDetails.providerNumber;
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                                context.startActivity(intent);
                            }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //do nothing

                            }
                        })//setNegativeButton

                        .create();
                callBox.show();
            }
        });

        holder.foodPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imgActivity = new Intent(context, ViewPhoto.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                imgActivity.putExtra("link", myCartDetails.getImageURL());

                context.startActivity(imgActivity);
            }
        });

        holder.foodPrice.setText("Ksh "+myCartDetails.getPrice());
        holder.foodName.setText(myCartDetails.getName());
        holder.foodDescription.setText(myCartDetails.getDescription());
        holder.orderStatus.setText(myCartDetails.status);

        holder.foodName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Slide to new activity
                Intent slideactivity = new Intent(context, ViewProfile.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                slideactivity.putExtra("phone", myCartDetails.providerNumber);
                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(context, R.anim.animation,R.anim.animation2).toBundle();
                context.startActivity(slideactivity, bndlanimation);
            }
        });


        try {
            //Loading image from Glide library.
            Glide.with(context).load(myCartDetails.getImageURL()).into(holder.foodPic);
            Log.d("glide", "onBindViewHolder: imageUrl: " + myCartDetails.getImageURL());
        } catch (Exception e){

        }

        holder.providerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!myPhone.equals(myCartDetails.customerNumber)){
                    Intent slideactivity = new Intent(context, ViewProfile.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    slideactivity.putExtra("phone", myCartDetails.customerNumber);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(context, R.anim.animation,R.anim.animation2).toBundle();
                    context.startActivity(slideactivity, bndlanimation);
                }
            }
        });

        holder.trackProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent slideactivity = new Intent(context, GeoFireActivity.class);
                String [] phone_ = new String[1];
                phone_[0] = myCartDetails.getProviderNumber();
                slideactivity.putExtra("nduthi_phone", phone_);
                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(context, R.anim.animation, R.anim.animation2).toBundle();
                context.startActivity(slideactivity, bndlanimation);
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
        TextView foodPrice , foodDescription, foodName, providerName, distAway, orderStatus, time;
        ImageView foodPic, orderStat;
        Button callProvider, confirmOrd, trackProvider;

        public MyHolder(View itemView) {
            super(itemView);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            foodName = itemView.findViewById(R.id.foodName);
            foodDescription = itemView.findViewById(R.id.foodDescription);
            foodPic = itemView.findViewById(R.id.foodPic);
            providerName = itemView.findViewById(R.id.providerName);
            distAway = itemView.findViewById(R.id.distanceAway);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderStat = itemView.findViewById(R.id.orderStat);
            callProvider = itemView.findViewById(R.id.callProvider);
            confirmOrd = itemView.findViewById(R.id.confirmOrd);
            trackProvider = itemView.findViewById(R.id.trackProvider);
            time = itemView.findViewById(R.id.timeOrdered);

        }
    }


}