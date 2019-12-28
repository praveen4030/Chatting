package com.kraigs.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kraigs.chattingapp.Chat.ChatActivity;
import com.kraigs.chattingapp.Chat.ChatsFragment;
import com.kraigs.chattingapp.Chat.User;
import com.kraigs.chattingapp.Chat.UserDetailsActivity;
import com.kraigs.chattingapp.Login.LoginActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;
import com.tbuonomo.morphbottomnavigation.MorphBottomNavigationView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    DatabaseReference userRef, allUsersRef;
    String currentUserId;
    MorphBottomNavigationView bottomNavigation;
    ArrayList<User> list;
    RecyclerView searchRv;
    FrameLayout contentMainLl;
    LinearLayoutManager linearLayoutManager;
    MaterialSearchView searchView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        searchRv = findViewById(R.id.searchRv);
        linearLayoutManager = new LinearLayoutManager(this);
        searchRv.setLayoutManager(linearLayoutManager);
        contentMainLl = findViewById(R.id.fragment_container);
        searchView = findViewById(R.id.search_view);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        allUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        updateUserStatus();
        bottomNavigation = findViewById(R.id.bottomNavigationView);
        bottomNavigation.setSelectedItemId(R.id.action_home);
        loadFragment(new ChatsFragment());
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.action_request:
                        fragment = new RequestsFragment();
                        break;
                    case R.id.action_home:
                        fragment = new ChatsFragment();
                        break;
                    case R.id.action_user_profile:
                        fragment = new ProfileFragment();
                        break;
                }

                return loadFragment(fragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);

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

        SearchAdapter adapter = new SearchAdapter(myList);
        searchRv.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                break;
            case R.id.action_noti:
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        allUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    list = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        list.add(ds.getValue(User.class));
                    }

                    SearchAdapter adapter = new SearchAdapter(list);
                    searchRv.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                searchRv.setVisibility(View.VISIBLE);
                contentMainLl.setVisibility(View.GONE);
            }

            @Override
            public void onSearchViewClosed() {
                searchRv.setVisibility(View.GONE);
                contentMainLl.setVisibility(View.VISIBLE);
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                search(s);
                return true;
            }
        });

//                Query lastQuery = chatRef.child(userIDs).orderByKey().limitToLast(1);
//                lastQuery.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
////                        String message = dataSnapshot.child("message").getValue().toString();
////                        holder.userStatus.setText(message);
//
//                        for (DataSnapshot child: dataSnapshot.getChildren()) {
//                            String message = child.child("message").getValue().toString();
//                            String seen = child.child("seen").getValue().toString();
//
//                            if (seen.equals("false")){
//                                holder.userStatus.setTypeface(null, Typeface.BOLD);
//                                holder.userStatus.setTextColor(getResources().getColor(R.color.black));
//                            }
//
//                            holder.userStatus.setText(message);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        //Handle possible errors.
//                    }
//                });

//                friendsRef.child(userIDs).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if(dataSnapshot.exists()){
//                            String status = dataSnapshot.child("Friends").getValue().toString();
//                            if (status.equals("Saved")){
//                                holder.clientStatus.setVisibility(View.GONE);
//                            } else{
//                                holder.clientStatus.setVisibility(View.VISIBLE);
//                                holder.clientStatus.setText(status);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

//                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
//                        alertDialog.setTitle("Delete").setMessage("Do you want to delete this chat?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                notifyItemRemoved(position);
//
//                            }
//                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                            }
//                        }).show();
//
//                        return true;
//                    }
//                });

//                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()){
//                            if (dataSnapshot.hasChild("image")){
//                                profileImage[0] = dataSnapshot.child("image").getValue().toString();
//                                Picasso.get().load(profileImage[0])
//                                        .placeholder(R.drawable.user_profile_image)
//                                        .into(holder.profileImage);
//                            }
//
//                            if(dataSnapshot.hasChild("online")){
//                                String onlineStatus = dataSnapshot.child("online").getValue().toString();
//                                if (onlineStatus.equals("true")){
//                                    holder.onlineStatus.setVisibility(View.VISIBLE);
//                                } else{
//                                    holder.onlineStatus.setVisibility(View.GONE);
//                                }
//                            }
//
//                            final String profileName = dataSnapshot.child("name").getValue().toString();
//                            holder.userName.setText(profileName);

//                            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
//                                    intent.putExtra("visit_user_id",userIDs);
//                                    startActivity(intent);
//
////                                    FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("userState")
////                                            .setValue("online").addOnCompleteListener(new OnCompleteListener<Void>() {
////                                        @Override
////                                        public void onComplete(@NonNull Task<Void> task) {
////                                            if (task.isSuccessful()){
////                                                Intent intent = new Intent(getContext(),ChatActivity.class);
////                                                intent.putExtra("visit_user_id",userIDs);
////                                                startActivity(intent);
////                                            } else{
////                                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
////                                            }
////                                        }
////                                    });
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });

    }

    private void updateUserStatus() {

        userRef.child("online").setValue("true");

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("timestamp", ServerValue.TIMESTAMP);
        onlineStateMap.put("online", "false");

        userRef.onDisconnect().updateChildren(onlineStateMap);

    }

    class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

        ArrayList<User> list;

        public SearchAdapter(ArrayList<User> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
            return new SearchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            holder.onlineStatus.setVisibility(View.GONE);
            holder.userName.setText(list.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class SearchViewHolder extends RecyclerView.ViewHolder {
            TextView userName, userStatus, clientStatus;
            CircleImageView profileImage;
            ImageView onlineStatus;

            public SearchViewHolder(@NonNull View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.user_profile_name);
                userStatus = itemView.findViewById(R.id.user_status);
                profileImage = itemView.findViewById(R.id.users_profile_image);
                clientStatus = itemView.findViewById(R.id.clientStatus);
                onlineStatus = itemView.findViewById(R.id.onlineStatus);
            }
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {

        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
            return;
        } else if (doubleBackToExitPressedOnce) {

            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }

        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, "Press again to leave.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 3000);
    }

    private void logOut() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient;
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseAuth.getInstance().signOut();
                Intent i2 = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i2);
                finish();
            }
        });
    }

}