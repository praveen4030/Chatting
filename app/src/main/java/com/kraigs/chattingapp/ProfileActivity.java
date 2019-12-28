package com.kraigs.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kraigs.chattingapp.Chat.ChatActivity;
import com.kraigs.chattingapp.Chat.FriendsActivity;
import com.kraigs.chattingapp.Login.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    Button connectBt;
    DatabaseReference rootRef, friendsRef, reqRef, notiRef;
    String currentUserId, recieverUserID, currentState;
    CircleImageView userProfilePic;
    TextView friendsCountTv;
    TextView userNameTv, bioTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeFields();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recieverUserID = getIntent().getStringExtra("receiver_uid");

        rootRef = FirebaseDatabase.getInstance().getReference();
        friendsRef = rootRef.child("Friends");
        reqRef = rootRef.child("Friend Requests");
        notiRef = rootRef.child("Notifications").child("");

        friendsRef.keepSynced(true);
        reqRef.keepSynced(true);

        rootRef.child("Users").child(recieverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String image = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(image).into(userProfilePic);
                }

                String name = dataSnapshot.child("name").getValue().toString();
                userNameTv.setText(name);

                if (dataSnapshot.hasChild("bio")) {
                    String bio = dataSnapshot.child("bio").getValue().toString();
                    bioTv.setText(bio);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        counts();

        currentState = "new";
        manageChatRequest();
    }

    private void counts() {
        friendsCountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FriendsActivity.class);
                intent.putExtra("user_id", recieverUserID);
                startActivity(intent);
            }
        });

        friendsRef.child(recieverUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long childCount = dataSnapshot.getChildrenCount();
                    friendsCountTv.setText(String.valueOf(childCount)+ " Friends");
                } else {
                    friendsCountTv.setText("0 Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {
        reqRef.child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild(recieverUserID)) {
                                String requestType = dataSnapshot.child(recieverUserID).child("request_type").getValue().toString();
                                if (requestType.equals("sent")) {

                                    currentState = "request_sent";
                                    connectBt.setText("Request Sent");
                                    connectBt.setEnabled(true);

                                } else {
                                    currentState = "request_recieved";
                                    connectBt.setText("Confirm");
                                    connectBt.setEnabled(true);
                                }
                            } else {
                                friendsRef.child(currentUserId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    if (dataSnapshot.hasChild(recieverUserID)) {
                                                        currentState = "friends";
                                                        connectBt.setText("Friends");
                                                        connectBt.setEnabled(true);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                        } else {
                            friendsRef.child(currentUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                if (dataSnapshot.hasChild(recieverUserID)) {
                                                    currentState = "friends";
                                                    connectBt.setText("Message");
                                                    connectBt.setEnabled(true);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!currentUserId.equals(recieverUserID)) {

            connectBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentState.equals("new")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
                        alertDialog.setTitle("Send Request").setMessage("Do you really want to send a connect request?").setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendChatRequest();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }

                    if (currentState.equals("request_sent")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
                        alertDialog.setTitle("Delete Request").setMessage("Do you really want to delete a send request?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancelChatRequest();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }

                    if (currentState.equals("request_recieved")) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
                        alertDialog.setTitle("Accept Request").setMessage("Do you really want to accept a connect request?").setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AcceptChatRequest();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }

                    if (currentState.equals("friends")) {
                        Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
                        intent.putExtra("visit_user_id", recieverUserID);
                        startActivity(intent);
                    }
                }
            });
        } else {
            connectBt.setVisibility(View.GONE);

        }
    }

    private void AcceptChatRequest() {
        friendsRef.child(recieverUserID).child(currentUserId)
                .child("Friends").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendsRef.child(currentUserId).child(recieverUserID)
                                    .child("Friends").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                reqRef.child(recieverUserID).child(currentUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    reqRef.child(currentUserId).child(recieverUserID).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                                                        chatNotificationMap.put("from", currentUserId);
                                                                                        chatNotificationMap.put("type", "accept");
                                                                                        notiRef.child(recieverUserID).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    connectBt.setEnabled(true);
                                                                                                    currentState = "friends";
                                                                                                    connectBt.setText("Message");
                                                                                                } else {
                                                                                                    Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });

                                                                                    } else {
                                                                                        Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                } else {
                                                                    Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void cancelChatRequest() {
        reqRef.child(currentUserId).child(recieverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reqRef.child(recieverUserID).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                connectBt.setEnabled(true);
                                                currentState = "new";
                                                connectBt.setText("Connect");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        reqRef.child(currentUserId).child(recieverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reqRef.child(recieverUserID).child(currentUserId).child("request_type").setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", currentUserId);
                                                chatNotificationMap.put("type", "request");
                                                notiRef.child(recieverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    connectBt.setEnabled(true);
                                                                    currentState = "request_sent";
                                                                    connectBt.setText("Cancel Request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void initializeFields() {

        connectBt = findViewById(R.id.connectBt);
        friendsCountTv = findViewById(R.id.friendsCountTv);
        userNameTv = findViewById(R.id.userNameTv);
        userProfilePic = findViewById(R.id.user_profile_pic);

    }
}
