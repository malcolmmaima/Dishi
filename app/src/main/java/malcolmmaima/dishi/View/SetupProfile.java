package malcolmmaima.dishi.View;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rey.material.widget.Switch;

import malcolmmaima.dishi.R;
import malcolmmaima.dishi.customfonts.EditText_Roboto_Regular;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SetupProfile extends AppCompatActivity implements com.rey.material.widget.Spinner.OnItemSelectedListener, AdapterView.OnItemSelectedListener {

    Button logoutbutton, continueBtn, backButton;
    private TextView name;
    private ImageView image;
    private  TextView status;

    EditText_Roboto_Regular userName, userEmail, changeNumber;
    EditText userBio;
    RadioButton maleRd, femaleRd;
    RadioGroup gender;
    Spinner accType;
    Switch notifications;

    int account_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_setup);

        Typeface face1 = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        Typeface face2 = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");

        //name = (TextView) findViewById(R.id.name);
        //status = (TextView)findViewById(R.id.status);

        //name.setTypeface(face1);
        //status.setTypeface(face2);

        //Our EditText Boxes
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.emailAddress);
        userBio = findViewById(R.id.userBio);
        changeNumber = findViewById(R.id.phoneNumber);

        //Our RadioButtons
        maleRd = findViewById(R.id.maleRd);
        femaleRd = findViewById(R.id.femaleRd);

        gender = findViewById(R.id.gender);


        //Our Account Type Spinner
        accType = findViewById(R.id.accType);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.accounts, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        accType.setAdapter(adapter);
        accType.setOnItemSelectedListener(this);

        //Our Notifications Switch
        notifications = findViewById(R.id.Notifications);

        continueBtn = findViewById(R.id.continueBtn);
        backButton = findViewById(R.id.backButton);
        logoutbutton = findViewById(R.id.logoutbt);

        logoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SetupProfile.this,MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(SetupProfile.this,MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(CheckFieldValidation()) {
                    String name = userName.getText().toString();
                    String email = userEmail.getText().toString();

                    int userGender = gender.getCheckedRadioButtonId();

                    // find the radio button by returned id
                    RadioButton radioButton = findViewById(userGender);

                    String gender = radioButton.getText().toString();

                    Boolean switchState = notifications.isChecked();

                    Toast.makeText(SetupProfile.this, "Name: " + name + " email: " + email + " Gender: " + gender + " Account Type:" + account_type + " Notification: " + switchState, Toast.LENGTH_LONG).show();

                    // Write user data to the database
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("User");

                    myRef.child("Name ").setValue(name);
                    //myRef.child("Cover_pic ").setValue(cover_pic);
                    myRef.child("Email ").setValue(email);
                    myRef.child("Gender ").setValue(gender);
                    myRef.child("Account type ").setValue(account_type);
                    myRef.child("Notifications").setValue(switchState);


                }

            }
        });
    }




    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        account_type = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        account_type = 0;
    }

    @Override
    public void onItemSelected(com.rey.material.widget.Spinner parent, View view, int position, long id) {
        //account_type = Integer.parseInt((String) accType.getItemAtPosition(position));

    }

    //checking if field are empty
    private boolean CheckFieldValidation(){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        boolean valid=true;

        if(userName.getText().toString().equals("")){
            userName.setError("Can't be Empty");
            valid=false;
        }

        if(userEmail.equals(emailPattern)){
            userEmail.setError("Invalid Email");
            valid=false;
        }

        if(userEmail.getText().toString().equals("")){
            userEmail.setError("Cant't be empty");
            valid=false;
        }

        if (gender.getCheckedRadioButtonId() == -1){

            Toast.makeText(SetupProfile.this, "You must select gender", Toast.LENGTH_SHORT).show();
            valid=false;
        }

        if(account_type == 0){
            Toast.makeText(this, "You must select account type", Toast.LENGTH_SHORT).show();
            valid=false;
        }


        return valid;
    }
}