package puravidabloobs2k16.memester;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.IOException;
import java.util.Locale;
import android.graphics.drawable.BitmapDrawable;
import java.io.FileOutputStream;
import java.io.File;

public class MemesRMaiden extends Activity {
    private static ImageFinder imageFinder;
    private static PhraseFinder phraseFinder;
    protected static Display display;
    Bitmap bg;
    Meme meme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memes_rmaiden);

        imageFinder = new ImageFinder();
        phraseFinder = new PhraseFinder(this);
        meme = new Meme(phraseFinder, imageFinder);

        display = getWindowManager().getDefaultDisplay();

        // button to save a meme. opens a dialog box which prompts user to give a file name
        // should alert them if it's a duplicate file... TODO:this
        // shout out to this stackoverflow question http://stackoverflow.com/questions/4918079/android-drawing-a-canvas-to-an-imageview
        try {
           bg = createBitmap(meme.image_file_name);
            bg = bg.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(bg);
            Paint paint = get_paint(canvas);
            bg = draw_meme(bg, canvas, paint);
            //canvas.drawBitmap(bg, 0, 0, paint);
            ImageView imageView = (ImageView) findViewById(R.id.imageView1);
            imageView.setImageDrawable(new BitmapDrawable(getResources(), bg));
       } catch (IOException e) {
            e.printStackTrace();
        }

        final Button button = (Button) findViewById(R.id.save_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // using this stackoverflow article for alert dialog help:
                // http://stackoverflow.com/questions/18799216/how-to-make-a-edittext-box-in-a-dialog
                final EditText input = new EditText(v.getContext());
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("Meme File Name")
                        .setMessage("Name Your File")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                saveBitmap(bg, input.getText().toString());
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        });

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input); // uncomment this line
                alertDialog.show();
            }
        });
    }

    private Bitmap draw_meme(Bitmap bitmap, Canvas canvas, Paint paint) {
        Point screen_size = getScreenSize();
        int y_position = getYPosition(screen_size.y, bitmap.getHeight());
        int x_position = getXPosition(screen_size.x, bitmap.getWidth());
        //canvas.drawBitmap(bitmap, x_position, y_position, paint);
        return draw_text(paint, bitmap, canvas, 0, 0);
    }

    private Bitmap draw_text(Paint paint, Bitmap bitmap, Canvas canvas, int x_position, int y_position) {
        int font_size = getFontSize(paint, meme.message, bitmap);
        if (font_size < 80) {
            String[] texts = splitText(meme.message);
            String text1 = texts[0];
            String text2 = texts[1];
            font_size = getFontForTwo(paint, text1, text2, bitmap);
            System.out.println("font_size " + font_size);
            paint.setTextSize(font_size);
            System.out.println("bitmap width " + bitmap.getWidth() + " text1 width " + paint.measureText(text1) + " text2 width " + paint.measureText(text2));
            int y_offset;
            if (font_size >= 100) {
                y_offset = 8;
            } else {
                y_offset = 10;
            }
            canvas.drawText(text1, x_position, bitmap.getHeight() / y_offset + y_position, paint);
            canvas.drawText(text2, x_position, bitmap.getHeight() + y_position - (bitmap.getWidth() / 15), paint);
        } else {
            int y_offset;
            if (font_size >= 100) {
                y_offset = 8;
            } else {
                y_offset = 10;
            }
            canvas.drawText(meme.message, x_position, bitmap.getHeight() / 8 + y_position, paint);
        }
        return bitmap;
    }

    // method to init a new paint for a canvas and return it
    private Paint get_paint(Canvas canvas) {
        Paint paint = new Paint();
        //canvas.drawPaint(paint);
        paint.setColor(Color.WHITE);
        int text_size = 115;
        paint.setTextSize(text_size);
        paint.setTypeface(getTypeface());
        return paint;
    }


    // method to get this Impact-like font from the app's assets
    private Typeface getTypeface() {
        AssetManager am = this.getApplicationContext().getAssets();
        return Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "Coda-Heavy.ttf"));
    }

    //make a bitmap from an image file
    private Bitmap createBitmap(String file_name) throws IOException {
        Point display_size = getScreenSize();
        Bitmap bitmap = BitmapFactory.decodeFile(file_name, getSampleSize(display_size.x, file_name));
        if (isHorizontal(file_name)) { // if the image is horizontal
            bitmap = rotateBitmap(bitmap, 90); // rotate it by 90 degrees
        }
        return bitmap;
    }


    // when the meme only has one string, find the largest font size that fits on the image
    private int getFontSize(Paint paint, String text, Bitmap bitmap) {
        int font_size = 100;
        while (paint.measureText(text) > bitmap.getWidth()) {
            System.out.println("font size " + font_size);
            paint.setTextSize(font_size);
            font_size -= 1;
        }
        return font_size;
    }

    // when the meme's texts are split, find the largest font size that fits on the image
    private int getFontForTwo(Paint paint, String text1, String text2, Bitmap bitmap) {
        int font_size = 115;
        paint.setTextSize(font_size);
        while (paint.measureText(text1) > bitmap.getWidth() || paint.measureText(text2) > bitmap.getWidth()) {
            paint.setTextSize(font_size);
            font_size -= 1;
        }
        return font_size;
    }

    // gets the desired y position to center the image on the canvas
    private int getYPosition(int display_height, int image_height) {
        return (display_height - image_height) / 2;
    }

    // gets the desired x position to center the image on the canvas
    private int getXPosition(int display_width, int image_width) {
        return (display_width - image_width) / 2;
    }

    // split a text in (about) half at whitespace
    private String[] splitText(String text) {
        int split_index = text.length() / 2;

        // get the middle index and check if it's a space
        // if not, keep decrementing the index until a space is found or the index is 0
        while (split_index >= 0 && text.charAt(split_index) != ' ') {
            System.out.println("char at " + split_index + " is " + text.charAt(split_index));
            split_index -= 1;
        }
        if (split_index == -1 ) {
            split_index = 0;
        }
        String[] texts;
        if (split_index == 0) { // if the index is 0, just split the text in half and don't worry about being cute
            String firstHalf = text.substring(0, text.length()/ 2);
            String secondHalf = text.substring(text.length()/2, text.length());
            texts = new String[2];
            texts[0] = firstHalf;
            texts[1] = secondHalf;
        } else {
            System.out.println("INDEX " + split_index);
            String firstHalf = text.substring(0, split_index);
            String secondHalf = text.substring(split_index, text.length());
            texts = new String[2];
            texts[0] = firstHalf;
            texts[1] = secondHalf;
        }
        return texts;
    }

    // checks if the image is bigger than the display size
    // returns a the ratio between the image width and display width for resizing
    // used this tutorial http://www.informit.com/articles/article.aspx?p=2143148&seqNum=2u
    // TODO: incorporate height into this
    private BitmapFactory.Options getSampleSize(int display_width, String file_name) {
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

    // checks if an image is horizontal
    private boolean isHorizontal(String file_name) throws IOException {
        ExifInterface exif = new ExifInterface(file_name);
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) == 6;
    }

    // rotates a bitmap
    private Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // gets the size of the screen, returning it as a point
    private Point getScreenSize() {
        Point point = new Point();
        display.getSize(point);
        return point;
    }

    // creates a new Meme object from global ImageFinder and PhraseFinder objects
    protected static Meme makeMeme() {
        return new Meme(phraseFinder, imageFinder);
    }


    // src: https://developer.android.com/training/basics/data-storage/files.html
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void saveBitmap(Bitmap bitmap, String filename) {
        if (isExternalStorageWritable()) {
            try {
                //String path = Environment.getExternalStoragePublicDirectory(
                //        Environment.DIRECTORY_DCIM + "/Pictures");
                //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + filename;
                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), filename + ".png");
                for (int i = 0; i < 15; i++) {
                    System.out.println("path is " + file.getPath().toString());
                }
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
