package malcolmmaima.dishi.View;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import malcolmmaima.dishi.R;

public class SetupProfile extends AppCompatActivity {

    Button logoutbutton;
    private TextView name;
    private ImageView image;
    private  TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");

        name = (TextView) findViewById(R.id.name);
        status = (TextView)findViewById(R.id.status);


        //name.setTypeface(face1);
        //status.setTypeface(face2);

        setContentView(R.layout.profile_setup);
        logoutbutton=(Button)findViewById(R.id.logoutbt);
        logoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SetupProfile.this,MainActivity.class));
                finish();
            }
        });
    }
}