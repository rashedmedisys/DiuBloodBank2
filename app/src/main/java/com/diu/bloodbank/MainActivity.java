package com.diu.bloodbank;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TOOLBAR
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("DIU Blood Bank");

        //TABS
        mViewPager = findViewById(R.id.maintTabPager);
        mSectionsPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout = findViewById(R.id.mainTabs);
        mTabLayout.setupWithViewPager(mViewPager);


        //GETTING USER PHOTO AND NAME
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mUserDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            mUserDataRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild("phone")){

                        Intent userInfoActivity = new Intent(MainActivity.this, UserInfoActivity.class);
                        startActivity(userInfoActivity);
                        finish();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            sentToRegisterActivity();
        }
    }

    private void sentToRegisterActivity() {

        Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.mainLogoutButton){

            FirebaseAuth.getInstance().signOut();
            sentToRegisterActivity();

        }else if(item.getItemId() == R.id.mainMyProfile){

            startActivity(new Intent(MainActivity.this, ProfileActivity.class));

        }else if(item.getItemId() == R.id.mainSearchButton){
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
            View mView = getLayoutInflater().inflate(R.layout.search_layout, null);

            final EditText mSearchArea = mView.findViewById(R.id.searchArea);
            final EditText mSearchBloodGroup = mView.findViewById(R.id.searchBloodGroup);
            Button mSearchButton = mView.findViewById(R.id.searchButtonSearch);

            mSearchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String searchAreaKey = mSearchArea.getText().toString();
                    String searchGroupKey = mSearchBloodGroup.getText().toString();

                    String searchKey = searchAreaKey+searchGroupKey;
                    searchKey = searchKey.toLowerCase();

                    if(!searchAreaKey.equals("") && !mSearchBloodGroup.equals("")){

                        Intent searchResult = new Intent(MainActivity.this, SearchResultActivity.class);
                        searchResult.putExtra("searchKey", searchKey);
                        startActivity(searchResult);

                    }else {
                        Toast.makeText(MainActivity.this, "Please Input Area and Group!", Toast.LENGTH_LONG).show();
                    }

                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();

        }
        return true;
    }
}
