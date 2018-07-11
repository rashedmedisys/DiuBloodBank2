package com.diu.bloodbank;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.diu.bloodbank.R.color.colorAccent;

public class ResultUserProfileActivity extends AppCompatActivity {

    private CircleImageView mResultUserImage;
    private TextView mResultUserName;
    private TextView mResultUserGroup;
    private TextView mResultUserLocation;
    private Button mAskForBloodButton;
    private Button mDecilenRequestButton;
    private ImageButton mResultUserCallButton;
    private String resultUserPhone;

    private String mCurrentState;
    private String mCurrentUser;

    private DatabaseReference mResultUserDataRef;
    private DatabaseReference mConnectRequestDatabase;
    private DatabaseReference mConnectedDatabase;
    private DatabaseReference mNotificationDatabase;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_user);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        final String resultUserId = getIntent().getStringExtra("resultUserId");

        mResultUserImage = findViewById(R.id.resultUserImage);
        mResultUserName = findViewById(R.id.resultUserName);
        mResultUserGroup = findViewById(R.id.resultUserGroup);
        mResultUserLocation = findViewById(R.id.resultUserLocation);
        mAskForBloodButton = findViewById(R.id.askForBloodButton);
        mDecilenRequestButton = findViewById(R.id.declineRequestButton);
        mResultUserCallButton = findViewById(R.id.resultUserCallButton);

        mCurrentState = "not_connected";

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mConnectRequestDatabase = FirebaseDatabase.getInstance().getReference().child("ConnectRequest");
        mConnectedDatabase = FirebaseDatabase.getInstance().getReference().child("Connected");
        mResultUserDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(resultUserId);
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mResultUserDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("photoUri")){
                    String photo = dataSnapshot.child("photoUri").getValue().toString();
                    Picasso.with(getApplicationContext()).load(photo).placeholder(R.drawable.avatar).into(mResultUserImage);
                }

                if(dataSnapshot.hasChild("name")){
                    mResultUserName.setText(dataSnapshot.child("name").getValue().toString());
                }
                if(dataSnapshot.hasChild("bloodGroup")){
                    mResultUserGroup.setText("Group:  "+dataSnapshot.child("bloodGroup").getValue().toString());
                }

                if(dataSnapshot.hasChild("area")){
                    mResultUserLocation.setText(dataSnapshot.child("area").getValue().toString());
                }

                if(dataSnapshot.hasChild("phone")){
                    resultUserPhone = dataSnapshot.child("phone").getValue().toString();
                }

                //----------- CONNECT LIST / REQUEST FEATURE---------------------

                mConnectRequestDatabase.child(mCurrentUser).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(resultUserId)){
                            String requestType = dataSnapshot.child(resultUserId).child("requestType").getValue().toString();

                            if(requestType.equals("received")){

                                mCurrentState = "req_received";
                                mAskForBloodButton.setText("Accept Blood Request");
                                mAskForBloodButton.setBackgroundColor(Color.GRAY);

                                //SHOWING DECLINE BUTTON
                                mDecilenRequestButton.setVisibility(View.VISIBLE);
                                mDecilenRequestButton.setEnabled(true);

                            }else if (requestType.equals("sent")){

                                mCurrentState = "req_sent";
                                mAskForBloodButton.setText("Cancel Asking For Blood");
                                mAskForBloodButton.setBackgroundColor(Color.GRAY);

                            }

                            progressDialog.dismiss();

                        } else {

                            mConnectedDatabase.child(mCurrentUser).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(resultUserId)){

                                        mCurrentState = "connected";
                                        mAskForBloodButton.setText("Disconnect Donor");
                                        mAskForBloodButton.setBackgroundResource(R.drawable.gradiant_background);

                                    }
                                    progressDialog.dismiss();


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mResultUserCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", resultUserPhone, null));
                startActivity(intent);

            }
        });

        mAskForBloodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //HIDING BUTTON FOR SAME USER
                if(mCurrentUser == resultUserId){
                    mAskForBloodButton.setVisibility(View.GONE);
                    mResultUserCallButton.setVisibility(View.GONE);
                }

                mAskForBloodButton.setEnabled(false);

                //-----------NOT CONNECTED--------------

                if(mCurrentState.equals("not_connected")){

                    mConnectRequestDatabase.child(mCurrentUser).child(resultUserId).child("requestType").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                mConnectRequestDatabase.child(resultUserId).child(mCurrentUser).child("requestType")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", mCurrentUser);
                                        notificationData.put("type", "request");

                                        mNotificationDatabase.child(resultUserId).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {



                                            }
                                        });

                                        mCurrentState = "req_sent";
                                        mAskForBloodButton.setText("Cancel Asking For Blood");
                                        mAskForBloodButton.setBackgroundColor(Color.GRAY);
                                        //Toast.makeText(ResultUserProfileActivity.this, "Connect Request Sent!", Toast.LENGTH_SHORT).show();


                                    }
                                });

                            }else {
                                Toast.makeText(ResultUserProfileActivity.this, "Failed Connecting Request", Toast.LENGTH_SHORT).show();
                            }

                            mAskForBloodButton.setEnabled(true);

                        }
                    });

                }

                //------CANECL REQUEST--------
                if(mCurrentState.equals("req_sent")){

                    mConnectRequestDatabase.child(mCurrentUser).child(resultUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mConnectRequestDatabase.child(resultUserId).child(mCurrentUser).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mAskForBloodButton.setEnabled(true);
                                    mCurrentState = "not_connected";
                                    mAskForBloodButton.setText("Ask For Blood");
                                    mAskForBloodButton.setBackgroundResource(R.drawable.gradiant_background);

                                }
                            });

                        }
                    });

                }

                //-----------REQUEST RECEIVED STATE-----------
                if(mCurrentState.equals("req_received")){

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    mConnectedDatabase.child(mCurrentUser).child(resultUserId).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mConnectedDatabase.child(resultUserId).child(mCurrentUser).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mConnectRequestDatabase.child(mCurrentUser).child(resultUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mConnectRequestDatabase.child(resultUserId).child(mCurrentUser).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mAskForBloodButton.setEnabled(true);
                                                    mCurrentState = "connected";
                                                    mAskForBloodButton.setText("Disconnect Donor");
                                                    mAskForBloodButton.setBackgroundColor(Color.GRAY);

                                                }
                                            });

                                        }
                                    });

                                }
                            });

                        }
                    });

                }

                // MY OWN WRITTEN CODE apart from Tutorial
                //-----------CONNECTED STATE-------------------
                if(mCurrentState.equals("connected")){

                    mConnectedDatabase.child(mCurrentUser).child(resultUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mConnectedDatabase.child(resultUserId).child(mCurrentUser).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mCurrentState = "not_connected";
                                    mAskForBloodButton.setText("Ask For Blood");
                                    mAskForBloodButton.setBackgroundResource(R.drawable.gradiant_background);

                                }
                            });

                        }
                    });

                }


            }
        });

    }
}
