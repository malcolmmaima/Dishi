package malcolmmaima.dishi.View.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.LinearLayout;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import malcolmmaima.dishi.Controller.TrackingService;
import malcolmmaima.dishi.Model.OrderDetails;
import malcolmmaima.dishi.Model.ProductDetails;
import malcolmmaima.dishi.R;

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

        final DatabaseReference mylocationRef, providerRef;
        FirebaseDatabase db;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String myPhone = user.getPhoneNumber(); //Current logged in user phone number

        // Assign FirebaseStorage instance to storageReference.

        db = FirebaseDatabase.getInstance();
        mylocationRef = db.getReference(myPhone + "/location"); //under each user, there's a location node with location coordinates
        providerRef = db.getReference(orderDetails.providerNumber + "/location");

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
                    dist[position] = distance(mylat[position], mylon[position], provlat[position], provlon[position], 0, 0);
                    //Toast.makeText(context,  "dist: (" + dist[position] + ")m to " + orderDetails.providerName, Toast.LENGTH_SHORT).show();

                    holder.distAway.setText(dist[position] + "m away");
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
                //Toast.makeText(context, "(my lon): " + mylon[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(mylat[position], mylon[position], provlat[position], provlon[position], 0, 0);
                    holder.distAway.setText(dist[position] + "m away");
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
                //Toast.makeText(context, orderDetails.providerName + " (lat): " + provlat[position], Toast.LENGTH_SHORT).show();
                try {
                    dist[position] = distance(mylat[position], mylon[position], provlat[position], provlon[position], 0, 0);
                    holder.distAway.setText(dist[position] + "m away");
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
                //Toast.makeText(context, "(prov lon): " + provlon[position], Toast.LENGTH_SHORT).show();

                try {
                    dist[position] = distance(mylat[position], mylon[position], provlat[position], provlon[position], 0, 0);
                    holder.distAway.setText(dist[position] + "m away");
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

                //Toast.makeText(context, "(Name): " + orderDetails.providerName + " (Phone): "+orderDetails.providerNumber, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
     *
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        //return Math.sqrt(distance);

        return distance;
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