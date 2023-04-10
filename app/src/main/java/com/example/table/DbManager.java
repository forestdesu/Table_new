package com.example.table;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.table.adapter.DataSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private Context context;
    private Query mQuery;
    private List<NewPost> newPostsList;
    private DataSender dataSender;
    private FirebaseDatabase db;
    private FirebaseStorage fs;
    private int cat_ads_counter = 0;
    private String[] category_ads = {"Машины", "Компьютеры", "Смартфоны", "Бытовая техника"};

    public void deleteItem(final NewPost newPost)
    {
        StorageReference sRef = fs.getReferenceFromUrl(newPost.getImageId());
        sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                DatabaseReference dbRef = db.getReference(newPost.getCat());
                dbRef.child(newPost.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, R.string.item_not_deleted, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, R.string.item_not_deleted, Toast.LENGTH_SHORT).show();
            }
        });
    }
    public DbManager(DataSender dataSender, Context context) {
        this.dataSender = dataSender;
        this.context = context;
        newPostsList = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
        fs = FirebaseStorage.getInstance();
    }

    public void getDataFromDb(String path) {
        DatabaseReference dbRef = db.getReference(path);
        mQuery = dbRef.orderByChild("anuncio/time");
        readDataUpdate();
    }
    public void getMyAdsDataFromDb(String uid) {
        if (newPostsList.size() > 0) newPostsList.clear();
        DatabaseReference dbRef = db.getReference(category_ads[0]);
        mQuery = dbRef.orderByChild("anuncio/uid").equalTo(uid);
        readMyAdsDataUpdate(uid);
        cat_ads_counter++;
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
    public void readMyAdsDataUpdate(final String uid) {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    NewPost newPost = ds.child("anuncio").getValue(NewPost.class);
                    newPostsList.add(newPost);
                }
                if (cat_ads_counter > 3)
                {
                    dataSender.onDataRecived(newPostsList);
                    newPostsList.clear();
                    cat_ads_counter = 0;
                }
                else
                {
                    DatabaseReference dbRef = db.getReference(category_ads[cat_ads_counter]);
                    mQuery = dbRef.orderByChild("anuncio/uid").equalTo(uid);
                    readMyAdsDataUpdate(uid);
                    cat_ads_counter++;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
