package com.example.tm18app.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

/**
 * A simple {@link Fragment} abstract subclass. This class is responsible for media selection
 * functions
 *
 * @author Sebastian Ampuero
 * @version  1.0
 * @since 03.12.2019
 */
public abstract class BaseFragmentMediaSelector extends BaseFragment{

    protected static final int PICK_IMAGE = 100;
    protected static final int PICK_VIDEO = 200;
    protected BitmapLoaderInterface mBitmapLoaderInterface;

    /**
     * Interface to get called for when a {@link Bitmap} gets loaded by {@link Picasso}
     */
    interface BitmapLoaderInterface {

        /**
         * When the selected {@link Bitmap} is successfully loaded
         * @param bitmap {@link Bitmap}
         */
        void onBitmapLoaded(Bitmap bitmap);

        /**
         * When the selected {@link Bitmap} is currently loading.
         */
        void onLoadingBitmap();

        void onErrorLoadingBitmap();
    }

    public BaseFragmentMediaSelector() {
    }

    /**
     * Sets the implementation of {@link BitmapLoaderInterface}
     * @param ic {@link BitmapLoaderInterface}
     */
    protected void setBitmapLoaderInterface(BitmapLoaderInterface ic){
        this.mBitmapLoaderInterface = ic;
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

    /**
     * Opens an {@link Intent} to select a video from the gallery
     */
    protected void openGalleryForVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),PICK_VIDEO);
    }

    /**
     * Loads the selected image from gallery by {@link Picasso}
     * @param imageUri {@link Uri}
     * @param height {@link Integer} height to resize the image, can be 0. If 0, original height is used
     * @param width {@link Integer} width to resize the image, can be 0. If 0, original width is used
     */
    protected void processImageURI(Uri imageUri, int height, int width){
        RequestCreator creator = Picasso.get().load(imageUri);
        if(height != 0 && width != 0)
            creator = creator.resize(width, height).centerCrop();
        creator.into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mBitmapLoaderInterface.onBitmapLoaded(bitmap); // call the method for fragments to
                // know that the bitmap was loaded
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                mBitmapLoaderInterface.onErrorLoadingBitmap();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                mBitmapLoaderInterface.onLoadingBitmap();
            }
        });
    }

}
