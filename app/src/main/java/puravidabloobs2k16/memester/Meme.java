package puravidabloobs2k16.memester;

/**
 * Created by appleowner on 5/22/16.
 * Class to make a meme by getting a random image and random phrase
 */
public class Meme {
    protected String image_file_name; // the meme's image filename
    protected String message; // the meme's message

    // constructor that sets the meme's image and message
    protected Meme(PhraseFinder phraseFinder, ImageFinder imageFinder) {
        image_file_name = imageFinder.getImageFileName();
        message = phraseFinder.getPhrase();
    }
}
