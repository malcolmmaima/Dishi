package malcolmmaima.dishi.View.Adapters;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import malcolmmaima.dishi.Model.RestaurantDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.MainActivity;
import malcolmmaima.dishi.View.MyAccountRestaurant;
import malcolmmaima.dishi.View.ViewRestaurant;

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

        final Double[] dist = new Double[listdata.size()];
        //Lets create a Double[] array containing my lat/lon
        final Double[] mylat = new Double[listdata.size()];
        final Double[] mylon = new Double[listdata.size()];

        //Lets create a Double[] array containing the provider lat/lon
        final Double[] provlat = new Double[listdata.size()];
        final Double[] provlon = new Double[listdata.size()];

        final DatabaseReference mylocationRef, providerRef, myFavourites, providerFavs, restaurantRef;
        FirebaseDatabase db;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        // Assign FirebaseStorage instance to storageReference.

        db = FirebaseDatabase.getInstance();
        mylocationRef = db.getReference(myPhone + "/location"); //loggedin user location reference
        providerRef = db.getReference(restaurantDetails.phone + "/location"); //food item provider location reference
        providerFavs = db.getReference(restaurantDetails.phone + "/favourites");
        myFavourites = db.getReference(myPhone + "/restaurant_favs");
        restaurantRef = db.getReference(restaurantDetails.phone);

        //My latitude longitude coordinates
        mylocationRef.child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mylat[position] = dataSnapshot.getValue(Double.class);
                ////////
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
                                notifyDataSetChanged();
                                //notifyItemInserted(position);
                                holder.distAway.setText(dist[position] + " km away");

                            }
                        } catch (Exception e){

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                ///////
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
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

                ////////
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
                                //notifyItemInserted(position);
                                holder.distAway.setText(dist[position] + " km away");

                            }
                        } catch (Exception e){

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                ///////
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        try {
            //Loading image from Glide library.
            Glide.with(context).load(restaurantDetails.getProfilepic()).into(holder.profilePic);
            Log.d("glide", "onBindViewHolder: imageUrl: " + restaurantDetails.getProfilepic());
        } catch (Exception e){

        }

        //On laoding adapter fetch the like status
        myFavourites.child(restaurantDetails.phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String phone = dataSnapshot.getValue(String.class);
                try {
                    if (phone.equals("fav")) {
                        holder.likeImageView.setTag(R.drawable.ic_liked);
                        holder.likeImageView.setImageResource(R.drawable.ic_liked);
                    } else {
                        holder.likeImageView.setTag(R.drawable.ic_like);
                        holder.likeImageView.setImageResource(R.drawable.ic_like);
                    }
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        restaurantRef.child("favourites").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    int likesTotal = (int) dataSnapshot.getChildrenCount();
                    holder.likes.setText(""+likesTotal);
                } catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.likeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int id = (int)holder.likeImageView.getTag();
                if( id == R.drawable.ic_like){
                    //Add to my favourites
                    myFavourites.child(restaurantDetails.phone).setValue("fav").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            holder.likeImageView.setTag(R.drawable.ic_liked);
                            holder.likeImageView.setImageResource(R.drawable.ic_liked);

                            providerFavs.child(myPhone).setValue("fav").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Add favourite to restaurant's node as well
                                }
                            });
                            //Toast.makeText(context,restaurantDetails.getName()+" added to favourites",Toast.LENGTH_SHORT).show();
                        }
                    });


                } else{
                    //Remove from my favourites
                    myFavourites.child(restaurantDetails.phone).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            holder.likeImageView.setTag(R.drawable.ic_like);
                            holder.likeImageView.setImageResource(R.drawable.ic_like);

                            providerFavs.child(myPhone).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //remove favourite from restaurant's node as well
                                }
                            });
                            //Toast.makeText(context,restaurantDetails.getName()+" removed from favourites",Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

        holder.callRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                        //set message, title, and icon
                        .setTitle("Call Restaurant")
                        .setMessage("Call " + restaurantDetails.getName() + "?")
                        //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                        //set three option buttons
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String phone = restaurantDetails.phone;
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

        holder.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(restaurantDetails.phone != null){
                    //Slide to new activity
                    Intent slideactivity = new Intent(context, ViewRestaurant.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    slideactivity.putExtra("restaurant_phone", restaurantDetails.phone);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(context, R.anim.animation,R.anim.animation2).toBundle();
                    context.startActivity(slideactivity, bndlanimation);
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
    /*::	This function converts decimal degrees to radians			:*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts radians to decimal degrees			:*/
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
        TextView restaurantName, distAway, likes;
        ImageView profilePic, likeImageView, shareImageView, callRestaurant;

        public MyHolder(View itemView) {
            super(itemView);

            likeImageView = itemView.findViewById(R.id.likeImageView);
            shareImageView = itemView.findViewById(R.id.shareImageView);
            profilePic = itemView.findViewById(R.id.coverImageView);
            restaurantName = itemView.findViewById(R.id.titleTextView);
            distAway = itemView.findViewById(R.id.distanceAway);
            callRestaurant = itemView.findViewById(R.id.callRestaurant);
            likes = itemView.findViewById(R.id.likesTotal);

        }
    }
}
