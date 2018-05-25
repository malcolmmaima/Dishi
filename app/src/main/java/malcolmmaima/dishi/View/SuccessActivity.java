package malcolmmaima.dishi.View;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import malcolmmaima.dishi.R;

public class SuccessActivity extends AppCompatActivity {

    Button logoutbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
        logoutbutton=(Button)findViewById(R.id.logoutbt);
        logoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SuccessActivity.this,MainActivity.class));
                finish();
            }
        });
    }
}