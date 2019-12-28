package com.kraigs.chattingapp.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kraigs.chattingapp.ProfileActivity;
import com.kraigs.chattingapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    RecyclerView usersRv;
    String currentUserId;
    DatabaseReference rootRef,reqRef,friendsRef,notiRef,usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        RecyclerView friendsRv= findViewById(R.id.friendsRv);
        usersRv= findViewById(R.id.usersRv);
        friendsRv.setLayoutManager(new LinearLayoutManager(this));
        usersRv.setLayoutManager(new LinearLayoutManager(this));
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        friendsRef = rootRef.child("Friends");
        reqRef = rootRef.child("Friend Requests");
        notiRef = rootRef.child("Notifications");

        friendsRef.keepSynced(true);
        reqRef.keepSynced(true);
        usersRef = rootRef.child("Users").child(currentUserId);

        UsersRv();

        Query query = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();
        FirebaseRecyclerAdapter<User, FriendsListHolder> adapter = new FirebaseRecyclerAdapter<User,FriendsListHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsListHolder holder, int i, @NonNull User model) {
                String userUid = getRef(i).getKey();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FriendsActivity.this, ChatActivity.class);
                        intent.putExtra("visit_user_id",userUid);
                        startActivity(intent);
                    }
                });

                userRef.child(userUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.hasChild("image")){
                                String image = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(image).placeholder(R.drawable.user_profile_image).fit().into(holder.userProfilePic);
                            }

                            if (dataSnapshot.hasChild("name")){
                                String name = dataSnapshot.child("name").getValue().toString();
                                holder.nameTv.setText(name);
                            }

                            if (dataSnapshot.hasChild("userName")){
                                String userName = dataSnapshot.child("userName").getValue().toString();
                                holder.userIDTv.setText(userName);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_friends,parent,false);
                return  new FriendsListHolder(v);
            }
        };

        friendsRv.setAdapter(adapter);
        adapter.startListening();
    }

    private void UsersRv(){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(userRef,User.class)
                .build();
        FirebaseRecyclerAdapter<User, UserListHolder> adapter = new FirebaseRecyclerAdapter<User,UserListHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserListHolder holder, int i, @NonNull User model) {
                Picasso.get().load(model.getImage()).placeholder(R.drawable.user_profile_image).fit().into(holder.userProfilePic);
                holder.nameTv.setText(model.getName());

                String recieverUserID = getRef(i).getKey();
                final String[] currentState = {"new"};
                if (!recieverUserID.equals(currentUserId)){
                    holder.connectTv.setVisibility(View.VISIBLE);
                } else{
                    holder.connectTv.setVisibility(View.GONE);
                }

                reqRef.child(currentUserId)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild(recieverUserID)) {
                                        String requestType = dataSnapshot.child(recieverUserID).child("request_type").getValue().toString();
                                        if (requestType.equals("sent")) {
                                            currentState[0] = "request_sent";
                                            holder.connectTv.setText("Request Sent");
                                            holder.connectTv.setEnabled(true);

                                        } else {
                                            currentState[0] = "request_recieved";
                                            holder.connectTv.setText("Confirm");
                                            holder.connectTv.setEnabled(true);
                                        }
                                    } else {
                                        friendsRef.child(currentUserId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            if (dataSnapshot.hasChild(recieverUserID)) {
                                                                currentState[0] = "friends";
                                                                holder.connectTv.setText("Friends");
                                                                holder.connectTv.setEnabled(true);
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
                                                            currentState[0] = "friends";
                                                            holder.connectTv.setText("Message");
                                                            holder.connectTv.setEnabled(true);
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

                holder.connectTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentState[0].equals("new")) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FriendsActivity.this);
                            alertDialog.setTitle("Send Request").setMessage("Do you really want to send a connect request?").setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
                                                                                                holder.connectTv.setEnabled(true);
                                                                                                currentState[0] = "request_sent";
                                                                                                holder.connectTv.setText("Cancel Request");
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
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }

                        if (currentState[0].equals("request_sent")) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FriendsActivity.this);
                            alertDialog.setTitle("Delete Request").setMessage("Do you really want to delete a send request?").setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
                                                                            holder.connectTv.setEnabled(true);
                                                                            currentState[0] = "new";
                                                                            holder.connectTv.setText("Connect");
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }

                        if (currentState[0].equals("request_recieved")) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(FriendsActivity.this);
                            alertDialog.setTitle("Accept Request").setMessage("Do you really want to accept a connect request?").setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
                                                                                                                                holder.connectTv.setEnabled(true);
                                                                                                                                currentState[0] = "friends";
                                                                                                                                holder.connectTv.setText("Message");
                                                                                                                            } else {
                                                                                                                                Toast.makeText(FriendsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                                                            }
                                                                                                                        }
                                                                                                                    });

                                                                                                                } else {
                                                                                                                    Toast.makeText(FriendsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                            } else {
                                                                                                Toast.makeText(FriendsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        } else {
                                                                            Toast.makeText(FriendsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                    } else {
                                                        Toast.makeText(FriendsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }

                        if (currentState[0].equals("friends")) {
                            Intent intent = new Intent(FriendsActivity.this, ChatActivity.class);
                            intent.putExtra("visit_user_id", recieverUserID);
                            startActivity(intent);
                        }
                    }
                });


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FriendsActivity.this, ProfileActivity.class);
                        intent.putExtra("receiver_uid",recieverUserID);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public UserListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_users,parent,false);
                return  new UserListHolder(v);
            }
        };

        usersRv.setAdapter(adapter);
        adapter.startListening();
    }

    private class UserListHolder extends RecyclerView.ViewHolder {
        CircleImageView userProfilePic;
        TextView nameTv,connectTv;

        public UserListHolder(@NonNull View itemView) {
            super(itemView);

            userProfilePic = itemView.findViewById(R.id.user_profile_pic);
            nameTv = itemView.findViewById(R.id.userNameTv);
            connectTv = itemView.findViewById(R.id.connectTv);

        }
    }

    private class FriendsListHolder extends RecyclerView.ViewHolder {
        CircleImageView userProfilePic;
        TextView nameTv,userIDTv;

        public FriendsListHolder(@NonNull View itemView) {
            super(itemView);

            userProfilePic = itemView.findViewById(R.id.user_profile_pic);
            nameTv = itemView.findViewById(R.id.userNameTv);
            userIDTv = itemView.findViewById(R.id.userIDTv);

        }
    }
}
