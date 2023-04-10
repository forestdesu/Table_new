package com.example.table.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.table.DbManager;
import com.example.table.EditActivity;
import com.example.table.MainActivity;
import com.example.table.NewPost;
import com.example.table.R;
import com.example.table.utils.MyConstans;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> {
    private List<NewPost> arrayPost;
    private Context context;
    private OnItemClickCustom onItemClickCustom;
    private DbManager dbManager;

    public PostAdapter(List<NewPost> arrayPost, Context context, OnItemClickCustom onItemClickCustom) {
        this.arrayPost = arrayPost;
        this.context = context;
        this.onItemClickCustom = onItemClickCustom;
    }

    @NonNull
    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ads, parent, false);
        return new ViewHolderData(view, onItemClickCustom);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {
        holder.setData(arrayPost.get(position));
    }

    @Override
    public int getItemCount() {
        return arrayPost.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView tvPriceTel, tvDisc, tvTitle;
        private ImageView imAds;
        private LinearLayout edit_layout;
        private ImageButton deleteButton;
        private ImageButton editButton;
        private OnItemClickCustom onItemClickCustom;


        public ViewHolderData(@NonNull View itemView, OnItemClickCustom onItemClickCustom) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPriceTel = itemView.findViewById(R.id.tvPriceTel);
            tvDisc = itemView.findViewById(R.id.tvDisc);
            imAds = itemView.findViewById(R.id.imAds);
            edit_layout = itemView.findViewById(R.id.edit_layout);
            deleteButton = itemView.findViewById(R.id.emDeleteItem);
            editButton = itemView.findViewById(R.id.emEditItem);
            itemView.setOnClickListener(this);
            this.onItemClickCustom = onItemClickCustom;
        }
        public void setData(NewPost newPost){
            if (newPost.getUid().equals(MainActivity.MAUTH))
            {
                edit_layout.setVisibility(View.VISIBLE);
            }
            else {
                edit_layout.setVisibility(View.GONE);
            }
            Picasso.get().load(newPost.getImageId()).into(imAds);
            tvTitle.setText(newPost.getTitle());
            String price_tel = "Цена: " + newPost.getPrice() + "\nТелефон: " + newPost.getPhone();
            tvPriceTel.setText(price_tel);
            String textDisc = newPost.getDisc();
            if (textDisc.length() > 50) textDisc = textDisc.substring(0, 50) + "...";
            tvDisc.setText(textDisc);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteDialog(newPost, getAdapterPosition());
                }
            });
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, EditActivity.class);
                    i.putExtra(MyConstans.imageId, newPost.getImageId());
                    i.putExtra(MyConstans.title, newPost.getTitle());
                    i.putExtra(MyConstans.price, newPost.getPrice());
                    i.putExtra(MyConstans.phone, newPost.getPhone());
                    i.putExtra(MyConstans.disc, newPost.getDisc());
                    i.putExtra(MyConstans.key, newPost.getKey());
                    i.putExtra(MyConstans.uid, newPost.getUid());
                    i.putExtra(MyConstans.time, newPost.getTime());
                    i.putExtra(MyConstans.cat, newPost.getCat());
                    i.putExtra(MyConstans.editState, true);
                    context.startActivity(i);
                }
            });
        }

        @Override
        public void onClick(View v) {
            onItemClickCustom.onItemSelected(getAdapterPosition());
        }
    }
    private void deleteDialog(final NewPost newPost, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_title);
        builder.setMessage(R.string.delete_message);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbManager.deleteItem(newPost);
                arrayPost.remove(position);
                notifyItemRemoved(position);
            }
        });
        builder.show();
    }
    public interface OnItemClickCustom {
        void onItemSelected(int position);
    }
    public void updateAdapter(List<NewPost> listData) {
        arrayPost.clear();
        arrayPost.addAll(listData);
        notifyDataSetChanged();
    }
    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }
}
