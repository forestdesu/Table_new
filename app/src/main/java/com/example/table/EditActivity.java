package com.example.table;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URI;

public class EditActivity extends AppCompatActivity {
    private StorageReference mStorageRef;
    private ImageView imItem;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        init();
    }
    private void init(){
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        imItem = findViewById(R.id.imItem);
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null && data.getData() != null){
            if (resultCode == RESULT_OK) {
                imItem.setImageURI(data.getData());
            }
        }
    }
    private void uploadImage(){
        Bitmap bitMap = ((BitmapDrawable)imItem.getDrawable()).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitMap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] byteArray = out.toByteArray();
        final StorageReference mRef = mStorageRef.child(System.currentTimeMillis() + "_image");
        UploadTask up = mRef.putBytes(byteArray);
        Task<URI> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<URI>>() {
            @Override
            public Task<URI> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return null;
            }
        })
    }
    public void onClickImage(View view){
        getImage();
    }
    private void getImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 10);
    }
}
