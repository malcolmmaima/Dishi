package malcolmmaima.dishi.View.Adapters;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.util.List;

import malcolmmaima.dishi.Model.MyCartDetails;
import malcolmmaima.dishi.Model.RestaurantDetails;
import malcolmmaima.dishi.Model.RestaurantReview;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.ViewProfile;

public class RestaurantReviewAdapter extends RecyclerView.Adapter<RestaurantReviewAdapter.MyHolder> {

    Context context;
    List<RestaurantReview> listdata;
    DatabaseReference customerNode, mylocationRef, providerRef, reviewNode;

    public RestaurantReviewAdapter(Context context, List<RestaurantReview> listdata) {
        this.listdata = listdata;
        this.context = context;
    }

    @NonNull
    @Override
    public RestaurantReviewAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_restaurant_review,parent,false);

        RestaurantReviewAdapter.MyHolder myHolder = new RestaurantReviewAdapter.MyHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RestaurantReviewAdapter.MyHolder holder, final int position) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        final RestaurantReview restaurantReview = listdata.get(position);
        customerNode = FirebaseDatabase.getInstance().getReference(restaurantReview.getPhone());
        mylocationRef = FirebaseDatabase.getInstance().getReference(myPhone + "/location");
        providerRef = FirebaseDatabase.getInstance().getReference(restaurantReview.getRestaurantphone() + "/location");
        reviewNode = FirebaseDatabase.getInstance().getReference(restaurantReview.getRestaurantphone() + "/customer_reviews");

        final Double[] dist = new Double[listdata.size()];
        //Lets create a Double[] array containing my lat/lon
        final Double[] mylat = new Double[listdata.size()];
        final Double[] mylon = new Double[listdata.size()];

        //Lets create a Double[] array containing the provider lat/lon
        final Double[] provlat = new Double[listdata.size()];
        final Double[] provlon = new Double[listdata.size()];

        final String profilePic[] = new String[listdata.size()];
        final String profileName[] = new String[listdata.size()];

        //Allow user to delete their reviews by showing delete button only if that review belongs to them
        if(myPhone.equals(restaurantReview.getPhone())){
            holder.deleteBtn.setVisibility(View.VISIBLE);

            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog deleteDialogBox = new AlertDialog.Builder(v.getContext())
                            //set message, title, and icon
                            .setMessage("Delete review?")
                            //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                            //set three option buttons
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    reviewNode.child(restaurantReview.key).removeValue();

                                }
                            })//setPositiveButton


                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //Do not delete
                                    //Toast.makeText(context, "No", Toast.LENGTH_SHORT).show();

                                }
                            })//setNegativeButton

                            .create();
                    deleteDialogBox.show();
                }
            });
        }
        else {
            holder.deleteBtn.setVisibility(View.INVISIBLE);
        }

        holder.userReview.setText(restaurantReview.getReview());

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
                                //holder.distAway.setText(dist[position]*1000 + " m away");
                                holder.distAway.setVisibility(View.INVISIBLE);

                            } else {
                                notifyDataSetChanged();
                                //notifyItemInserted(position);
                                //holder.distAway.setText(dist[position] + " km away");
                                holder.distAway.setVisibility(View.INVISIBLE);

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
                                //holder.distAway.setText(dist[position]*1000 + " m away");
                                holder.distAway.setVisibility(View.INVISIBLE);
                            } else {
                                //notifyItemInserted(position);
                                //holder.distAway.setText(dist[position] + " km away");
                                holder.distAway.setVisibility(View.INVISIBLE);

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
            customerNode.child("profilepic").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    profilePic[position] = dataSnapshot.getValue(String.class);
                    Glide.with(context).load(profilePic[position]).into(holder.profilePic);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //Fetch name
            customerNode.child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    profileName[position] = dataSnapshot.getValue(String.class);
                    holder.customerName.setText(profileName[position]);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } catch (Exception e){

        }

        holder.customerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!myPhone.equals(restaurantReview.getPhone())){
                    Intent slideactivity = new Intent(context, ViewProfile.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    slideactivity.putExtra("phone", restaurantReview.getPhone());
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(context, R.anim.animation,R.anim.animation2).toBundle();
                    context.startActivity(slideactivity, bndlanimation);
                }
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
        TextView customerName, distAway, userReview;
        ImageView profilePic, deleteBtn;

        public MyHolder(View itemView) {
            super(itemView);

            customerName = itemView.findViewById(R.id.profileName);
            distAway = itemView.findViewById(R.id.distanceAway);
            userReview = itemView.findViewById(R.id.userReview);
            profilePic = itemView.findViewById(R.id.profilePic);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
