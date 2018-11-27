package malcolmmaima.dishi.View;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import malcolmmaima.dishi.Model.StatusUpdateModel;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.CommentAdapter;
import malcolmmaima.dishi.View.Adapters.StatusUpdateAdapter;

import static android.view.View.INVISIBLE;

public class ViewStatus extends AppCompatActivity {

    DatabaseReference authorRef, myRef;
    TextView profileName, userUpdate, likesTotal, commentsTotal, emptyTag, timePosted;
    ImageView profilePic, deleteBtn, likePost, comments, sharePost;
    String myPhone;
    Button postStatus;
    EditText statusPost;
    RecyclerView recyclerView;
    List<StatusUpdateModel> list;
    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_status);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number

        profileName = findViewById(R.id.profileName);
        userUpdate = findViewById(R.id.userUpdate);
        profilePic = findViewById(R.id.profilePic);
        deleteBtn = findViewById(R.id.deleteBtn);
        likePost = findViewById(R.id.likePost);
        comments = findViewById(R.id.comments);
        sharePost = findViewById(R.id.sharePost);
        likesTotal = findViewById(R.id.likesTotal);
        commentsTotal = findViewById(R.id.commentsTotal);
        postStatus = findViewById(R.id.postStatus);
        statusPost = findViewById(R.id.inputComment);
        recyclerView = findViewById(R.id.rview);
        emptyTag = findViewById(R.id.empty_tag);
        timePosted = findViewById(R.id.timePosted);

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Comments");

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Go back to previous activity
            }
        });
        //topToolBar.setLogo(R.drawable.logo);
        //topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));

        final String providerPhone = getIntent().getStringExtra("phone");
        final String userName = getIntent().getStringExtra("username");
        final String update = getIntent().getStringExtra("update");
        final String pic = getIntent().getStringExtra("profilepic");
        final String key = getIntent().getStringExtra("key");
        final String currentWall = getIntent().getStringExtra("currentWall");
        final String timeposted = getIntent().getStringExtra("timePosted");

        authorRef = FirebaseDatabase.getInstance().getReference(currentWall);
        myRef = FirebaseDatabase.getInstance().getReference(myPhone);

        if(providerPhone.equals(null) || providerPhone == null || providerPhone.equals("null")){
            Toast.makeText(this, "Error fetching details, try again!", Toast.LENGTH_LONG).show();
            finish();
        }

        profileName.setText(userName);
        timePosted.setText(timeposted);

        profileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!myPhone.equals(providerPhone)){
                    Intent slideactivity = new Intent(ViewStatus.this, ViewProfile.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    slideactivity.putExtra("phone", providerPhone);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(ViewStatus.this, R.anim.animation,R.anim.animation2).toBundle();
                    startActivity(slideactivity, bndlanimation);
                }
            }
        });

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!myPhone.equals(providerPhone)){
                    Intent slideactivity = new Intent(ViewStatus.this, ViewProfile.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    slideactivity.putExtra("phone", providerPhone);
                    Bundle bndlanimation =
                            ActivityOptions.makeCustomAnimation(ViewStatus.this, R.anim.animation,R.anim.animation2).toBundle();
                    startActivity(slideactivity, bndlanimation);
                }
            }
        });

        userUpdate.setText(update);
        try {
            Glide.with(ViewStatus.this).load(pic).into(profilePic);
        } catch (Exception e){

        }

        postStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                TimeZone timeZone = TimeZone.getTimeZone("GMT+03:00");
                Calendar calendar = Calendar.getInstance(timeZone);
                String time = date+ ":" +
                        String.format("%02d" , calendar.get(Calendar.HOUR_OF_DAY))+":"+
                        String.format("%02d" , calendar.get(Calendar.MINUTE))+":"+
                        String.format("%02d" , calendar.get(Calendar.SECOND)); //+":"+
                //String.format("%03d" , calendar.get(Calendar.MILLISECOND));

                final StatusUpdateModel statusUpdateModel = new StatusUpdateModel();
                statusUpdateModel.setStatus(statusPost.getText().toString());
                statusUpdateModel.setTimePosted(time);
                statusUpdateModel.setAuthor(myPhone);
                statusUpdateModel.setPostedTo(providerPhone);

                final String commentKey = authorRef.push().getKey();

                if(statusPost.getText().toString().equals("")){
                    Toast.makeText(ViewStatus.this, "You must enter something!", Toast.LENGTH_SHORT).show();
                }

                else {
                    authorRef.child("status_updates").child(key).child("comments").child(commentKey).setValue(statusUpdateModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ViewStatus.this, "Comment posted!", Toast.LENGTH_SHORT).show();
                                statusUpdateModel.key = commentKey;
                                statusPost.setText("");
                            }
                        });

                }

            }

        });


        //Fetch status update likes
        authorRef.child("status_updates").child(key).child("likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likesTotal.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Fetch comments count
        authorRef.child("status_updates").child(key).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentsTotal.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //On loading adapter fetch the like status
        authorRef.child("status_updates").child(key).child("likes").child(myPhone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                phone = dataSnapshot.getValue(String.class);
                if(phone == null){
                    //Toast.makeText(context, "phoneLike: is null", Toast.LENGTH_SHORT).show();
                    likePost.setTag(R.drawable.ic_like);
                    likePost.setImageResource(R.drawable.ic_like);
                }
                else {
                    //Toast.makeText(context, "phoneLike: not null", Toast.LENGTH_SHORT).show();
                    likePost.setTag(R.drawable.ic_liked);
                    likePost.setImageResource(R.drawable.ic_liked);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        likePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int id = (int)likePost.getTag();
                if( id == R.drawable.ic_like){
                    //Add to my favourites
                    authorRef.child("status_updates").child(key).child("likes").child(myPhone).setValue("like").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            likePost.setTag(R.drawable.ic_liked);
                            likePost.setImageResource(R.drawable.ic_liked);
                            //Toast.makeText(context,restaurantDetails.getName()+" added to favourites",Toast.LENGTH_SHORT).show();
                        }
                    });


                } else{
                    //Remove from my favourites
                    authorRef.child("status_updates").child(key).child("likes").child(myPhone).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            likePost.setTag(R.drawable.ic_like);
                            likePost.setImageResource(R.drawable.ic_like);

                            //Toast.makeText(context,restaurantDetails.getName()+" removed from favourites",Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

        //Fetch the updates from status_updates node
        authorRef.child("status_updates").child(key).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list = new ArrayList<>();
                for(DataSnapshot updates : dataSnapshot.getChildren()){
                    StatusUpdateModel statusUpdateModel = updates.getValue(StatusUpdateModel.class);
                    statusUpdateModel.key = updates.getKey();
                    statusUpdateModel.setCommentKey(key);
                    statusUpdateModel.setCurrentWall(currentWall);
                    list.add(statusUpdateModel);
                }

                try {
                    if (!list.isEmpty()) {
                        emptyTag.setVisibility(INVISIBLE);
                        //Collections.reverse(list);
                        recyclerView.setVisibility(View.VISIBLE);
                        CommentAdapter recycler = new CommentAdapter(ViewStatus.this, list);
                        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(ViewStatus.this);
                        recyclerView.setLayoutManager(layoutmanager);
                        recyclerView.setItemAnimator(new SlideInLeftAnimator());

                        recycler.notifyDataSetChanged();

                        recyclerView.getItemAnimator().setAddDuration(1000);
                        recyclerView.getItemAnimator().setRemoveDuration(1000);
                        recyclerView.getItemAnimator().setMoveDuration(1000);
                        recyclerView.getItemAnimator().setChangeDuration(1000);

                        recyclerView.setAdapter(recycler);
                    } else {
                        emptyTag.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(INVISIBLE);
                    }
                }

                catch (Exception e){
                    recyclerView.setVisibility(INVISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
