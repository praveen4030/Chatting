package com.kraigs.chattingapp.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kraigs.chattingapp.MainActivity;
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
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.miguelcatalan.materialsearchview.SearchAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    RecyclerView usersRv,friendsRv;
    String currentUserId;
    DatabaseReference rootRef,reqRef,friendsRef,notiRef,usersRef,allUsersRef;
    MaterialSearchView searchView;
    ArrayList<User> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        friendsRv= findViewById(R.id.friendsRv);
        usersRv= findViewById(R.id.usersRv);
        searchView = findViewById(R.id.searchView);
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

        FriendsRv();
        fetchUsers();

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });

    }

    private void fetchUsers() {
        allUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    list = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        list.add(ds.getValue(User.class));
                    }

                    UsersAdapter adapter = new UsersAdapter(list);
                    usersRv.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void FriendsRv() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.friends_menu,menu);
         MenuItem item = menu.findItem(R.id.action_search);
         searchView.setMenuItem(item);

         return true;
    }

    private void search(String s) {
        ArrayList<User> myList = new ArrayList<>();
        for (User object : list) {
            if (object.getName().toLowerCase().contains(s.toLowerCase())) {
                myList.add(object);
            }
        }

        UsersAdapter adapter = new UsersAdapter(myList);
        usersRv.setAdapter(adapter);

    }

    class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersListHolder>{
    ArrayList<User> list;

        UsersAdapter(ArrayList<User> myList){
            this.list = myList;
        }

        @NonNull
        @Override
        public UsersListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_users,parent,false);
            return  new UsersListHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull UsersListHolder holder, int position) {
            User model = list.get(position);
            Picasso.get().load(model.getImage()).placeholder(R.drawable.user_profile_image).fit().into(holder.userProfilePic);
            holder.nameTv.setText(model.getName());

            String recieverUserID = model.getKey();
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
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        private class UsersListHolder extends RecyclerView.ViewHolder {
            CircleImageView userProfilePic;
            TextView nameTv,connectTv;

            public UsersListHolder(@NonNull View itemView) {
                super(itemView);

                userProfilePic = itemView.findViewById(R.id.user_profile_pic);
                nameTv = itemView.findViewById(R.id.userNameTv);
                connectTv = itemView.findViewById(R.id.connectTv);

            }
        }
    }

    private class FriendsListHolder extends RecyclerView.ViewHolder {
        CircleImageView userProfilePic;
        TextView nameTv;

        public FriendsListHolder(@NonNull View itemView) {
            super(itemView);

            userProfilePic = itemView.findViewById(R.id.user_profile_pic);
            nameTv = itemView.findViewById(R.id.userNameTv);

        }
    }


}
