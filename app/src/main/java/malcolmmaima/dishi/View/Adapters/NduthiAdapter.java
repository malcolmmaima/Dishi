package malcolmmaima.dishi.View.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.List;

import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.NduthiNearMe;
import malcolmmaima.dishi.Model.RequestNduthi;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.SelectNduthiGuy;

import static malcolmmaima.dishi.R.drawable.ic_delivered_order;
import static malcolmmaima.dishi.R.drawable.ic_order_in_transit;
import static malcolmmaima.dishi.R.drawable.ic_pending_order;

public class NduthiAdapter extends RecyclerView.Adapter<NduthiAdapter.MyHolder>{

    SelectNduthiGuy context;
    List<NduthiNearMe> listdata;

    public NduthiAdapter(SelectNduthiGuy context, List<NduthiNearMe> listdata) {
        this.listdata = listdata;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nduthi_card,parent,false);

        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }


    public void onBindViewHolder(final MyHolder holder, final int position) {
        final NduthiNearMe nduthiNearMe = listdata.get(position);
        final DatabaseReference myRef, mylocationRef, nduthiRef, requestRideRef, nduthiGuyRef, nduthiRequests;
        FirebaseDatabase db;

        final Double[] provlon = new Double[listdata.size()];
        final Double[] provlat = new Double[listdata.size()];
        final Double[] myLat = new Double[listdata.size()];
        final Double[] myLong = new Double[listdata.size()];
        final Double[] dist = new Double[listdata.size()];

        final String[] itemCount = new String[1];

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number
        final String[] myName = new String[1];

        // Assign FirebaseStorage instance to storageReference.

        db = FirebaseDatabase.getInstance();
        requestRideRef = db.getReference(nduthiNearMe.phone + "/request_ride");
        nduthiGuyRef = db.getReference(nduthiNearMe.phone);
        nduthiRequests = db.getReference(nduthiNearMe.phone + "/request_menus"+"/request_"+myPhone);

        myRef = db.getReference(myPhone);

        try {

            nduthiGuyRef.child("engaged").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //Toast.makeText(context, "engaged: " + dataSnapshot.getValue(), Toast.LENGTH_LONG).show();

                    if(dataSnapshot.getValue().equals("false")){
                        //Default state is green meaning is active and ready to receive requests
                        Glide.with(context).load(ic_delivered_order).into(holder.nduthiStat);
                        holder.nduthiStatMsg.setText("Ready");
                    }

                    //Once confimed request, set status to pending as nduthi goes to collect the orders
                    if(dataSnapshot.getValue().equals("true")){
                        Glide.with(context).load(ic_pending_order).into(holder.nduthiStat);
                        holder.nduthiStatMsg.setText("engaged");
                    }

                    //Is actively in trnsit delivering an order
                    if(dataSnapshot.getValue().equals("transit")) {
                        Glide.with(context).load(ic_order_in_transit).into(holder.nduthiStat);
                        holder.nduthiStatMsg.setText("Busy");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (Exception e){

        }



        //Toast.makeText(context, "provider" + (" x:" + provlat[0] +" y:"+ provlon[0]) , Toast.LENGTH_SHORT).show();

        holder.callNduthi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setTitle("Call Nduthi")
                        .setCancelable(false)
                        .setMessage("Call "+nduthiNearMe.name+"?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String phone = nduthiNearMe.phone;
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

        holder.selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        myName[0] = dataSnapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setTitle("Select "+nduthiNearMe.name)
                        .setCancelable(false)
                        .setMessage("Do you want "+nduthiNearMe.name + " to fulfil your order?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Send request to nduthi guy for delivery of the said products
                                final String key = requestRideRef.push().getKey();
                                RequestNduthi requestNduthi = new RequestNduthi();
                                requestNduthi.status = "pending";
                                requestNduthi.name = myName[0];
                                requestNduthi.phone = myPhone;
                                requestRideRef.child(key).setValue(requestNduthi).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Send the items I want to the nduthi guy so he can view them as well
                                        myRef.child("mycart").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot cart : dataSnapshot.getChildren()){
                                                    final MyCartDetails myCartDetails = cart.getValue(MyCartDetails.class);
                                                    //Toast.makeText(context, "cart=> provider:" + myCartDetails.providerNumber
                                                    //        + "\n,item: " + myCartDetails.getName(), Toast.LENGTH_SHORT).show();

                                                    String key = nduthiRequests.push().getKey();
                                                    nduthiRequests.child(key).setValue(myCartDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            //My cart items sent to nduthi guy
                                                            Toast.makeText(context, "Request sent to "
                                                                    + nduthiNearMe.name + "! wait for confirmation or call nduthi!", Toast.LENGTH_SHORT).show();

                                                            holder.selectBtn.setEnabled(false);
                                                            holder.selectBtn.setText("Sent");

                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                myQuittingDialogBox.show();
            }
        });

        nduthiRef = db.getReference(nduthiNearMe.phone);
        //Item provider latitude longitude coordinates
        nduthiRef.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot providerLoc : dataSnapshot.getChildren()){

                    try {
                        if(providerLoc.getKey().equals("longitude")){
                            provlon[position] = providerLoc.getValue(Double.class);
                            //Toast.makeText(getContext(), "(prov lat): " + provlon[0], Toast.LENGTH_SHORT).show();
                        }

                        if(providerLoc.getKey().equals("latitude")){
                            provlat[position] = providerLoc.getValue(Double.class);
                            //Toast.makeText(getContext(), "(prov lon): " + provlat[0], Toast.LENGTH_SHORT).show();
                        }

                        dist[position] = distance(provlat[position], provlon[position], myLat[position], myLong[position], "K");
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

                        dist[position] = distance(provlat[position], provlon[position], myLat[position], myLong[position], "K");
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

        holder.nduthiName.setText(nduthiNearMe.name);
        holder.nduthiBio.setText(nduthiNearMe.bio);
        holder.tripsMade.setText("Trips made");


        //Loading image from Glide library.
        Glide.with(context).load(nduthiNearMe.profilepic).into(holder.nduthiProfile);

    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        TextView nduthiName , nduthiBio, tripsMade, distanceAway, nduthiStatMsg;
        ImageView nduthiProfile, nduthiStat;
        Button callNduthi, selectBtn;

        public MyHolder(View itemView) {
            super(itemView);
            nduthiName = itemView.findViewById(R.id.nduthiName);
            nduthiBio = itemView.findViewById(R.id.nduthiBio);
            nduthiProfile = itemView.findViewById(R.id.nduthiProfile);
            tripsMade = itemView.findViewById(R.id.tripsMade);
            distanceAway = itemView.findViewById(R.id.distanceAway);
            callNduthi = itemView.findViewById(R.id.callNduthi);
            selectBtn = itemView.findViewById(R.id.selectBtn);
            nduthiStat = itemView.findViewById(R.id.nduthiStat);
            nduthiStatMsg = itemView.findViewById(R.id.nduthiStatMsg);

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