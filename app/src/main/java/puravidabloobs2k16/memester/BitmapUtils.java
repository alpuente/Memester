package puravidabloobs2k16.memester;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by appleowner on 10/23/16.
 */
public class BitmapUtils {

    protected static void saveBitmap(Bitmap bitmap, String filename) {
        if (isExternalStorageWritable()) {
            try {
                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), filename + ".png");
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // src: https://developer.android.com/training/basics/data-storage/files.html
    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    // rotates a bitmap
    protected static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // checks if the image is bigger than the display size
    // returns a the ratio between the image width and display width for resizing
    // used this tutorial http://www.informit.com/articles/article.aspx?p=2143148&seqNum=2u
    protected static BitmapFactory.Options getSampleSize(int display_width, String file_name) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file_name, options);
        int width = options.outWidth;
        if (width > display_width) {
            int width_ratio = Math.round((float) width / (float) display_width);
            options.inSampleSize = width_ratio;
        }

        options.inJustDecodeBounds = false;
        return options;
    }

    //make a bitmap from an image file
    protected static Bitmap createBitmap(String file_name) throws IOException {
        Point display_size = MemesRMaiden.getScreenSize();
        Bitmap bitmap = BitmapFactory.decodeFile(file_name, getSampleSize(display_size.x, file_name));
        if (isHorizontal(file_name)) { // if the image is horizontal
            bitmap = rotateBitmap(bitmap, 90); // rotate it by 90 degrees
        }
        return bitmap;
    }

    // checks if an image is horizontal
    private static boolean isHorizontal(String file_name) throws IOException {
        ExifInterface exif = new ExifInterface(file_name);
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) == 6;
    }
}
