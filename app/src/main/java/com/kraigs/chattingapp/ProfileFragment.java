package com.kraigs.chattingapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kraigs.chattingapp.Chat.ChatActivity;
import com.kraigs.chattingapp.Chat.FriendsActivity;
import com.kraigs.chattingapp.Login.LoginActivity;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    View v;
    Button connectBt;
    DatabaseReference rootRef, friendsRef,usersRef;
    String currentUserId;
    CircleImageView userProfilePic;
    TextView friendsCountTv;
    TextView userNameTv, bioTv;
    @BindView(R.id.friendsRl)
    RelativeLayout friendsRl;
    @BindView(R.id.addFriendsRl)
    RelativeLayout addFriendsRl;
    @BindView(R.id.shareRl)
    RelativeLayout shareRl;
    @BindView(R.id.rateRl)
    RelativeLayout rateRl;
    @BindView(R.id.logOutRl)
    RelativeLayout logOutRl;
    @BindView(R.id.userIdTv)
    TextView userIdTv;
    ProgressDialog loadingBar;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, v);
        initializeFields();
        loadingBar = new ProgressDialog(getActivity());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("Users").child(currentUserId);
        friendsRef = rootRef.child("Friends");

        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(userProfilePic);
                    }

                    if (dataSnapshot.hasChild("userID")) {
                        String userID = dataSnapshot.child("userID").getValue().toString();
                        userIdTv.setText(userID);
                    }

                    if (dataSnapshot.hasChild("name")) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        userNameTv.setText(name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(getContext(),ProfileFragment.this);
            }
        });

        counts();

        return v;
    }

    private void counts() {
        friendsCountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendsActivity.class);
                intent.putExtra("user_id", currentUserId);
                startActivity(intent);
            }
        });

        friendsRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long childCount = dataSnapshot.getChildrenCount();
                    friendsCountTv.setText(String.valueOf(childCount) + " Friends");
                } else {
                    friendsCountTv.setText("0 Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void logOut() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Logout").setMessage("Do you want to logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient;
                mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut();
                        Intent i2 = new Intent(getActivity(), LoginActivity.class);
                        startActivity(i2);
                        getActivity().finish();
                    }
                });
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();

    }


    private void initializeFields() {

        friendsCountTv = v.findViewById(R.id.friendsCountTv);
        userNameTv = v.findViewById(R.id.userNameTv);
        userProfilePic = v.findViewById(R.id.user_profile_pic);

    }

    @OnClick({R.id.user_profile_pic, R.id.friendsRl, R.id.addFriendsRl, R.id.shareRl, R.id.rateRl, R.id.logOutRl})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.user_profile_pic:

                break;
            case R.id.friendsRl:
            case R.id.addFriendsRl:
                Intent intent2 = new Intent(getActivity(), FriendsActivity.class);
                startActivity(intent2);
                break;
            case R.id.shareRl:
                break;
            case R.id.rateRl:
                break;
            case R.id.logOutRl:
                logOut();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Profile Pic");
                loadingBar.setMessage("Please wait,we are updating your profile.");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri uri = result.getUri();

                StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Profile Images").child(currentUserId + ".jpg");
                filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    usersRef.child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                loadingBar.dismiss();
                                                Toast.makeText(getActivity(), "Profile pic updated successfully!", Toast.LENGTH_SHORT).show();
                                            } else{
                                                Toast.makeText(getActivity(), "Please try again", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        }
    }

}
