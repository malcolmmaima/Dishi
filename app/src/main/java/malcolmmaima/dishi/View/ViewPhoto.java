package malcolmmaima.dishi.View;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;

import malcolmmaima.dishi.R;

public class ViewPhoto extends AppCompatActivity {

    ImageView viewImage;
    Toolbar topToolBar;
    DatabaseReference imageLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        viewImage = findViewById(R.id.imageView);

        android.support.v7.widget.Toolbar topToolBar = findViewById(R.id.toolbar);
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
        //topToolBar.setLogo(R.drawable.logo);
        //topToolBar.setLogoDescription(getResources().getString(R.string.logo_desc));

        final String imageLink = getIntent().getStringExtra("link");

        //Will need this data later on
        //final String phone = getIntent().getStringExtra("phone");
        //final String key = getIntent().getStringExtra("key");

        try {
            //Loading image from Glide library.
            Glide.with(ViewPhoto.this).load(imageLink).into(viewImage);
        } catch (Exception e){
            //Error
            Toast.makeText(ViewPhoto.this, "failed!", Toast.LENGTH_SHORT).show();
        }


    }
}
