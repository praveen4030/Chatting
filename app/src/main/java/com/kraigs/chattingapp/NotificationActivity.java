package com.kraigs.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kraigs.chattingapp.Model.Notification;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationActivity extends AppCompatActivity {


    RecyclerView notiRv;
    LinearLayoutManager llm;
    DatabaseReference notiRef,userRef;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notiRef = FirebaseDatabase.getInstance().getReference().child("Notifications").child(currentUserID);
        notiRv = findViewById(R.id.notiRv);
        llm = new LinearLayoutManager(this);
        notiRv.setLayoutManager(llm);

        notiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    findViewById(R.id.noNoti).setVisibility(View.GONE);
                    notiRv.setVisibility(View.VISIBLE);
                } else{
                    findViewById(R.id.noNoti).setVisibility(View.VISIBLE);
                    notiRv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Notification> options = new FirebaseRecyclerOptions.Builder<Notification>()
                .setQuery(notiRef,Notification.class)
                .build();
        FirebaseRecyclerAdapter<Notification,NotiAdapter> adapter = new FirebaseRecyclerAdapter<Notification, NotiAdapter>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NotiAdapter notiAdapter, int i, @NonNull Notification model) {
                String fromUserId = model.getFrom();
                final String[] userName = {null};
                userRef.child(fromUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            if (dataSnapshot.hasChild("image")){
                                String image = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(image).fit().into(notiAdapter.userPic);
                            }

                            userName[0] = dataSnapshot.child("name").getValue().toString();
                            if(model.getType().equals("accept")){
                                notiAdapter.notiTv.setText(userName[0] + " has accepted your connect request.");
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                if (model.getType().equals("request")){
                    notiAdapter.notiTv.setText("You have a new connect request");
                }
            }

            @NonNull
            @Override
            public NotiAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.zunit_noti,parent,false);
                return new NotiAdapter(v);
            }
        };

        notiRv.setAdapter(adapter);
        adapter.startListening();

    }

    private class NotiAdapter extends RecyclerView.ViewHolder {
        CircleImageView userPic;
        TextView notiTv;

        public NotiAdapter(@NonNull View itemView) {
            super(itemView);

            userPic = itemView.findViewById(R.id.userPic);
            notiTv = itemView.findViewById(R.id.notiTv);

        }
    }
}
