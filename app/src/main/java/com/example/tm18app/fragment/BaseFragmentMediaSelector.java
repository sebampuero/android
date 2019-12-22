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
public abstract class BaseFragmentMediaSelector extends BaseFragment{

    protected static final int PICK_IMAGE = 100;
    protected static final int PICK_VIDEO = 200;
    protected BitmapLoaderInterface bitmapLoaderInterface;

    private static final int MAX_HEIGHT = 800;

    /**
     * Interface to get called for when a {@link Bitmap} gets loaded by {@link Picasso}
     */
    interface BitmapLoaderInterface {

        /**
         * When the selected {@link Bitmap} is successfully loaded
         * @param bitmap {@link Bitmap}
         */
        void onBitmapLoaded(Bitmap bitmap);

        void onLoadingBitmap();
    }

    public BaseFragmentMediaSelector() {
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
    protected void openGalleryForImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),PICK_IMAGE);
    }

    protected void openGalleryForVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),PICK_VIDEO);
    }

    /**
     * Loads the selected image from gallery by {@link Picasso}
     * @param imageUri {@link Uri}
     * @param width {@link Integer}
     * @param height {@link Integer}
     */
    protected void processImageURI(Uri imageUri, int width, int height){
        if(height > MAX_HEIGHT)
            height = MAX_HEIGHT;
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
                bitmapLoaderInterface.onLoadingBitmap();
            }
        });
    }

}
