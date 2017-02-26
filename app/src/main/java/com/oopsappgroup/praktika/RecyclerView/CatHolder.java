package com.oopsappgroup.praktika.RecyclerView;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.oopsappgroup.praktika.Fragments.LazyCatsDataBaseFragment;
import com.oopsappgroup.praktika.Model.Cat;
import com.oopsappgroup.praktika.R;

import java.io.File;

public class CatHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public ImageView photo;
    public ImageButton menu;

    public CatHolder(View itemView) {
        super(itemView);

        name = (TextView)itemView.findViewById(R.id.tvCatName);
        photo = (ImageView)itemView.findViewById(R.id.ivCatPhoto);
        menu = (ImageButton)itemView.findViewById(R.id.ibAdditionMenu);
    }

    public void bindToCat(LazyCatsDataBaseFragment fragment, Cat cat, View.OnClickListener menuClickListener){
        name.setText(cat.name);

        File file = new File(cat.photo);
        Uri imageUri = Uri.fromFile(file);

        Glide.with(fragment)
                .load(imageUri)
                .centerCrop()
                .into(photo);

        menu.setOnClickListener(menuClickListener);
    }
}
