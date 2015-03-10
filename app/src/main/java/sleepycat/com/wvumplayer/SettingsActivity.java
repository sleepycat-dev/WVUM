package sleepycat.com.wvumplayer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Davis on 3/10/2015.
 */
public class SettingsActivity extends PreferenceActivity
{
    //methods

    //Overridden methods
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Must use deprecated function because this app doesn't make use of
        //fragments and supports Froyo.
        addPreferencesFromResource(R.xml.preferences);
    }
}
