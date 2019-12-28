package com.kraigs.chattingapp.Chat;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.kraigs.chattingapp.MainActivity;
import com.kraigs.chattingapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private DatabaseReference userRef, chatRef;
    private FirebaseAuth mAuth;
    private static final String TAG = "MessageAdapter";
    String currentUserId;

    public MessagesAdapter(List<Messages> userMessageList) {
        this.userMessageList = userMessageList;
    }

    public MessagesAdapter() {
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_message_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int i) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(i);
        String key = userMessageList.get(userMessageList.size() - 1).getKey();
        String lastMessageFrom = userMessageList.get(userMessageList.size() - 1).getTo();
        Log.d(TAG,lastMessageFrom + " " + key);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatRef = FirebaseDatabase.getInstance().getReference().child("Message").child(lastMessageFrom).child(currentUserId).child(key);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("MessageAdapter",dataSnapshot.child("message").getValue().toString());
                    String seen = dataSnapshot.child("seen").getValue().toString();
                    if (seen.equals("true")) {
                        holder.deliverTv.setText("Seen");
                    } else{
                        holder.deliverTv.setText("Delivered");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (userMessageList.size() - 1 == i) {
            if (fromUserId.equals(messageSenderId)) {
                if (fromMessageType.equals("text")) {
                    holder.deliverTv.setVisibility(View.VISIBLE);
                    holder.imageDeliver.setVisibility(View.GONE);

                } else if (fromMessageType.equals("image")) {
                    holder.deliverTv.setVisibility(View.GONE);
                    holder.imageDeliver.setVisibility(View.VISIBLE);
                }

            } else {

                holder.deliverTv.setVisibility(View.GONE);
                holder.imageDeliver.setVisibility(View.GONE);
            }
        } else {

            holder.deliverTv.setVisibility(View.GONE);
            holder.imageDeliver.setVisibility(View.GONE);
        }

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("image")) {
                        String recieverImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(recieverImage).into(holder.recieverProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.recieverMessageText.setVisibility(View.GONE);
        holder.recieverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageRecieverPicture.setVisibility(View.GONE);
        holder.recieverTimeTv.setVisibility(View.GONE);
        holder.senderTimeTv.setVisibility(View.GONE);

        holder.senderRl.setVisibility(View.GONE);
        holder.recieverRl.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {
            if (fromUserId.equals(messageSenderId)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderRl.setVisibility(View.VISIBLE);
                holder.senderTimeTv.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
//                holder.senderMessageText.setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());
                holder.senderMessageText.setText(messages.getMessage());
                holder.senderTimeTv.setText(messages.getTime());

            } else {
                holder.recieverMessageText.setVisibility(View.VISIBLE);
                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                holder.recieverRl.setVisibility(View.VISIBLE);
                holder.recieverTimeTv.setVisibility(View.VISIBLE);

                holder.recieverMessageText.setBackgroundResource(R.drawable.reciever_message_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
//                holder.recieverMessageText.setText(messages.getMessage() + "\n\n" + messages.getTime() + " - " + messages.getDate());
                holder.recieverMessageText.setText(messages.getMessage());
                holder.recieverTimeTv.setText(messages.getTime());
                holder.senderTimeTv.setVisibility(View.GONE);

            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserId.equals(messageSenderId)) {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);

            } else {
                holder.messageRecieverPicture.setVisibility(View.VISIBLE);
                holder.recieverProfileImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage())
                        .into(holder.messageRecieverPicture);
            }
        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {
            if (fromUserId.equals(messageSenderId)) {
                holder.recieverMessageText.setVisibility(View.GONE);
                holder.recieverProfileImage.setVisibility(View.GONE);
                holder.senderMessageText.setVisibility(View.GONE);
                holder.messageRecieverPicture.setVisibility(View.GONE);

                holder.messageSenderPicture.setVisibility(View.VISIBLE);
//                Picasso.get().load("gs://whatsapp-f3779.appspot.com/Image Files/ic_fileile.png").into(holder.messageSenderPicture);
                holder.messageSenderPicture.setBackgroundResource(R.drawable.ic_file);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(i).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });

            } else {
                holder.recieverMessageText.setVisibility(View.GONE);
                holder.senderMessageText.setVisibility(View.GONE);
                holder.messageSenderPicture.setVisibility(View.GONE);


                holder.messageRecieverPicture.setVisibility(View.VISIBLE);
                holder.recieverProfileImage.setVisibility(View.VISIBLE);

//                Picasso.get().load("gs://whatsapp-f3779.appspot.com/Image Files/ic_fileile.png").into(holder.messageRecieverPicture);

                holder.messageRecieverPicture.setBackgroundResource(R.drawable.ic_file);

            }
        }

        if (fromUserId.equals(messageSenderId)) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessageList.get(i).getType().equals("pdf") || userMessageList.get(i).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Download and View this Document",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    deleteSendMessage(i, holder);
                                } else if (position == 1) {

                                    int SDK_INT = Build.VERSION.SDK_INT;
                                    if (SDK_INT > 8) {
                                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                                                .permitAll().build();
                                        StrictMode.setThreadPolicy(policy);

                                        String url = userMessageList.get(i).getMessage();
                                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                        request.setDescription("Downloading...");
                                        request.setTitle("Downloads");
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                            request.allowScanningByMediaScanner();
                                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        }
                                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Documents");

                                        DownloadManager manager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                        manager.enqueue(request);
                                        Toast.makeText(holder.itemView.getContext(), "Downloading...", Toast.LENGTH_SHORT).show();

//                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse());
//                                        holder.itemView.getContext().startActivity(intent);

                                    }

                                } else if (position == 3) {
                                    deleteMessageForEveryone(i, holder);

                                }
                            }
                        });
                        builder.show();
                    } else if (userMessageList.get(i).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    deleteSendMessage(i, holder);
                                } else if (position == 2) {
                                    deleteMessageForEveryone(i, holder);

                                }
                            }
                        });
                        builder.show();
                    } else if (userMessageList.get(i).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "View This Image",
                                "Cancel",
                                "Delete for Everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    deleteSendMessage(i, holder);
                                } else if (position == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewrActivity.class);
                                    intent.putExtra("url", userMessageList.get(i).getMessage());
                                    holder.itemView.getContext().startActivity(intent);

                                } else if (position == 3) {
                                    deleteMessageForEveryone(i, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (userMessageList.get(i).getType().equals("pdf") || userMessageList.get(i).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Download and View this Document",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    deleteRecievedMessage(i, holder);

                                } else if (position == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(i).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    } else if (userMessageList.get(i).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    deleteRecievedMessage(i, holder);
                                }
                            }
                        });
                        builder.show();
                    } else if (userMessageList.get(i).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For Me",
                                "View This Image",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    deleteRecievedMessage(i, holder);

                                } else if (position == 1) {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewrActivity.class);
                                    intent.putExtra("url", userMessageList.get(i).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }

                }

            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    private void deleteSendMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                    holder.itemView.getContext().startActivity(intent);

                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured...", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void deleteRecievedMessage(final int position, final MessageViewHolder holder) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                    holder.itemView.getContext().startActivity(intent);
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder) {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Message")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    rootRef.child("Message").child(userMessageList.get(position).getFrom())
                            .child(userMessageList.get(position).getTo())
                            .child(userMessageList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                holder.itemView.getContext().startActivity(intent);
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderMessageText, recieverMessageText;
        private CircleImageView recieverProfileImage;
        private ImageView messageSenderPicture, messageRecieverPicture;
        public TextView deliverTv, imageDeliver, senderTimeTv, recieverTimeTv;
        RelativeLayout senderRl, recieverRl;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            recieverMessageText = (TextView) itemView.findViewById(R.id.reciever_message_text);
            recieverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture = (ImageView) itemView.findViewById(R.id.message_sender_image_view);
            messageRecieverPicture = (ImageView) itemView.findViewById(R.id.message_reciever_image_view);
            deliverTv = (TextView) itemView.findViewById(R.id.messageDeliverTv);
            imageDeliver = itemView.findViewById(R.id.messageImageDeliverTv);
            senderTimeTv = itemView.findViewById(R.id.sender_time_tv);
            recieverTimeTv = itemView.findViewById(R.id.reciever_time_tv);
            senderRl = itemView.findViewById(R.id.sender_message_rl);
            recieverRl = itemView.findViewById(R.id.reciever_message_rl);

        }

    }
}
















