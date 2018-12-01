package malcolmmaima.dishi.View.Activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import malcolmmaima.dishi.Model.DishiUser;
import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Adapters.UserAdapter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class Followers extends AppCompatActivity {

    String phone;
    DatabaseReference dbRef;
    public List<DishiUser> users;
    RecyclerView recyclerView;
    TextView emptyTag;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        recyclerView = findViewById(R.id.rview);
        emptyTag = findViewById(R.id.empty_tag);
        progressBar = findViewById(R.id.progressBar);

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Followers");

        phone = getIntent().getStringExtra("phone");
        //Toast.makeText(this, "phone: " + phone, Toast.LENGTH_SHORT).show();
        dbRef = FirebaseDatabase.getInstance().getReference(phone + "/followers");

        //Back button on toolbar
        topToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Go back to previous activity
            }
        });

        users = new ArrayList<>();

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final ArrayList<String> phones = new ArrayList<String>();

                String userPhone;
                for(DataSnapshot users_ : dataSnapshot.getChildren()){
                    userPhone = users_.getKey();
                    phones.add(userPhone); //Store the followers phone numbers in an arraylist, will be primary key for getting their data
                }


                for(int i = 0; i < phones.size(); i++ ){
                    //Toast.makeText(Followers.this, "phones: " + phones.get(i), Toast.LENGTH_SHORT).show();
                    DatabaseReference[] userRef = new DatabaseReference[phones.size()];

                    //Fetch individual user details from their node in firebase
                    userRef[i] = FirebaseDatabase.getInstance().getReference(phones.get(i));

                    final int finalI = i;
                    userRef[i].addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DishiUser dishiUser = dataSnapshot.getValue(DishiUser.class);
                            dishiUser.setPhone(phones.get(finalI));
                            users.add(dishiUser);

                            if (!users.isEmpty()) {
                                recyclerView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                recyclerView.setVisibility(VISIBLE);
                                UserAdapter recycler = new UserAdapter(Followers.this, users);
                                RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(Followers.this);
                                recyclerView.setLayoutManager(layoutmanager);
                                recyclerView.setItemAnimator(new SlideInLeftAnimator());

                                recycler.notifyDataSetChanged();

                                recyclerView.getItemAnimator().setAddDuration(1000);
                                recyclerView.getItemAnimator().setRemoveDuration(1000);
                                recyclerView.getItemAnimator().setMoveDuration(1000);
                                recyclerView.getItemAnimator().setChangeDuration(1000);

                                recyclerView.setAdapter(recycler);
                                emptyTag.setVisibility(View.GONE);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                recyclerView.setVisibility(INVISIBLE);
                                emptyTag.setVisibility(VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                                emptyTag.setText("No followers");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //Toast.makeText(Followers.this, "Size: " + users.size(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
