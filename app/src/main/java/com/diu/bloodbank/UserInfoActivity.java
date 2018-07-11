package com.diu.bloodbank;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Calendar;
import java.util.HashMap;

public class UserInfoActivity extends AppCompatActivity {

    private TextInputLayout mUserPhone;
    private TextInputLayout mUserArea;
    private TextInputLayout mUserBlood;
    private TextView mUserLastDonateDate;
    private Button mUserInfoSubmitButton;

    int year_x,month_x,day_x;
    private static final int DIALOG_ID = 0;
    private String userAssignedLastDoanteDate;

    private DatabaseReference mUserInfoDatabaseRef;

    private String userName, userPhotoUri, userPhone, userBloodGroup, userArea, userSearchKey;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait...");
        pd.setCanceledOnTouchOutside(false);

        final Calendar calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        mUserPhone = findViewById(R.id.userPhoneInput);
        mUserArea = findViewById(R.id.userAreaInput);
        mUserBlood = findViewById(R.id.userBloogGroupInput);
        mUserLastDonateDate = findViewById(R.id.userInfoAddLastDonate);
        mUserInfoSubmitButton = findViewById(R.id.userInfoSubmitButton);

        mUserLastDonateDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(DIALOG_ID);
            }
        });

        userAssignedLastDoanteDate = String.valueOf(day_x) + "/" + String.valueOf(month_x) + "/"  +String.valueOf(year_x);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserInfoDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mUserInfoDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("name")){
                    userName = dataSnapshot.child("name").getValue().toString();
                }
                if(dataSnapshot.hasChild("photoUri")){
                    userPhotoUri = dataSnapshot.child("photoUri").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUserInfoSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pd.show();

                userPhone = mUserPhone.getEditText().getText().toString();
                userArea = mUserArea.getEditText().getText().toString();
                userArea = userArea.toLowerCase();
                userBloodGroup = mUserBlood.getEditText().getText().toString();
                userBloodGroup = userBloodGroup.toLowerCase();


                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                userSearchKey = userArea+userBloodGroup;

                if(!userPhone.equals("") && !userArea.equals("") && !userBloodGroup.equals("")){

                    HashMap<String, String> map = new HashMap<>();
                    map.put("name", userName);
                    map.put("photoUri", userPhotoUri);
                    map.put("phone", userPhone);
                    map.put("area", userArea);
                    map.put("bloodGroup", userBloodGroup);
                    map.put("searchKey", userSearchKey);
                    map.put("lastDonate", userAssignedLastDoanteDate);
                    map.put("deviceToken", deviceToken);

                    mUserInfoDatabaseRef.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                Intent mainIntent = new Intent(UserInfoActivity.this, MainActivity.class);
                                startActivity(mainIntent);
                                finish();
                                pd.dismiss();



                            }

                        }
                    });

                }else{
                    pd.hide();
                    Toast.makeText(UserInfoActivity.this, "Please Check above field and Input All", Toast.LENGTH_LONG).show();
                }

            }
        });



    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id == DIALOG_ID)
            return new DatePickerDialog(this, datePickerListener, year_x, month_x, day_x);
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

            year_x = year;
            month_x = monthOfYear + 1;
            day_x = dayOfMonth;

            mUserLastDonateDate.setText("Last Donate: " + String.valueOf(day_x) + "/" + String.valueOf(month_x) + "/"  +String.valueOf(year_x));

        }
    };

}
