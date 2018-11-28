package malcolmmaima.dishi.View.Adapters;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import malcolmmaima.dishi.Model.DishiUser;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Activities.ViewProfile;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyHolder> {

    Context context;
    List<DishiUser> listdata;
    DatabaseReference myRef, postStatus;

    public UserAdapter(Context context, List<DishiUser> listdata) {
        this.listdata = listdata;
        this.context = context;
    }

    @Override
    public UserAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user,parent,false);

        UserAdapter.MyHolder myHolder = new UserAdapter.MyHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.MyHolder holder, final int position) {

        final DishiUser dishiUser = listdata.get(position);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myPhone = user.getPhoneNumber(); //Current logged in user phone number
        myRef = FirebaseDatabase.getInstance().getReference(myPhone);
        final DatabaseReference [] dbRef = new DatabaseReference[listdata.size()];
        dbRef[position] = FirebaseDatabase.getInstance().getReference(dishiUser.getPhone());

        try {
            Glide.with(context).load(dishiUser.getProfilepic()).into(holder.profilePic);
        } catch (Exception e){

        }

        holder.profileName.setText(dishiUser.getName());
        holder.userBio.setText(dishiUser.getBio());

        if(dishiUser.getPhone().equals(myPhone)){
            holder.followUser.setVisibility(View.GONE);
        }

        //Check following status
        myRef.child("following").child(dishiUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    holder.followUser.setText("UNFOLLOW");
                } else {
                    holder.followUser.setText("FOLLOW");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.profileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(dishiUser.getPhone() != null){
                    if(!myPhone.equals(dishiUser.getPhone())){
                        Intent slideactivity = new Intent(context, ViewProfile.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        slideactivity.putExtra("phone", dishiUser.getPhone());
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(context, R.anim.animation,R.anim.animation2).toBundle();
                        context.startActivity(slideactivity, bndlanimation);
                    }
                }

            }
        });

        holder.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dishiUser.getPhone() != null){
                    if(!myPhone.equals(dishiUser.getPhone())){
                        Intent slideactivity = new Intent(context, ViewProfile.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        slideactivity.putExtra("phone", dishiUser.getPhone());
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(context, R.anim.animation,R.anim.animation2).toBundle();
                        context.startActivity(slideactivity, bndlanimation);
                    }
                }
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dishiUser.getPhone() != null){
                    if(!myPhone.equals(dishiUser.getPhone())){
                        Intent slideactivity = new Intent(context, ViewProfile.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        slideactivity.putExtra("phone", dishiUser.getPhone());
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(context, R.anim.animation,R.anim.animation2).toBundle();
                        context.startActivity(slideactivity, bndlanimation);
                    }
                }
            }
        });

        holder.followUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.followUser.getText().toString().equals("FOLLOW")){
                    myRef.child("following").child(dishiUser.getPhone()).setValue("following").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //update provider's node as well
                            dbRef[position].child("followers").child(myPhone).setValue("follower");
                        }
                    });
                }

                else {
                    myRef.child("following").child(dishiUser.getPhone()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //update provider's node as well
                            dbRef[position].child("followers").child(myPhone).removeValue();
                        }
                    });
                }

            }
        });

    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        TextView profileName, userBio;
        ImageView profilePic;
        CardView cardView;
        Button followUser;

        public MyHolder(View itemView) {
            super(itemView);

            profileName = itemView.findViewById(R.id.profileName);
            profilePic = itemView.findViewById(R.id.profilePic);
            cardView = itemView.findViewById(R.id.card_view);
            userBio = itemView.findViewById(R.id.userBio);
            followUser = itemView.findViewById(R.id.followUser);
        }
    }
}
