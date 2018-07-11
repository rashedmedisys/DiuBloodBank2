package com.diu.bloodbank;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class DonorsFragment extends Fragment {

    private View mMainView;
    private RecyclerView mDonorsList;
    private String userId;

    private DatabaseReference mDonorsDatabase;

    public DonorsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMainView =  inflater.inflate(R.layout.fragment_donors, container, false);

        mDonorsList = mMainView.findViewById(R.id.donorsRecyclerView);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDonorsDatabase = FirebaseDatabase.getInstance().getReference().child("Connected").child(userId);

        mDonorsList.setHasFixedSize(true);
        mDonorsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Donors, DonorsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Donors, DonorsViewHolder>(

                Donors.class,
                R.layout.single_donor_layout,
                DonorsViewHolder.class,
                mDonorsDatabase

        ) {
            @Override
            protected void populateViewHolder(final DonorsViewHolder viewHolder, final Donors model, int position) {

                viewHolder.setConnectedDate(model.getDate());
                final String connectedDonorId = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //CLICK EVENT LISTENER
                                if(i == 0){
                                    Intent resultUserProfile = new Intent(getContext(), ResultUserProfileActivity.class);
                                    resultUserProfile.putExtra("resultUserId", connectedDonorId);
                                    startActivity(resultUserProfile);
                                }

                                if(i == 1){

                                    Intent chat = new Intent(getContext(), ChatActivity.class);
                                    chat.putExtra("resultUserId", connectedDonorId);
                                    startActivity(chat);

                                }

                            }
                        });

                        builder.show();

                    }
                });

                DatabaseReference mConnectedDonorDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(connectedDonorId);
                mConnectedDonorDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild("photoUri")){
                            viewHolder.setImage(dataSnapshot.child("photoUri").getValue().toString(), getContext());
                        }

                        if(dataSnapshot.hasChild("bloodGroup")){
                            viewHolder.setGroup(dataSnapshot.child("bloodGroup").getValue().toString());
                        }

                        if(dataSnapshot.hasChild("name")){
                            viewHolder.setName(dataSnapshot.child("name").getValue().toString());
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mDonorsList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class DonorsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public DonorsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setConnectedDate(String date) {

            TextView connectedDate = mView.findViewById(R.id.connectedDonorDate);
            connectedDate.setText("Connected Since " + date);

        }

        public void setImage(String photoUri, Context context) {

            CircleImageView connectedDonorImage = mView.findViewById(R.id.connectedDonorImage);
            Picasso.with(context).load(photoUri).placeholder(R.drawable.avatar).into(connectedDonorImage);


        }

        public void setGroup(String bloodGroup) {

            TextView connectedDonorGroup = mView.findViewById(R.id.connectedDonorGroup);
            connectedDonorGroup.setText(bloodGroup);

        }

        public void setName(String name) {

            TextView connectedDonorName = mView.findViewById(R.id.connectedDonorName);
            connectedDonorName.setText(name);

        }
    }

}
