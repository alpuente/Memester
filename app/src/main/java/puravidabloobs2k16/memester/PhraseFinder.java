package puravidabloobs2k16.memester;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by appleowner on 5/22/16.
 * Class to grab a random meme phrase from the phone's SMS storage.
 */
public class PhraseFinder {
    private List<String> texts;

    // constructor takes in a context object and gets the phone's sent sms messages
    protected PhraseFinder(Context context) {
        this.texts = getAllSms(context);
    }

    // returns the message content of a random SMS message from the phone's list of SMS messages
    protected String getPhrase() {
        return texts.get(getRandomIndex(texts.size()));
    }


    private int getRandomIndex(int max) {
        Random r = new Random();
        return r.nextInt(max) + 0;
    }

    // method to get all the sent SMS messages in a phone
    // returns them as a list
    protected List<String> getAllSms(Context context) {
        ArrayList<String> texts = new ArrayList<String>();
        ContentResolver cr = context.getContentResolver();

        Cursor in = cr.query(Telephony.Sms.Inbox.CONTENT_URI,
                new String[] {Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY},
                null,
                null,
                Telephony.Sms.Inbox.DEFAULT_SORT_ORDER);

        Cursor out = cr.query(Telephony.Sms.Sent.CONTENT_URI,
                new String[] {Telephony.Sms.Sent.ADDRESS, Telephony.Sms.Sent.BODY},
                null,
                null,
                Telephony.Sms.Outbox.DEFAULT_SORT_ORDER);

        int totalSMSOut = out.getCount();
        int totalSMSIn = in.getCount();

        if (out.moveToFirst()) {
            for (int i = 0; i < totalSMSOut - 1; i++) {
                texts.add(out.getString(1));
                out.moveToNext();
            }
        }

        if (in.moveToFirst()) {
            for (int i = 0; i < totalSMSIn - 1; i++) {
                texts.add(in.getString(1));
                in.moveToNext();
            }
        }

        in.close();
        out.close();

        return texts;
    }
}
