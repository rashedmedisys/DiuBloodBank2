package com.diu.bloodbank;

import android.content.Context;
import android.content.Intent;
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


public class RequestFragment extends Fragment {

    private View mMainView;
    private RecyclerView mRequestDonorsList;
    private String userId;

    private DatabaseReference mRequestDonorsDatabase;

    public RequestFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMainView =  inflater.inflate(R.layout.fragment_request, container, false);

        mRequestDonorsList = mMainView.findViewById(R.id.requestRecyclerView);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRequestDonorsDatabase = FirebaseDatabase.getInstance().getReference().child("ConnectRequest").child(userId);

        mRequestDonorsList.setHasFixedSize(true);
        mRequestDonorsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Donors, RequestDonorsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Donors, RequestDonorsViewHolder>(

                Donors.class,
                R.layout.single_donor_layout,
                RequestDonorsViewHolder.class,
                mRequestDonorsDatabase

        ) {
            @Override
            protected void populateViewHolder(final RequestDonorsViewHolder viewHolder, Donors model, int position) {

                final String connectedDonorId = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent resultUserProfile = new Intent(getContext(), ResultUserProfileActivity.class);
                        resultUserProfile.putExtra("resultUserId", connectedDonorId);
                        startActivity(resultUserProfile);

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

                        if(dataSnapshot.hasChild("area")){
                            viewHolder.setLocation(dataSnapshot.child("area").getValue().toString());
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mRequestDonorsList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class RequestDonorsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public RequestDonorsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
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

        public void setLocation(String area) {

            TextView requestDonorArea = mView.findViewById(R.id.connectedDonorDate);
            requestDonorArea.setText(area);

        }
    }
}