package malcolmmaima.dishi.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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

import malcolmmaima.dishi.Model.RestaurantDetails;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.ViewPagerAdapter;
import malcolmmaima.dishi.View.Fragments.RestaurantMenu;

public class ViewRestaurant extends AppCompatActivity {
    FirebaseAuth mAuth;
    DatabaseReference restaurantRef, myFavourites, providerFavs;
    ImageView restaurantPic, favourite, callBtn, shareRest;
    TextView restaurantName, distAway, likes;
    String RestaurantName;
    Double provlat, provlon, mylat, mylon, dist;

    //This is our tablayout
    private TabLayout tabLayout;

    //This is our viewPager
    private ViewPager viewPager;

    ViewPagerAdapter adapter;

    //Fragments
    RestaurantMenu restaurantMenu;
    RestaurantMenu restaurantReviews;
    RestaurantMenu restaurantStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_restaurant);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getInstance().getCurrentUser() == null){

            //User is not signed in, send them back to verification page
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ViewRestaurant.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));//Load Main Activity and clear activity stack
        }

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Go back to previous activity
            }
        });

        restaurantPic = findViewById(R.id.coverImageView);
        favourite = findViewById(R.id.likeImageView);
        callBtn = findViewById(R.id.callRestaurant);
        shareRest = findViewById(R.id.shareImageView);

        favourite.setTag(R.drawable.ic_like);

        distAway = findViewById(R.id.distanceAway);
        restaurantName = findViewById(R.id.titleTextView);
        likes = findViewById(R.id.likesTotal);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        setupViewPager(viewPager);

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tablayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(),false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.getTabAt(position).select();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        final String restaurantPhone = getIntent().getStringExtra("restaurant_phone");

        if(restaurantPhone.equals(null)){
            Toast.makeText(this, "Error fetching restaurant, try again!", Toast.LENGTH_LONG).show();
            finish();
        }
        setTitle("Restaurant");
        restaurantRef = FirebaseDatabase.getInstance().getReference(restaurantPhone);
        myFavourites = FirebaseDatabase.getInstance().getReference(myPhone);
        providerFavs = FirebaseDatabase.getInstance().getReference(restaurantPhone + "/favourites");

        //Fetch the restauant basic info
        restaurantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                RestaurantDetails restaurantDetails = dataSnapshot.getValue(RestaurantDetails.class);
                restaurantDetails.phone = restaurantPhone;

                try {
                    //Loading image from Glide library.
                    Glide.with(ViewRestaurant.this).load(restaurantDetails.getProfilepic()).into(restaurantPic);
                    restaurantName.setText(restaurantDetails.getName());
                    RestaurantName = restaurantDetails.getName();
                    Log.d("glide", "onBindViewHolder: imageUrl: " + restaurantDetails.getProfilepic());
                } catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Fetch location details

        //My latitude longitude coordinates
        myFavourites.child("location").child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mylat = dataSnapshot.getValue(Double.class);
                ////////
                myFavourites.child("location").child("longitude").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mylon = dataSnapshot.getValue(Double.class);
                        //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                        try {
                            dist = distance(provlat, provlon, mylat, mylon, "K");
                            //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                            if(dist < 1.0){
                                distAway.setText(dist*1000 + " m away");
                            } else {

                                distAway.setText(dist + " km away");

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

        restaurantRef.child("location").child("latitude").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                provlat = dataSnapshot.getValue(Double.class);
                //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();

                ////////
                restaurantRef.child("location").child("longitude").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        provlon = dataSnapshot.getValue(Double.class);
                        //Toast.makeText(context, "(my lat): " + mylat[position], Toast.LENGTH_SHORT).show();
                        try {
                            dist = distance(provlat, provlon, mylat, mylon, "K");
                            //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                            if (dist < 1.0) {
                                distAway.setText(dist * 1000 + " m away");
                            } else {
                                //notifyItemInserted(position);
                                distAway.setText(dist + " km away");

                            }
                        } catch (Exception e) {

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
                            likes.setText(""+likesTotal);
                        } catch (Exception e){

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                callBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog myQuittingDialogBox = new AlertDialog.Builder(v.getContext())
                                //set message, title, and icon
                                .setTitle("Call Restaurant")
                                .setMessage("Call " + RestaurantName + "?")
                                //.setIcon(R.drawable.icon) will replace icon with name of existing icon from project
                                //set three option buttons
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String phone = restaurantPhone;
                                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                                        startActivity(intent);
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

                //On loading fetch the like status
                myFavourites.child("restaurant_favs").child(restaurantPhone).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String phone = dataSnapshot.getValue(String.class);
                        try {
                            if (phone.equals("fav")) {
                                favourite.setTag(R.drawable.ic_liked);
                                favourite.setImageResource(R.drawable.ic_liked);
                            } else {
                                favourite.setTag(R.drawable.ic_like);
                                favourite.setImageResource(R.drawable.ic_like);
                            }
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                favourite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int id = (int) favourite.getTag();
                        if (id == R.drawable.ic_like) {
                            //Add to my favourites
                            myFavourites.child("restaurant_favs").child(restaurantPhone).setValue("fav").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    favourite.setTag(R.drawable.ic_liked);
                                    favourite.setImageResource(R.drawable.ic_liked);

                                    providerFavs.child(myPhone).setValue("fav").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Add favourite to restaurant's node as well
                                        }
                                    });
                                    //Toast.makeText(context,restaurantDetails.getName()+" added to favourites",Toast.LENGTH_SHORT).show();
                                }
                            });


                        } else {
                            //Remove from my favourites
                            myFavourites.child("restaurant_favs").child(restaurantPhone).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    favourite.setTag(R.drawable.ic_like);
                                    favourite.setImageResource(R.drawable.ic_like);

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    private void setupViewPager(ViewPager viewPager)
    {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        restaurantMenu = new RestaurantMenu();
        restaurantReviews = new RestaurantMenu();
        restaurantStats = new RestaurantMenu();

        adapter.addFragment(restaurantMenu,"Menu");
        adapter.addFragment(restaurantReviews,"Reviews");
        adapter.addFragment(restaurantStats,"Stats");
        viewPager.setAdapter(adapter);
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
}