package com.oopsappgroup.praktika.Dialogs;

import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.oopsappgroup.praktika.Fragments.LazyCatsDataBaseFragment;
import com.oopsappgroup.praktika.R;

import java.io.File;

import static android.app.Activity.RESULT_OK;

public class NewLazyCatDialog extends DialogFragment implements View.OnClickListener {

    EditText name;
    ImageButton newPhoto;
    ImageView catPhoto;

    String catName;
    String photoUrl;
    int adapterPosition;

    RelativeLayout newPhotoLayout;

    LazyCatsDataBaseFragment fragment;

    public NewLazyCatDialog newInstance(LazyCatsDataBaseFragment context, String catName, String photoUrl, int adapterPosition){
        NewLazyCatDialog dialog = new NewLazyCatDialog();
        fragment = context;

        if (catName != null) {
            this.catName = catName;
            this.adapterPosition = adapterPosition;
            this.photoUrl = photoUrl;
        }

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCancelable(false);
        View v = inflater.inflate(R.layout.dialog_new_cat, null);

        name = (EditText)v.findViewById(R.id.etCatName);

        catPhoto = (ImageView)v.findViewById(R.id.ivDialogCatPhoto);

        newPhoto = (ImageButton)v.findViewById(R.id.ibAddCatPhoto);

        newPhotoLayout = (RelativeLayout)v.findViewById(R.id.rlAddNew);

        if (catName != null) {
            name.setText(catName);

            File file = new File(photoUrl);
            Uri imageUri = Uri.fromFile(file);
            Glide.with(getActivity())
                    .load(imageUri)
                    .centerCrop()
                    .into(catPhoto);

            catPhoto.setVisibility(View.VISIBLE);
            newPhotoLayout.setVisibility(View.INVISIBLE);
        }

        newPhoto.setOnClickListener(this);

        v.findViewById(R.id.btnNewCatOk).setOnClickListener(this);
        v.findViewById(R.id.btnNewCatCancel).setOnClickListener(this);

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                photoUrl = cursor.getString(columnIndex);
                cursor.close();

                Glide.with(getActivity())
                        .load(selectedImage)
                        .centerCrop()
                        .into(catPhoto);

                catPhoto.setVisibility(View.VISIBLE);
                newPhotoLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ibAddCatPhoto:
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, 1);
                break;
            case R.id.btnNewCatOk:
                if (photoUrl != null) {
                    String catName = getName();

                    if (catName.equals("")) catName = getString(R.string.no_cat_name);

                    if (this.catName != null && !this.catName.equals(catName))
                        fragment.updateItem(adapterPosition, catName, null);
                    else if (this.catName == null)
                        fragment.addItem(catName, photoUrl);

                    dismiss();
                }else
                    Toast.makeText(getActivity(), R.string.no_image_error, Toast.LENGTH_SHORT).show();

                break;
            case R.id.btnNewCatCancel:
                dismiss();
                break;
        }
    }

    @NonNull
    private String getName(){
        return name.getText().toString();
    }
}
