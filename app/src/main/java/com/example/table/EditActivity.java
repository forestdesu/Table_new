package com.example.table;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.table.utils.MyConstans;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.net.URI;

public class EditActivity extends AppCompatActivity {
    private StorageReference mStorageRef;
    private ImageView imItem;
    private Uri uploadUri;
    private Spinner spinner;
    private DatabaseReference dRef;
    private FirebaseAuth mAuth;
    private EditText edTitle, edPrice, edPhone, edDisc;
    private boolean editState = false;
    private String temp_cat = "";
    private String temp_uid = "";
    private String temp_time = "";
    private String temp_key = "";
    private String temp_url = "";
    private boolean temp_image_update = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        init();
    }
    private void init(){
        edTitle = findViewById(R.id.edTitle);
        edPhone = findViewById(R.id.edPhone);
        edPrice = findViewById(R.id.edPrice);
        edDisc = findViewById(R.id.edDescription);
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        imItem = findViewById(R.id.imItem);
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        getMyIntent();
    }
    private void getMyIntent(){
        if (getIntent() != null) {
            Intent i = getIntent();
            editState = i.getBooleanExtra(MyConstans.editState, false);
            if (editState)setDataAds(i);
        }
    }
    private void setDataAds(Intent i) {
        Picasso.get().load(i.getStringExtra(MyConstans.imageId)).into(imItem);
        edTitle.setText(i.getStringExtra(MyConstans.title));
        edPrice.setText(i.getStringExtra(MyConstans.price));
        edPhone.setText(i.getStringExtra(MyConstans.phone));
        edDisc.setText(i.getStringExtra(MyConstans.disc));
        temp_cat = i.getStringExtra(MyConstans.cat);
        temp_uid = i.getStringExtra(MyConstans.uid);
        temp_time = i.getStringExtra(MyConstans.time);
        temp_key = i.getStringExtra(MyConstans.key);
        temp_url = i.getStringExtra(MyConstans.imageId);

    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null && data.getData() != null){
            if (resultCode == RESULT_OK) {
                imItem.setImageURI(data.getData());
                temp_image_update = true;
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
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                assert uploadUri != null;
                savePost();
                Toast.makeText(EditActivity.this, "Upload done", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private void uploadUpdateImage(){
        Bitmap bitMap = ((BitmapDrawable)imItem.getDrawable()).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitMap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] byteArray = out.toByteArray();
        final StorageReference mRef = FirebaseStorage.getInstance().getReferenceFromUrl(temp_url);
        UploadTask up = mRef.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                assert uploadUri != null;
                temp_url = uploadUri.toString();
                updatePost();
                Toast.makeText(EditActivity.this, "Upload done", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    public void onClickSavePost(View view){
        if (!editState) {
            uploadImage();
        }
        else {
            if (temp_image_update) {
                uploadUpdateImage();
            }
            else {
                updatePost();
            }
        }
        finish();
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
    private void updatePost(){
        dRef = FirebaseDatabase.getInstance().getReference(temp_cat);
        NewPost post = new NewPost();

        post.setImageId(temp_url);
        post.setTitle(edTitle.getText().toString());
        post.setPhone(edPhone.getText().toString());
        post.setPrice(edPrice.getText().toString());
        post.setDisc(edDisc.getText().toString());
        post.setKey(temp_key);
        post.setCat(temp_cat);
        post.setTime(temp_time);
        post.setUid(temp_uid);

        dRef.child(temp_key).child("anuncio").setValue(post);
    }
    private void savePost(){
        dRef = FirebaseDatabase.getInstance().getReference(spinner.getSelectedItem().toString());
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getUid() != null) {
            String key = dRef.push().getKey();
            NewPost post = new NewPost();

            post.setImageId(uploadUri.toString());
            post.setTitle(edTitle.getText().toString());
            post.setPhone(edPhone.getText().toString());
            post.setPrice(edPrice.getText().toString());
            post.setDisc(edDisc.getText().toString());
            post.setKey(key);
            post.setCat(spinner.getSelectedItem().toString());
            post.setTime(String.valueOf(System.nanoTime()));
            post.setUid(mAuth.getUid());

            if (key != null) dRef.child(key).child("anuncio").setValue(post);
        }
    }
}
