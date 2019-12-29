package com.example.tm18app.util;

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
            // Quality of 70% because:
            // No high resolutions needed due to the only usage of smartphones < 5.5"
            // Save network bandwidth
            // Save Cloudinary limited bandwidth and storage usage
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
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


}
