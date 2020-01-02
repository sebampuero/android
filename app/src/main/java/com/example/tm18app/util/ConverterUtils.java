package com.example.tm18app.util;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Utility class for data type conversions
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class ConverterUtils {

    /**
     * Get bytes from an {@link InputStream}
     * @param inputStream {@link InputStream}
     * @return array of bytes
     * @throws IOException
     */
    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * Get bytes of a {@link Bitmap}
     * @param bitmap {@link Bitmap}
     * @return byte array of the Bitmap
     */
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = null;
        try{
            stream = new ByteArrayOutputStream();
            // only 50% of original quality. Bandwidth and network are costly
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            return stream.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
            return new byte[]{0};
        }finally {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get density pixels out of a plain pixel input
     * @param dp {@link Integer} density pixels to convert from
     * @param context {@link Context}
     * @return pixel values
     */
    public static int dpToPx(int dp, Context context) {
        float density = context.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }


    /**
     * Extracts the key of an Image hosted in the internet, e.g. host.com/files/[12345.jpg]
     * Useful for caching images. Picasso caches images using a key. By default, Picasso uses the whole
     * URL but since Cloudinary changes the URL to tweak quality it invalidates the Cache. For
     * that reason, use the image key that remains unchanged instead of the whole URL.
     * @param imgUrl {@link String} the URL
     * @return {@link String} the key of the URL
     */
    public static String extractUrlKey(String imgUrl) {
        return imgUrl.substring(imgUrl.lastIndexOf("/")+1);
    }
}
