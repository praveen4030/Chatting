package com.kraigs.chattingapp.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.Query;
import com.kraigs.chattingapp.GetTimeAgo;
import com.kraigs.chattingapp.MainActivity;
import com.kraigs.chattingapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String messageRecieverID, messageSenderId;
    private TextView userName,lastSeen,typingTv;
    private CircleImageView userImage;

    private Toolbar chatToolbar;
    private ImageButton sendMessageButton, sendFilesButton, imagesBt;
    private EditText messageInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef, imageRootRef, notiRef, friendsRef, channelRef,userRef,chatRef;
    Query chatQuery;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myUrl = "";
    private Uri fileUri;
    private StorageTask uploadTask;
    private ProgressDialog loadingBar;
    RelativeLayout customchatToolbar;
    public static int FLAG_SEEN = 0;
    LinearLayout backLl;
    String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        messageRecieverID = getIntent().getStringExtra("visit_user_id");
        rootRef = FirebaseDatabase.getInstance().getReference();
        imageRootRef = FirebaseDatabase.getInstance().getReference();
        notiRef = FirebaseDatabase.getInstance().getReference().child("ChatNotify");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        channelRef = rootRef.child("ChatChannel");
        chatRef = rootRef.child("Message").child(messageSenderId).child(messageRecieverID);
        chatQuery = rootRef.child("Message").child(messageSenderId).child(messageRecieverID).orderByChild("time");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(messageSenderId);

        InitializeFields();

        updateUserStatus();

        rootRef.child("Users").child(messageRecieverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                     image = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.user_profile_image).into(userImage);
                }

                if (dataSnapshot.hasChild("name")) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    userName.setText(name);
                }

                if(dataSnapshot.hasChild("online")){
                    String onlineStatus = dataSnapshot.child("online").getValue().toString();
                    if (onlineStatus.equals("true")){
                        lastSeen.setText("Online");
                    } else{
                        if(dataSnapshot.hasChild("timestamp")){
                            long timestamp = (long)dataSnapshot.child("timestamp").getValue();
                            GetTimeAgo gta = new GetTimeAgo();
                            String time = gta.getTimeAgo(timestamp);
                            lastSeen.setText(time);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    String key = dataSnapshot.getKey();
                    String seen = messages.getSeen();

                    if (FLAG_SEEN == 1){
                        if (seen != null) {
                            if (seen.equals("false")) {
                                chatRef.child(key).child("seen").setValue("true");
                            }
                        } else{
                            chatRef.child(key).child("seen").setValue("true");
                        }
                    }

                    messagesList.add(messages);
                    messagesAdapter.notifyDataSetChanged();
                    userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        customchatToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, UserDetailsActivity.class);
                intent.putExtra("mentor_id", messageRecieverID);
                startActivity(intent);
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        imagesBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "image";
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ChatActivity.this);
            }
        });

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "pdf";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent.createChooser(intent, "Select Pdf File"), 438);
            }
        });

        messageInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    channelRef.child(messageSenderId).child(messageRecieverID).child("typing").setValue("true");
                } else {
                    channelRef.child(messageSenderId).child(messageRecieverID).child("typing").setValue("false");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        channelRef.child(messageRecieverID).child(messageSenderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("typing")){
                        String typing = dataSnapshot.child("typing").getValue().toString();
                        if(typing.equals("true")){
                            typingTv.setText("Typing...");
                            lastSeen.setVisibility(View.INVISIBLE);
                            typingTv.setVisibility(View.VISIBLE);

                        } else{
                            typingTv.setText("");
                            typingTv.setVisibility(View.INVISIBLE);
                            lastSeen.setVisibility(View.VISIBLE);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {

        String messageText = messageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(ChatActivity.this, "Send something...", Toast.LENGTH_SHORT).show();
        } else {

            String messageSenderRef = "Message/" + messageSenderId + "/" + messageRecieverID;
            String messageRecieverRef = "Message/" + messageRecieverID + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Message")
                    .child(messageSenderId).child(messageRecieverID).push();
            String messagePushId = userMessageKeyRef.getKey();

            HashMap<String,Object> messageTextBody = new HashMap<>();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);
            messageTextBody.put("to", messageRecieverID);
            messageTextBody.put("messageID", messagePushId);
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("seen", "false");
            messageTextBody.put("key", userMessageKeyRef.getKey());
            if (!TextUtils.isEmpty(image)){
                messageTextBody.put("image", image);
            }

            Map messageBodydetails = new HashMap();
            messageBodydetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodydetails.put(messageRecieverRef + "/" + messagePushId, messageTextBody);

            rootRef.updateChildren(messageBodydetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("type", "message");
                        map.put("from", messageSenderId);

                        notiRef.child(messageRecieverID).push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    channelRef.child(messageSenderId).child(messageRecieverID).child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                channelRef.child(messageRecieverID).child(messageSenderId).child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                        } else {
                                                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                } else {
                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();

                    }
                    messageInputText.setText("");
                }
            });
        }
    }

    private void InitializeFields() {

        chatToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        getSupportActionBar().setTitle("");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        backLl = findViewById(R.id.backLl);
        backLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        loadingBar = new ProgressDialog(this);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_IMAGE);
        lastSeen = findViewById(R.id.onlineStatus);
        typingTv = findViewById(R.id.typing);
        userName = (TextView) findViewById(R.id.custom_profile_name);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_btn);
        messageInputText = (EditText) findViewById(R.id.input_message);
        sendFilesButton = (ImageButton) findViewById(R.id.filesBt);
        imagesBt = (ImageButton) findViewById(R.id.imagesBt);

        customchatToolbar = (RelativeLayout) findViewById(R.id.customChatRl);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Sending");
                loadingBar.setMessage("Please wait,we are sending your image.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri uri = result.getUri();

                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Message/" + messageSenderId + "/" + messageRecieverID;
                final String messageRecieverRef = "Message/" + messageRecieverID + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Message")
                        .child(messageSenderId).child(messageRecieverID).push();
                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageRef.child(messagePushId + "." + "jpg");
                uploadTask = filePath.putFile(uri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            HashMap<String, Object> messageTextBody = new HashMap<>();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", uri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderId);
                            messageTextBody.put("to", messageRecieverID);
                            messageTextBody.put("messageID", messagePushId);
                            messageTextBody.put("time", ServerValue.TIMESTAMP);
                            messageTextBody.put("seen", "false");
                            messageTextBody.put("key", userMessageKeyRef.getKey());
                            if (!TextUtils.isEmpty(image)){
                                messageTextBody.put("image", image);
                            }


                            HashMap<String, Object> messageBodyDetails = new HashMap<>();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                            messageBodyDetails.put(messageRecieverRef + "/" + messagePushId, messageTextBody);

                            imageRootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("type", "image");
                                        map.put("from", messageSenderId);

                                        notiRef.child(messageRecieverID).push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    channelRef.child(messageSenderId).child(messageRecieverID).child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                channelRef.child(messageRecieverID).child(messageSenderId).child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            loadingBar.dismiss();
                                                                            Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                                                        } else {
                                                                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                            }

                                                        }
                                                    });

                                                } else {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();

                                    }

                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });
            }
        }

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            loadingBar.setTitle("Sending ic_file");
            loadingBar.setMessage("Please wait,we are sending your ic_file.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();

            if (!checker.equals("image")) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Doc Files");
                final String messageSenderRef = "Message/" + messageSenderId + "/" + messageRecieverID;
                final String messageRecieverRef = "Message/" + messageRecieverID + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Message")
                        .child(messageSenderId).child(messageRecieverID).push();
                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageRef.child(messagePushId + "." + checker);
                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    HashMap<String, Object> messageTextBody = new HashMap<>();

                                    messageTextBody.put("message", uri.toString());
                                    messageTextBody.put("name", fileUri.getLastPathSegment());
                                    messageTextBody.put("type", checker);
                                    messageTextBody.put("from", messageSenderId);
                                    messageTextBody.put("to", messageRecieverID);
                                    messageTextBody.put("messageID", messagePushId);
                                    messageTextBody.put("time", ServerValue.TIMESTAMP);
                                    messageTextBody.put("seen", "false");
                                    messageTextBody.put("key", userMessageKeyRef.getKey());
                                    if (!TextUtils.isEmpty(image)){
                                        messageTextBody.put("image", image);
                                    }

                                    HashMap<String, Object> messageBodydetails = new HashMap<>();
                                    messageBodydetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                                    messageBodydetails.put(messageRecieverRef + "/" + messagePushId, messageTextBody);
                                    rootRef.updateChildren(messageBodydetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("type", "document");
                                                map.put("from", messageSenderId);

                                                notiRef.child(messageRecieverID).push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            channelRef.child(messageSenderId).child(messageRecieverID).child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        channelRef.child(messageRecieverID).child(messageSenderId).child("timestamp").setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    loadingBar.dismiss();
                                                                                    Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                                                                } else {
                                                                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    } else {
                                                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                                    }

                                                                }
                                                            });
                                                        } else {
                                                            loadingBar.dismiss();
                                                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                loadingBar.dismiss();
                                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + "% Uploading...");
                    }
                });
            } else {

                loadingBar.dismiss();
                Toast.makeText(this, "Nothing selected, Error.", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onBackPressed() {
        channelRef.child(messageSenderId).child(messageRecieverID).child("typing").setValue("false");
        super.onBackPressed();
        finish();
        FLAG_SEEN = 0;
    }

    @Override
    protected void onStop() {
        channelRef.child(messageSenderId).child(messageRecieverID).child("typing").setValue("false");
        super.onStop();
        FLAG_SEEN = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        FLAG_SEEN = 1;
    }

    @Override
    protected void onPause() {
        channelRef.child(messageSenderId).child(messageRecieverID).child("typing").setValue("false");
        super.onPause();
        FLAG_SEEN = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FLAG_SEEN = 1;
    }

    private void updateUserStatus() {

        userRef.child("online").setValue("true");

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("timestamp", ServerValue.TIMESTAMP);
        onlineStateMap.put("online", "false");

        userRef.onDisconnect().updateChildren(onlineStateMap);

    }
}
