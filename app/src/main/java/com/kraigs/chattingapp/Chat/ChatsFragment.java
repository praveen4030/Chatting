package com.kraigs.chattingapp.Chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kraigs.chattingapp.MainActivity;
import com.kraigs.chattingapp.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */

public class ChatsFragment extends Fragment {

    View v;
    private RecyclerView chatsList;
    private DatabaseReference chatRef,usersRef,friendsRef,channelRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    FloatingActionButton chatFb;
    LinearLayoutManager linearLayoutManager;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Message").child(currentUserId);
        channelRef = FirebaseDatabase.getInstance().getReference().child("ChatChannel").child(currentUserId);
        chatsList = (RecyclerView) v.findViewById(R.id.chats_list);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        chatsList.setLayoutManager(linearLayoutManager);
        chatFb = v.findViewById(R.id.chatFb);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        channelRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    v.findViewById(R.id.noChat).setVisibility(View.GONE);
                    chatsList.setVisibility(View.VISIBLE);

                } else{
                    v.findViewById(R.id.noChat).setVisibility(View.VISIBLE);
                    chatsList.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        chatFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendsActivity.class);
                startActivity(intent);
            }
        });
        return  v;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query channelQuery = FirebaseDatabase.getInstance().getReference().child("ChatChannel").child(currentUserId).orderByChild("timestamp");
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(channelQuery,User.class)
                .build();

        FirebaseRecyclerAdapter<User,ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<User, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull User model) {
                final String userIDs = getRef(position).getKey();
                final String profileImage[] = {"default_image"};
                holder.onlineStatus.setVisibility(View.GONE);

                Query lastQuery = chatRef.child(userIDs).orderByKey().limitToLast(1);
                lastQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        String message = dataSnapshot.child("message").getValue().toString();
//                        holder.userStatus.setText(message);

                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            String message = child.child("message").getValue().toString();
                            String seen = child.child("seen").getValue().toString();
                            String type = child.child("type").getValue().toString();
                            if (type.equals("image")){
                                holder.userStatus.setText("New Image");
                            } else  if (type.equals("pdf")){
                                holder.userStatus.setText("New File");
                            } else{
                                holder.userStatus.setText(message);
                            }

                            if (seen.equals("false")){
                                holder.userStatus.setTypeface(null, Typeface.BOLD);
                                holder.userStatus.setTextColor(Color.BLACK);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Handle possible errors.
                    }
                });

                friendsRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String status = dataSnapshot.child("Friends").getValue().toString();
                            if (status.equals("Saved")){
                                holder.clientStatus.setVisibility(View.GONE);
                            } else{
                                holder.clientStatus.setVisibility(View.VISIBLE);
                                holder.clientStatus.setText(status);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                        alertDialog.setTitle("Delete").setMessage("Do you want to delete this chat?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                notifyItemRemoved(position);

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();

                        return true;
                    }
                });

                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.hasChild("image")){
                                profileImage[0] = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(profileImage[0])
                                        .placeholder(R.drawable.user_profile_image)
                                        .into(holder.profileImage);
                            }

                            if(dataSnapshot.hasChild("online")){
                                String onlineStatus = dataSnapshot.child("online").getValue().toString();
                                if (onlineStatus.equals("true")){
                                    holder.onlineStatus.setVisibility(View.VISIBLE);
                                } else{
                                    holder.onlineStatus.setVisibility(View.GONE);
                                }
                            }

                            if(dataSnapshot.hasChild("name")){
                                final String profileName = dataSnapshot.child("name").getValue().toString();
                                holder.userName.setText(profileName);
                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("visit_user_id",userIDs);
                                    startActivity(intent);

//                                    FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("userState")
//                                            .setValue("online").addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()){
//                                                Intent intent = new Intent(getContext(),ChatActivity.class);
//                                                intent.putExtra("visit_user_id",userIDs);
//                                                startActivity(intent);
//                                            } else{
//                                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    });
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                return new ChatsViewHolder(view);
            }
        };

        chatsList.setAdapter(adapter);
        linearLayoutManager.smoothScrollToPosition(chatsList, null, 0);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                chatsList.smoothScrollToPosition(positionStart + 1);
                linearLayoutManager.setReverseLayout(true);
                linearLayoutManager.setStackFromEnd(true);
            }
        });

        adapter.startListening();
    }
    public class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName , userStatus,clientStatus;
        CircleImageView profileImage;
        ImageView onlineStatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            clientStatus = itemView.findViewById(R.id.clientStatus);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
        }
    }


}
