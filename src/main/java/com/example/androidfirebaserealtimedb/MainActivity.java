package com.example.androidfirebaserealtimedb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    EditText editTitle;
    EditText editContent;
    Button postBtn;
    Button editBtn;
    Button deleteBtn;
    RecyclerView recyclerView;

    FirebaseDatabase database;
    DatabaseReference databaseReference;
    FirebaseRecyclerOptions<Post> options;
    FirebaseRecyclerAdapter<Post, MyRecyclerViewHolder> adapter;

    Post selectedPost;
    String selectedKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Edit Texts, Button, and RecyclerView
        editTitle = (EditText) findViewById(R.id.editTitle);
        editContent = (EditText) findViewById(R.id.editContent);
        postBtn = (Button) findViewById(R.id.postBtn);
        editBtn = (Button) findViewById(R.id.editBtn);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Initialize Firebase
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("MY_FIREBASE");
        //display new posts when added
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //notify to update posts display
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference
                        .child(selectedKey)
                        .setValue(new Post(editTitle.getText().toString(),
                                editContent.getText().toString()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this,
                                        "Edit Success",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,
                                ""+e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference
                        .child(selectedKey)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this,
                                        "Post Deleted",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,
                                ""+e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        displayComment();
    }

    @Override
    protected void onStop(){
        //stop recyclerview adapter from listening on stop (if not null)
        if(adapter != null)
            adapter.stopListening();
        super.onStop();
    }


    private void postComment() {
        String title = editTitle.getText().toString();
        String content = editContent.getText().toString();

        Post post = new Post(title,content);

        //create unique id of comment
        databaseReference.push().setValue(post);

        adapter.notifyDataSetChanged();
    }

    private void displayComment() {
        //create firebase recycler options
        options = new FirebaseRecyclerOptions.Builder<Post>().
                        setQuery(databaseReference,Post.class).
                        build();
        //create firebase recycler adapter
        adapter = new FirebaseRecyclerAdapter<Post, MyRecyclerViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MyRecyclerViewHolder holder, int position, @NonNull final Post model) {
                        //Set title and content for post item
                        holder.titleText.setText(model.getTitle());
                        holder.contentText.setText(model.getContent());

                        holder.setItemClickListener(new ItemClickListener() {
                            @Override
                            public void onClick(View view, int position) {
                                selectedPost = model;
                                selectedKey = getSnapshots().getSnapshot(position).getKey();
                                Log.d("Key Item",""+selectedKey);

                                //bind edited post data
                                editContent.setText(model.getContent());
                                editTitle.setText(model.getTitle());
                            }
                        });
                    }
                    @NonNull
                    @Override
                    public MyRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        //inflate and return post item in viewholder
                        View itemView =
                                LayoutInflater.from(getBaseContext()).
                                        inflate(R.layout.post_item,parent,false);
                        return new MyRecyclerViewHolder(itemView);
                    }
                };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
}
