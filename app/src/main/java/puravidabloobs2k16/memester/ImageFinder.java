package puravidabloobs2k16.memester;

import android.os.Environment;

import java.io.File;
import java.util.Random;

/**
 * Created by appleowner on 5/22/16.
 * Class to grab a random image from the phone's external storage.
 */
public class ImageFinder {

    protected String getImageFileName() {

        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM + "/Camera");
        File[] files = path.listFiles();

        return files[getRandomIndex(files.length)].toString();
    }

    private int getRandomIndex(int max) {
        Random r = new Random();
        return r.nextInt(max) + 0;
    }
}
