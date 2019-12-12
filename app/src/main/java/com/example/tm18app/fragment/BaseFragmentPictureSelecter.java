package com.example.tm18app.fragment;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} abstract subclass. This class is responsible for image selection
 * functions
 *
 * @author Sebastian Ampuero
 * @version  1.0
 * @since 03.12.2019
 */
public abstract class BaseFragmentPictureSelecter extends Fragment{

    protected static final int PICK_IMAGE = 100;
    public BaseFragmentPictureSelecter() {
    }

    /**
     * Opens an {@link Intent} to select an image from the gallery
     */
    protected void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    /**
     * Applies a selected {@link Uri} to a {@link ImageView}
     * @param imageUri {@link Uri}
     * @param imageView {@link ImageView}
     * @param width {@link Integer}
     * @param height {@link Integer}
     */
    protected void applyImageUriToImageView(final Uri imageUri, ImageView imageView, final int width, final int height){
        Picasso.get().load(imageUri).resize(width, height).centerCrop().into(imageView);
    }
}
