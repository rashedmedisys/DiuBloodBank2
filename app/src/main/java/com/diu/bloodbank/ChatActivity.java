package com.diu.bloodbank;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser, mCurrentUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private EditText mChatMessageView;
    private ImageButton mChatSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mChatUser = getIntent().getStringExtra("resultUserId");
        mChatToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mChatMessageView = findViewById(R.id.chatWriteMessage);
        mChatSendButton = findViewById(R.id.chatSendMessageButton);

        mRootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mChatUser);
        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String chatUserName = dataSnapshot.child("name").getValue().toString();
                getSupportActionBar().setTitle(chatUserName);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }

            private void sendMessage() {

                String message = mChatMessageView.getText().toString();
                if(!message.equals("")){

                    String currentUserRef = "messages/" + mCurrentUser + "/" + mChatUser;
                    String chatUserRef = "messages/" + mChatUser + "/" + mCurrentUser;

                    DatabaseReference userMessagePush = mRootRef.child("messages").child(mCurrentUser).child(mChatUser).push();
                    String push_id = userMessagePush.getKey();

                    Map messageMap = new HashMap();
                    messageMap.put("message", message);
                    messageMap.put("seen", false);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(currentUserRef + "/" + push_id, messageMap);
                    messageUserMap.put(chatUserRef + "/" + push_id, messageMap);

                    mRootRef.updateChildren(messageMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }

                        }
                    });

                }

            }
        });

    }
}
