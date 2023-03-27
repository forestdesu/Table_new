package com.example.table;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.table.adapter.DataSender;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private Query mQuery;
    private List<NewPost> newPostsList;
    private DataSender dataSender;

    public DbManager(DataSender dataSender) {
        this.dataSender = dataSender;
        newPostsList = new ArrayList<>();
    }

    public void getDataFromDb(String path) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = db.getReference(path);
        mQuery = dbRef.orderByChild("anuncios/time");
        readDataUpdate();
    }
    public void readDataUpdate() {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (newPostsList.size() > 0) newPostsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    NewPost newPost = ds.child("anuncio").getValue(NewPost.class);
                    newPostsList.add(newPost);
                }

                dataSender.onDataRecived(newPostsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
