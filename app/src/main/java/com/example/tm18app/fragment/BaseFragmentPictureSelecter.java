package com.example.tm18app.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.tm18app.util.ConverterUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public abstract class BaseFragmentPictureSelecter extends Fragment{

    public interface FromBitmapToUriCallbackInterface {
        void uriResultCallback(Uri imageUri);
    }

    protected static final int PICK_IMAGE = 100;

    protected FromBitmapToUriCallbackInterface ic;

    protected void setIc(FromBitmapToUriCallbackInterface ic) {
        this.ic = ic;
    }

    public BaseFragmentPictureSelecter() {
    }

    protected void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    protected void applyImageUriToImageView(final Uri imageUri, ImageView imageView, final int width, final int height){
        Picasso.get().load(imageUri).resize(width, height).centerCrop().into(imageView);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = Picasso.get().load(imageUri).get();
                    ic.uriResultCallback(ConverterUtils.getImageUri(getContext(), bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
