package malcolmmaima.dishi.View.Adapters;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import malcolmmaima.dishi.Model.RequestNduthi;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Activities.ViewRequestItems;

import static malcolmmaima.dishi.R.drawable.ic_delivered_order;
import static malcolmmaima.dishi.R.drawable.ic_order_in_transit;
import static malcolmmaima.dishi.R.drawable.ic_pending_order;

public class DeliveryRequestsNduthi extends RecyclerView.Adapter<DeliveryRequestsNduthi.MyHolder>{

    Context context;
    List<RequestNduthi> listdata;

    public DeliveryRequestsNduthi(Context context, List<RequestNduthi> listdata) {
        this.listdata = listdata;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delivery_requests_card,parent,false);

        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }


    public void onBindViewHolder(final MyHolder holder, final int position) {
        final RequestNduthi requestNduthi = listdata.get(position);
        final DatabaseReference myRef;
        final DatabaseReference mylocationRef;
        DatabaseReference customerRef = null;
        final DatabaseReference requestRideRef;
        final DatabaseReference nduthiGuyRef;
        final DatabaseReference customerRequests;
        FirebaseDatabase db;

        final Double[] custlon = new Double[listdata.size()];
        final Double[] custlat = new Double[listdata.size()];
        final Double[] myLat = new Double[listdata.size()];
        final Double[] myLong = new Double[listdata.size()];
        final Double[] dist = new Double[listdata.size()];

        final String[] itemCount = new String[listdata.size()];

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        // Assign FirebaseStorage instance to storageReference.

        db = FirebaseDatabase.getInstance();
        customerRequests = db.getReference(myPhone + "/request_menus"+"/request_"+requestNduthi.phone); //get customer shopping list

        myRef = db.getReference(myPhone);

        try {
            if (requestNduthi.status.equals("pending")) {
                //Default state is green meaning is active and ready to receive requests
                    Glide.with(context).load(ic_pending_order).into(holder.orderStatIcon);
                    holder.orderStatus.setText("pending"); }

                //Once customer has confirmed delivery
            if (requestNduthi.status.equals("confirmed")) {
                    Glide.with(context).load(ic_delivered_order).into(holder.orderStatIcon);
                    holder.orderStatus.setText("confirmed"); }

                //You have accepted the order. in transit to the customer
            if (requestNduthi.status.equals("transit")) {
                    Glide.with(context).load(ic_order_in_transit).into(holder.orderStatIcon);
                    holder.orderStatus.setText("transit"); }

            customerRequests.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    itemCount[position] = String.valueOf(dataSnapshot.getChildrenCount());
                    holder.itemCount.setText(itemCount[position] + " items");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } catch (Exception e){ }

        //Toast.makeText(context, "provider" + (" x:" + provlat[0] +" y:"+ provlon[0]) , Toast.LENGTH_SHORT).show();

        holder.callCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setTitle("Call Customer")
                        .setCancelable(false)
                        .setMessage("Call "+requestNduthi.name+"?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String phone = requestNduthi.phone;
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


        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent slideactivity = new Intent(context, ViewRequestItems.class);
                String [] nduthiPhone = new String[1];
                nduthiPhone[0] = requestNduthi.phone;

                if(nduthiPhone != null && itemCount[position] != null && requestNduthi.key != null){

                    slideactivity.putExtra("customer_phone", nduthiPhone);
                    slideactivity.putExtra("customer_name", requestNduthi.name);
                    slideactivity.putExtra("item_count", itemCount[position]);
                    slideactivity.putExtra("profile_pic", requestNduthi.profilepic);
                    slideactivity.putExtra("key", requestNduthi.key);

                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(context, R.anim.animation,R.anim.animation2).toBundle();
                    context.startActivity(slideactivity, bndlanimation);
                }

            }
        });

        try {
            customerRef = db.getReference(requestNduthi.phone);

            //customer latitude longitude coordinates
            customerRef.child("location").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot providerLoc : dataSnapshot.getChildren()){

                        try {
                            if(providerLoc.getKey().equals("longitude")){
                                custlon[position] = providerLoc.getValue(Double.class);
                                //Toast.makeText(getContext(), "(prov lat): " + provlon[0], Toast.LENGTH_SHORT).show();
                            }

                            if(providerLoc.getKey().equals("latitude")){
                                custlat[position] = providerLoc.getValue(Double.class);
                                //Toast.makeText(getContext(), "(prov lon): " + provlat[0], Toast.LENGTH_SHORT).show();
                            }

                            dist[position] = distance(custlat[position], custlon[position], myLat[position], myLong[position], "K");
                            //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                            if(dist[position] < 1.0){
                                holder.distanceAway.setText(dist[position]*1000 + " m away");
                            } else {
                                holder.distanceAway.setText(dist[position] + " km away");
                            }


                        } catch (Exception e){
                            //Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e){

        }

        mylocationRef = db.getReference(myPhone);

        mylocationRef.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot providerLoc : dataSnapshot.getChildren()){

                    try {
                        if(providerLoc.getKey().equals("longitude")){
                            myLong[position] = providerLoc.getValue(Double.class);
                            //Toast.makeText(getContext(), "(prov lat): " + provlon[0], Toast.LENGTH_SHORT).show();
                        }

                        if(providerLoc.getKey().equals("latitude")){
                            myLat[position] = providerLoc.getValue(Double.class);
                            //Toast.makeText(getContext(), "(prov lon): " + provlat[0], Toast.LENGTH_SHORT).show();
                        }

                        dist[position] = distance(custlat[position], custlon[position], myLat[position], myLong[position], "K");
                        //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                        if(dist[position] < 1.0){
                            holder.distanceAway.setText(dist[position]*1000 + " m away");
                        } else {
                            holder.distanceAway.setText(dist[position] + " km away");
                        }


                    } catch (Exception e){
                        //Toast.makeText(context, "" + e, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.customerName.setText(requestNduthi.name);
        holder.distanceAway.setText("0 m away");

        try {
            //Loading image from Glide library.
            Glide.with(context).load(requestNduthi.profilepic).into(holder.customerPic);
        } catch (Exception e){

        }

    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        TextView customerName , itemCount, distanceAway, orderStatus;
        ImageView customerPic, orderStatIcon;
        Button callCustomer;
        CardView cardView;

        public MyHolder(View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customerName);
            customerPic = itemView.findViewById(R.id.customerPic);
            orderStatIcon = itemView.findViewById(R.id.orderStat);
            itemCount = itemView.findViewById(R.id.itemCount);
            distanceAway = itemView.findViewById(R.id.distanceAway);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            callCustomer = itemView.findViewById(R.id.callCustomer);
            cardView = itemView.findViewById(R.id.card_view);

        }
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


}