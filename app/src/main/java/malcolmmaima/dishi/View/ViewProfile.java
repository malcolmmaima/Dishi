package malcolmmaima.dishi.View;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import malcolmmaima.dishi.R;

public class ViewProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        Toolbar topToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(topToolBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Profile");

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

        if(providerPhone.equals(null) || providerPhone == null || providerPhone.equals("null")){
            Toast.makeText(this, "Error fetching provider details, try again!", Toast.LENGTH_LONG).show();
            finish();
        }

        Toast.makeText(this, "phone: " + providerPhone, Toast.LENGTH_LONG).show();
    }
}
