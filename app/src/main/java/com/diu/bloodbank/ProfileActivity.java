package com.diu.bloodbank;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView mProfileImage;
    private TextView mProfileName;
    private TextView mProfileDonatedNo;
    private TextView mProfileReceivedNo;
    private TextView mProfileBloodGroup;
    private TextView mProfileLocation;
    private TextView mProfilePhone;

    private DatabaseReference mProfileDatabaseRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProfileName = findViewById(R.id.profileName);
        mProfileImage = findViewById(R.id.profileImage);
        mProfileDonatedNo = findViewById(R.id.profileDonatedNumber);
        mProfileReceivedNo = findViewById(R.id.profileReceived);
        mProfileBloodGroup = findViewById(R.id.profileBloodGroup);
        mProfileLocation = findViewById(R.id.profileLocation);
        mProfilePhone = findViewById(R.id.profilePhone);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mProfileDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userId);

        mProfileDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("photoUri")){
                    String photo = dataSnapshot.child("photoUri").getValue().toString();
                    Picasso.with(getApplicationContext()).load(photo).placeholder(R.drawable.avatar).into(mProfileImage);
                }

                if(dataSnapshot.hasChild("name")){
                    mProfileName.setText(dataSnapshot.child("name").getValue().toString());
                }

                if(dataSnapshot.hasChild("donatedNo")){
                    mProfileDonatedNo.setText(dataSnapshot.child("donatedNo").getValue().toString());
                }

                if(dataSnapshot.hasChild("receivedNo")){
                    mProfileReceivedNo.setText(dataSnapshot.child("receivedNo").getValue().toString());
                }

                if(dataSnapshot.hasChild("bloodGroup")){
                    mProfileBloodGroup.setText("Group:  "+dataSnapshot.child("bloodGroup").getValue().toString());
                }

                if(dataSnapshot.hasChild("area")){
                    mProfileLocation.setText(dataSnapshot.child("area").getValue().toString());
                }

                if(dataSnapshot.hasChild("phone")){
                    mProfilePhone.setText(dataSnapshot.child("phone").getValue().toString());
                }

                progressDialog.dismiss();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
