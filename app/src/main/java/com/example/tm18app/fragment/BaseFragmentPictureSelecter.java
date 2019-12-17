package com.example.tm18app.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
    protected BitmapLoaderInterface bitmapLoaderInterface;

    /**
     * Interface to get called for when a {@link Bitmap} gets loaded by {@link Picasso}
     */
    interface BitmapLoaderInterface {

        /**
         * When the selected {@link Bitmap} is successfully loaded
         * @param bitmap {@link Bitmap}
         */
        void onBitmapLoaded(Bitmap bitmap);
    }

    public BaseFragmentPictureSelecter() {
    }

    /**
     * Sets the implementation of {@link BitmapLoaderInterface}
     * @param ic {@link BitmapLoaderInterface}
     */
    protected void setBitmapLoaderInterface(BitmapLoaderInterface ic){
        this.bitmapLoaderInterface = ic;
    }

    /**
     * Opens an {@link Intent} to select an image from the gallery
     */
    protected void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    /**
     * Loads the selected image from gallery by {@link Picasso}
     * @param imageUri {@link Uri}
     * @param width {@link Integer}
     * @param height {@link Integer}
     */
    protected void processImageURI(final Uri imageUri, final int width, final int height){
        //Picasso.get().load(imageUri).resize(width, height).centerCrop().into(imageView);
        Picasso.get().load(imageUri).resize(width, height).centerCrop().into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.e("TAG", "Loaded bitmap");
                bitmapLoaderInterface.onBitmapLoaded(bitmap); // call the method for fragments to
                // know that the bitmap was loaded
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.e("TAG", "failed bitmap");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.e("TAG", "on prepare load");
            }
        });
    }
}
