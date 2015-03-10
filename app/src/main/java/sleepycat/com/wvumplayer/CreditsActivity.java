package sleepycat.com.wvumplayer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Davis on 3/10/2015.
 * Intended to show dev credits and(?) WVUM staff
 */
public class CreditsActivity extends ActionBarActivity
{
    //members
    private static final String ABOUT_URL = "http://www.wvum.org/index.php/info/aboutus/";
    private static final String PROFILE_URL = "http://www.wvum.org/index.php/wvum/profile/";
    TextView m_Staff;
    String m_StaffNames;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        m_StaffNames = "";
        setContentView(R.layout.activity_credits);
        m_Staff = (TextView)findViewById(R.id.currentStaff);
        new getDataAsyncTask().execute("");
    }

    private class getDataAsyncTask extends AsyncTask<String, String, Boolean>
    {
        @Override
        protected Boolean doInBackground(String ... s)
        {
            //Initializations
            URL mWVUM_URL = null;
            BufferedReader mIn = null;

            try
            {
                mWVUM_URL = new URL(ABOUT_URL);
                mIn = new BufferedReader(new InputStreamReader(mWVUM_URL.openStream()));
                String inputLine;
                while ((inputLine = mIn.readLine()) != null)
                {
                    if(inputLine.contains(PROFILE_URL))
                    {
                        inputLine = trimString(inputLine);
                        Log.d("parsing URL ... ", inputLine);
                        if(inputLine.length() > 1)
                            m_StaffNames = m_StaffNames + "\n" + inputLine;
                    }
                }
                mIn.close();
            }
            catch (MalformedURLException e){}
            catch (IOException e){}
            publishProgress(m_StaffNames);
            return true;
        }

        protected void onProgressUpdate(String ... progress)
        {
            super.onProgressUpdate(progress);
            m_Staff.setText(progress[0]);
        }

        private String trimString(String sInput)
        {
            int nBegin = 0, nEnd = 0;
            for(nBegin = 2; nBegin < sInput.length(); nBegin++)
            {
                if(sInput.charAt(nBegin) == '>' && sInput.charAt(nBegin-1) == '"' && sInput.charAt(nBegin-2) == '/')
                    break;
            }
            for(nEnd = nBegin; nEnd < sInput.length(); nEnd++)
            {
                if(sInput.charAt(nEnd) == '<')
                    break;
            }
            sInput = sInput.substring(nBegin+1, nEnd);
            return sInput;
        }
    }
}