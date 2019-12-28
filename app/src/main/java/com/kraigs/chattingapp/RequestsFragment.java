package com.kraigs.chattingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.kraigs.chattingapp.Chat.ChatActivity;
import com.kraigs.chattingapp.Chat.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */

public class RequestsFragment extends Fragment {

    View v;
    RecyclerView requestListRv,onlineRv;
    DatabaseReference requestsRef, usersRef,friendsRef;
    String currentUserId;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_requests, container, false);

        requestsRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        requestListRv = (RecyclerView) v.findViewById(R.id.requestRv);
        onlineRv = v.findViewById(R.id.onlineRv);
        onlineRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        requestListRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        invitationRecyclerView();
        onlineRv();

        return v;
    }

    private void onlineRv() {
        Query query = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();
        FirebaseRecyclerAdapter<User,OnlineHolder> adapter = new FirebaseRecyclerAdapter<User, OnlineHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OnlineHolder onlineHolder, int i, @NonNull User user) {
                String userID = getRef(i).getKey();

                usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String name = dataSnapshot.child("name").getValue().toString();
                            String online = dataSnapshot.child("online").getValue().toString();
                            if (dataSnapshot.hasChild("image")){
                                String image = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(image).placeholder(R.drawable.user_profile_image).into(onlineHolder.userProfile);
                            }

                            onlineHolder.userNameTv.setText(name);
                            if (online.equals("false")){
                                onlineHolder.itemView.setVisibility(View.GONE);
                                ViewGroup.LayoutParams params = onlineHolder.itemView.getLayoutParams();
                                params.height=0;
                                params.width= 0;
                                onlineHolder.itemView.setLayoutParams(params);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                onlineHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra("visit_user_id",userID);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public OnlineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_online,parent,false);
                return  new OnlineHolder(v);
            }
        };

        onlineRv.setAdapter(adapter);
        adapter.startListening();
    }

    private void invitationRecyclerView() {

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(requestsRef.child(currentUserId), User.class)
                .build();

        FirebaseRecyclerAdapter<User, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<User, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull User model) {
                final String list_user_id = getRef(position).getKey();
                String requestType = model.getRequest_type();

                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.cancelButton.setVisibility(View.VISIBLE);

                if (requestType.equals("sent")){
                    holder.itemView.setVisibility(View.GONE);
                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                    params.height=0;
                    params.width= 0;
                    holder.itemView.setLayoutParams(params);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        intent.putExtra("mentor_id", list_user_id);
                        startActivity(intent);
                    }
                });

                holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        friendsRef.child(currentUserId).child(list_user_id).child("Friends")
                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    friendsRef.child(list_user_id).child(currentUserId).child("Friends")
                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            requestsRef.child(currentUserId).child(list_user_id).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                requestsRef.child(list_user_id).child(currentUserId).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    Toast.makeText(getActivity(), "New Friend added", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                                }
                            }
                        });
                    }
                });

                holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        requestsRef.child(currentUserId).child(list_user_id).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            requestsRef.child(list_user_id).child(currentUserId).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getActivity(), "Request Deleted Successfully", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });

                    }
                });

                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                                final String requestImage = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(requestImage).into(holder.profileImage);
                            }

                            final String requestName = dataSnapshot.child("name").getValue().toString();
                            holder.userName.setText(requestName);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.zunit_requests, viewGroup, false);
                return new RequestViewHolder(view);
            }
        };

        requestListRv.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        CircleImageView profileImage;
        ImageView acceptButton, cancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_btn);
            cancelButton = itemView.findViewById(R.id.request_cancel_btn);

        }
    }

    private class OnlineHolder extends RecyclerView.ViewHolder {
        CircleImageView userProfile;
        TextView userNameTv;
        ImageView onlineView;
        public OnlineHolder(@NonNull View itemView) {
            super(itemView);

            userProfile = itemView.findViewById(R.id.user_profile_pic);
            userNameTv = itemView.findViewById(R.id.userNameTv);
            onlineView = itemView.findViewById(R.id.onlineView);
        }
    }
}
