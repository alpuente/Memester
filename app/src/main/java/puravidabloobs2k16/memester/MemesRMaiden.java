package puravidabloobs2k16.memester;

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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import android.graphics.drawable.BitmapDrawable;
import java.io.FileOutputStream;
import java.io.File;
import android.provider.MediaStore.Images;
import android.net.Uri;


import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.content.Context;
import android.widget.Toast;

import android.graphics.Rect;

public class MemesRMaiden extends AppCompatActivity {

    private static final String DEBUG_TAG = "Gestures";
    //private GestureDetectorCompat mDetector;
    private SwipeListener mDetector;
    private static ImageFinder imageFinder;
    private static PhraseFinder phraseFinder;
    protected static Display display;
    Context meme_context = this; // used as context in gesture listener to start an intent
    private Bitmap global_bitmap;
    private Meme meme;
    private static ArrayList<Meme> g_memes; // store previous memes
    private static int current_meme_index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memes_rmaiden);

        set_background_black(); // make the background black

        imageFinder = new ImageFinder();
        phraseFinder = new PhraseFinder(this);
        g_memes = new ArrayList<>();
        current_meme_index = 0;

        if (phraseFinder.smsConversationsEmpty(this)) {
            Toast.makeText(this.getApplicationContext(), "Sorry, cannot make a meme if you have no saved SMS conversations!", Toast.LENGTH_LONG).show();
        } else if (imageFinder.isCameraDirectoryEmpty()) {
            Toast.makeText(this.getApplicationContext(), "Sorry, cannot make a meme if you have no saved pictures!", Toast.LENGTH_LONG).show();
        } else {

            meme = new Meme(phraseFinder, imageFinder); // don't call makeMeme, because current_meme_index should still be 0
            g_memes.add(meme); // add this meme at index 0

            display = getWindowManager().getDefaultDisplay();
            mDetector = new SwipeListener(this);

            // button to save a meme. opens a dialog box which prompts user to give a file name
            // should alert them if it's a duplicate file... TODO:this
            // shout out to this stackoverflow question http://stackoverflow.com/questions/4918079/android-drawing-a-canvas-to-an-imageview

            set_imageview();

            // thank you stack overflow http://stackoverflow.com/questions/21329132/android-custom-dropdown-popup-menu
            final FloatingActionButton menu_fab = (FloatingActionButton) findViewById(R.id.menu_fab);
            menu_fab.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    PopupMenu popup = new PopupMenu(MemesRMaiden.this, menu_fab);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater()
                            .inflate(R.menu.popup_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    // thank you !!! http://stackoverflow.com/questions/21329132/android-custom-dropdown-popup-menu
                    // https://developer.android.com/training/sharing/send.html
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.toString().contentEquals("Save")) {
                                save_meme(v);
                            } else if (item.toString().contentEquals("Share")) {
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                String pathofBmp = Images.Media.insertImage(getContentResolver(), global_bitmap, "meme", null);
                                Uri bmpUri = Uri.parse(pathofBmp);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                shareIntent.setType("image/png");
                                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
                            }
                            return true;
                        }
                    });
                    popup.show();
                }
            });
        }
    }

    private void set_imageview() {
        try {
            global_bitmap = createBitmap(meme.image_file_name);
            global_bitmap = global_bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(global_bitmap);
            Paint paint = get_paint(canvas);
            global_bitmap = draw_meme(global_bitmap, canvas, paint);
            //canvas.drawBitmap(global_bitmap, 0, 0, paint);
            ImageView imageView = (ImageView) findViewById(R.id.imageView1);
            imageView.setImageDrawable(new BitmapDrawable(getResources(), global_bitmap));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method to open a dialog box and save a meme to the phone external storage
    private void save_meme(View v) {
        // using this stackoverflow article for alert dialog help:
        // http://stackoverflow.com/questions/18799216/how-to-make-a-edittext-box-in-a-dialog
        final EditText input = new EditText(v.getContext());
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext())
                .setTitle("Meme File Name")
                .setMessage("Name Your File")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveBitmap(global_bitmap, input.getText().toString());
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

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private Bitmap draw_meme(Bitmap bitmap, Canvas canvas, Paint paint) {
        Point screen_size = getScreenSize();
        int y_position = getYPosition(screen_size.y, bitmap.getHeight());
        int x_position = getXPosition(screen_size.x, bitmap.getWidth());
        return draw_text(paint, bitmap, canvas, 0, 0);
    }

    // figure out what y position to draw the top text at
    // based on the value returned by paint.getTextBounds
    private int getTopYTextPosition(Rect rect,  Bitmap bitmap) {
        // y position will be an eighth of the bitmap's height + the height of the rectangle
        for (int i = 0; i < 10; i++) {
            System.out.println("height is " + (rect.bottom - rect.top));
        }
        return (rect.bottom - rect.top);
    }

    private int getBottomYTextPosition(Rect rect, Bitmap bitmap) {
        return (bitmap.getHeight() - (int) (Math.floor(.3 * (rect.bottom - rect.top))));
    }

    private Bitmap draw_text(Paint paint, Bitmap bitmap, Canvas canvas, int x_position, int y_position) {
        int font_size = getFontSize(paint, meme.message, bitmap);
        if (font_size < 80) {
            String[] texts = splitText(meme.message);
            String text1 = texts[0];
            String text2 = texts[1];
            while ((font_size = getFontForTwo(paint, text1, text2, bitmap)) < 80) {
                texts = splitText(text1);
                text1 = texts[0];
                text2 = texts[1];
            }
            int y_offset;
            if (font_size >= 100) {
                y_offset = 8;
            } else {
                y_offset = 10;
            }
            //canvas.drawText(text1, x_position, bitmap.getHeight() / y_offset + y_position, paint);
            //canvas.drawText(text2, x_position, bitmap.getHeight() + y_position - (bitmap.getWidth() / 15), paint);
            Rect rect = new Rect();
            paint.getTextBounds(text1, 0, text1.length() - 1, rect);
            int topYPosition = getTopYTextPosition(rect, bitmap);
            canvas.drawText(text1, 0, topYPosition, paint);
            paint.getTextBounds(text2, 0, text2.length() - 1, rect);
            int bottomYPosition = getBottomYTextPosition(rect, bitmap);
            canvas.drawText(text2, 0, bottomYPosition, paint);
        } else {
/*            int y_offset;
            if (font_size >= 100) {
                y_offset = 8;
            } else {
                y_offset = 10;
            }*/
            Rect rect = new Rect();
            paint.getTextBounds(meme.message, 0, meme.message.length() - 1, rect);
            int yPosition = getTopYTextPosition(rect, bitmap);
            //canvas.drawText(meme.message, x_position, bitmap.getHeight() / 8 + y_position, paint);
            canvas.drawText(meme.message, x_position, yPosition, paint);
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

    public void set_background_black () {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(0);
    }

    // when the meme only has one string, find the largest font size that fits on the image
    private int getFontSize(Paint paint, String text, Bitmap bitmap) {
        int font_size = 100;
        while (paint.measureText(text) > bitmap.getWidth()) {
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
            String secondHalf = text.substring(split_index + 1, text.length());
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
    // add the meme to the list at the current index, overwrite what was there if necessary
    protected static Meme makeMeme() {
        current_meme_index++;
        Meme new_meme = new Meme(phraseFinder, imageFinder);
        g_memes.add(current_meme_index, new_meme);
        for (int i = 0; i < 15; i++) {
            System.out.println("current meme index " + current_meme_index);
        }
        return new_meme;
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
                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), filename + ".png");
/*                for (int i = 0; i < 15; i++) {
                    System.out.println("path is " + file.getPath().toString());
                }*/
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Meme get_previous_meme() {
        if (current_meme_index == 0) { // if this is the first viewed meme, just return the same meme
            for (int i = 0; i < 15; i++) {
                System.out.println("same meme ");
            }
            return meme;
        } else { // else, get previous meme
            current_meme_index--;
            return g_memes.get(current_meme_index);
        }
    }

    // used this as a resource!
    // http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
    private class SwipeListener implements View.OnTouchListener{
        private static final String DEBUG_TAG = "Gestures";

        private final GestureDetector gestureDetector;

        public SwipeListener(Context context) {
            gestureDetector = new GestureDetector(context, new SwipeGestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        public final class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
            int diff_threshold = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    float ydiff = e2.getY() - e1.getY();
                    float xdiff = e2.getX() - e1.getX();
                    if (Math.abs(xdiff) > Math.abs(ydiff)) { // horizontal fling
                        if (xdiff > 0) {
                            onRightFling();
                        } else {
                            onLeftFling();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            public void onRightFling() { // if going backward, get previous meme if available
                meme = get_previous_meme();
                for (int i = 0; i < 15; i++) {
                    System.out.println("getting new meme at index " + current_meme_index);
                }
                set_imageview();
            }

            public void onLeftFling() { // if going forward, make a new meme
                meme = makeMeme();
                set_imageview();
            }
        }
    }

}
