package malcolmmaima.dishi.View;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import malcolmmaima.dishi.Model.User;
import malcolmmaima.dishi.R;

public class MyAccount extends AppCompatActivity {

    String Verified;
    EditText editText;
    Button fetch;
    DatabaseReference rootRef,demoRef;
    TextView demoValue;
    String myPhone;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getInstance().getCurrentUser() == null){

            //User is not signed in, send them back to verification page
            Toast.makeText(this, "Not logged in!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(MyAccount.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));//Load Main Activity and clear activity stack
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myPhone = user.getPhoneNumber(); //Current logged in user phone number

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = db.getReference(myPhone);

        //Check whether user is verified, if true send them directly to MyAccount
        dbRef.child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String account_name = dataSnapshot.getValue(String.class);
                Toast.makeText(MyAccount.this, "Welcome " + account_name, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        demoValue = findViewById(R.id.tvValue);
        fetch = findViewById(R.id.btnFetch);
    }
}
