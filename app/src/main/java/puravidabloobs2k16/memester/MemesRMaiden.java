package puravidabloobs2k16.memester;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
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

    private SwipeListener mDetector;
    private static ImageFinder imageFinder;
    private static PhraseFinder phraseFinder;
    protected static Display display;
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

            setGlobalBitmap();
            // shout out to this stackoverflow question http://stackoverflow.com/questions/4918079/android-drawing-a-canvas-to-an-imageview
            setImageView();

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

    // sets the global bitmap variable using the global meme image
    private void setGlobalBitmap() {
        try {
            global_bitmap = BitmapUtils.createBitmap(meme.image_file_name);
            global_bitmap = global_bitmap.copy(Bitmap.Config.ARGB_8888, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sets the imageview drawable with the global bitmap and
    // draws the meme to the imageview
    private void setImageView() {
        Canvas canvas = new Canvas(global_bitmap);
        Paint paint = getPaint();
        global_bitmap = drawText(paint, global_bitmap, canvas);
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        imageView.setImageDrawable(new BitmapDrawable(getResources(), global_bitmap));
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
                        BitmapUtils.saveBitmap(global_bitmap, input.getText().toString());
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

    // figure out what y position to draw the top text at
    // based on the value returned by paint.getTextBounds
    private int getTopYTextPosition(Rect rect,  Bitmap bitmap) {
        return (rect.bottom - rect.top);
    }

    // figure out what y position to draw the bottom text at
    // based on the value returned by paint.getTextBounds
    private int getBottomYTextPosition(Rect rect, Bitmap bitmap) {
        return (bitmap.getHeight() - (int) (Math.floor(.3 * (rect.bottom - rect.top))));
    }

    // draws text to a bitmap
    private Bitmap drawText(Paint paint, Bitmap bitmap, Canvas canvas) {
        Paint strokePaint = getStrokePaint();
        int font_size = getFontSize(paint, strokePaint, meme.message, bitmap);
        if (font_size < 80) {
            String[] texts = splitText(meme.message);
            String text1 = texts[0];
            String text2 = texts[1];
            while ((font_size ) < 80) {
                texts = splitText(text1);
                text1 = texts[0];
                text2 = texts[1];
                font_size = getFontSize(paint, strokePaint, text1, text2, bitmap);
            }
            Rect rect = new Rect();
            paint.getTextBounds(text1, 0, text1.length() - 1, rect);
            int topYPosition = getTopYTextPosition(rect, bitmap);
            canvas.drawText(text1, 0, topYPosition, strokePaint);
            canvas.drawText(text1, 0, topYPosition, paint);
            paint.getTextBounds(text2, 0, text2.length() - 1, rect);
            int bottomYPosition = getBottomYTextPosition(rect, bitmap);
            canvas.drawText(text2, 0, bottomYPosition, strokePaint);
            canvas.drawText(text2, 0, bottomYPosition, paint);
        } else {
            Rect rect = new Rect();
            paint.getTextBounds(meme.message, 0, meme.message.length() - 1, rect);
            int yPosition = getTopYTextPosition(rect, bitmap);
            canvas.drawText(meme.message, 0, yPosition, strokePaint);
            canvas.drawText(meme.message, 0, yPosition, paint);
        }
        return bitmap;
    }

    // method to init a new paint for a canvas and return it
    private Paint getPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(115);
        paint.setTypeface(getTypeface());
        return paint;
    }

    // using this stackoverflow question
    // http://stackoverflow.com/questions/1723846/how-do-you-draw-text-with-a-border-on-a-mapview-in-android
    // answer by Jeremy Logan
    private Paint getStrokePaint() {
        Paint strokePaint = new Paint();
        strokePaint.setARGB(255, 0, 0, 0);
        strokePaint.setTypeface(getTypeface());
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setTextSize(115);
        strokePaint.setStrokeWidth(10);
        return strokePaint;
    }


    // method to get this Impact-like font from the app's assets
    private Typeface getTypeface() {
        AssetManager am = this.getApplicationContext().getAssets();
        return Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "ImpactURW.ttf"));
    }

    public void set_background_black () {
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(0);
    }

    // when the meme only has one string, find the largest font size that fits on the image
    private int getFontSize(Paint paint, Paint strokePaint, String text, Bitmap bitmap) {
        int font_size = 100;
        while (paint.measureText(text) > bitmap.getWidth()) {
            paint.setTextSize(font_size);
            strokePaint.setTextSize(font_size);
            font_size -= 1;
        }
        return font_size;
    }

    // when the meme's texts are split, find the largest font size that fits on the image
    private int getFontSize(Paint paint, Paint strokePaint, String text1, String text2, Bitmap bitmap) {
        int font_size = 115;
        paint.setTextSize(font_size);
        while (paint.measureText(text1) > bitmap.getWidth() || paint.measureText(text2) > bitmap.getWidth()) {
            paint.setTextSize(font_size);
            strokePaint.setTextSize(font_size);
            font_size -= 1;
        }
        return font_size;
    }

    // split a text in (about) half at whitespace
    private String[] splitText(String text) {
        int split_index = text.length() / 2;

        // get the middle index and check if it's a space
        // if not, keep decrementing the index until a space is found or the index is 0
        while (split_index >= 0 && text.charAt(split_index) != ' ') {
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
            String firstHalf = text.substring(0, split_index);
            String secondHalf = text.substring(split_index + 1, text.length());
            texts = new String[2];
            texts[0] = firstHalf;
            texts[1] = secondHalf;
        }
        return texts;
    }

    // gets the size of the screen, returning it as a point
    protected static Point getScreenSize() {
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
        return new_meme;
    }

    private Meme get_previous_meme() {
        if (current_meme_index == 0) { // if this is the first viewed meme, just return the same meme
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
                setGlobalBitmap();
                setImageView();
            }

            public void onLeftFling() { // if going forward, make a new meme
                meme = makeMeme();
                setGlobalBitmap();
                setImageView();
            }
        }
    }

}
