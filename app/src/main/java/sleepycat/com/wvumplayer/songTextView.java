package sleepycat.com.wvumplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import sleepycat.com.wvumplayer.songInfoStore.songInfoStoreListener;

/**
 * Created by Davis on 3/10/2015.
 * Slightly modified TextView that allows for
 * easy attachment of a listener
 */
public class songTextView extends TextView implements songInfoStoreListener
{
    public songTextView(Context context)
    {
        super(context);
    }

    public songTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void onInfoChanged(String songinfo)
    {
        //update textView whenever song info is updated
        setText(songinfo);
    }
}
