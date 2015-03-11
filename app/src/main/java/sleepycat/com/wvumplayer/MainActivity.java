package sleepycat.com.wvumplayer;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;


public class MainActivity extends ActionBarActivity
{
    public static final String KEY_PREF_UPDATERATE = "updateFrequency";
    public static final String KEY_PREF_OFFSREEN = "offscreenStreaming";
    private static final int SETTINGS_RESULT = 1;
    //member variables
    //data
    private String m_sMetaDataStart;
    private String m_sMetaDataEnd;
    private String m_sSHOUTCastLink;
    private String m_sMetaDataLink;
    private boolean m_bDoRadio;
    private boolean m_bToOtherActivity;
    //how long between asynctask calls in milliseconds
    private int m_nPollTime;
    private boolean m_bIsReady;
    //objects
    private MediaPlayer m_WVUMStream;
    private ImageButton m_PlayButton;
    private ImageButton m_StopButton;
    private ImageButton m_LogoButton;
    private songTextView m_SongDisplayLabel;
    private songInfoStore m_SongData;
    private Handler m_TimerHandler;
    private Runnable m_TimerRunnable;
    private SharedPreferences m_Prefs;
    private ProgressBar m_LoadingSpinner;

    //methods

    //Overridden methods
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize member variables
        m_sMetaDataStart = "<font face=\"calibri\"><!--includeThisInApp-->";
        m_sMetaDataEnd = "</body></html></font>";
        m_sSHOUTCastLink = "http://wvum.org:9010/";
        m_sMetaDataLink = "http://wvum.org/index.php/wvum/stream/";
        m_SongData = new songInfoStore("");
        m_bDoRadio = true;
        m_bToOtherActivity = false;
        m_nPollTime = 30000;
        m_bIsReady = false;
        m_TimerHandler = null;
        m_TimerRunnable = null;
        m_WVUMStream = null;
        m_Prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(isNetworkAvailable())
        {
            //initialize streams
            initAudioStream();
        }
        else
            Toast.makeText(getApplicationContext(), "Please connect to the Internet.", Toast.LENGTH_LONG).show();
        //initialize GUI elements
        initGUI();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        clearTimer();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getValuesFromPrefs();
        m_bToOtherActivity = false;
        initTimer();
        if(!isNetworkAvailable())
            Toast.makeText(getApplicationContext(), "Please connect to the Internet.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(!m_bDoRadio && !m_bToOtherActivity)
        {
            m_WVUMStream.stop();
            m_bIsReady = false;
            m_WVUMStream.release();
            m_WVUMStream = null;
        }
        clearTimer();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        m_WVUMStream.stop();
        m_bIsReady = false;
        m_WVUMStream.release();
        m_WVUMStream = null;
        clearTimer();
    }

    //User methods
    private void initGUI()
    {
        m_LoadingSpinner = (ProgressBar)findViewById(R.id.loadingSpinner);
        m_PlayButton = (ImageButton)findViewById(R.id.playButton);
        m_StopButton = (ImageButton)findViewById(R.id.stopButton);
        m_LogoButton = (ImageButton)findViewById(R.id.wvumLogo);
        m_SongDisplayLabel = (songTextView)findViewById(R.id.songDataLabel);

        m_LoadingSpinner.setVisibility(View.GONE);
        //GUI Listeners
        m_SongData.setListener(m_SongDisplayLabel);
        m_PlayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isNetworkAvailable())
                {
                    if(m_WVUMStream == null)
                        initAudioStream();
                    if(!m_bIsReady)
                    {
                        m_LoadingSpinner.setVisibility(View.VISIBLE);
                        m_WVUMStream.prepareAsync();
                    }
                    else
                        m_WVUMStream.start();
                    if(m_nPollTime != -1)
                        new getDataAsyncTask().execute(m_sMetaDataLink);
                }
                else
                    Toast.makeText(getApplicationContext(), "Please connect to the Internet and try again.", Toast.LENGTH_LONG).show();
            }
        });
        m_StopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(m_WVUMStream != null)
                {
                    m_bIsReady = false;
                    m_WVUMStream.stop();
                }
            }
        });
        m_LogoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.wvum.org"));
                startActivity(browserIntent);
            }
        });
    }

    private void initAudioStream()
    {
        if(isNetworkAvailable())
        {
            //Address of wvum stream. Parse into uri and hand to media player
            Uri myUri = Uri.parse("http://wvum.org:9010/");

            m_WVUMStream = new MediaPlayer();
            m_WVUMStream.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try
            {
                m_WVUMStream.setDataSource(getApplicationContext(), myUri);
            }
            catch (IOException e)
            {
                Log.d("IOEXCEPTION", e.getMessage());
            }
        }
        //set listener so play/stop functionality can check for valid stream
        m_WVUMStream.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            public void onPrepared(MediaPlayer mp)
            {

                m_WVUMStream.start();
                m_LoadingSpinner.setVisibility(View.GONE);
                m_bIsReady = true;
            }
        });
        //m_WVUMStream.prepareAsync();
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManage = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManage.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void clearTimer()
    {
        m_TimerHandler.removeCallbacks(null);
        //m_TimerRunnable = null;
        //m_TimerHandler = null;
    }

    private void initTimer()
    {
        m_TimerHandler = new Handler();
        m_TimerRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(m_nPollTime > 0)
                {
                    if (isNetworkAvailable())
                        new getDataAsyncTask().execute(m_sMetaDataLink);
                    m_TimerHandler.postDelayed(this, m_nPollTime);
                }
                else
                    m_SongDisplayLabel.setText("");
            }
        };
        m_TimerHandler.postDelayed(m_TimerRunnable, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            m_bToOtherActivity = true;
            Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.action_credits)
        {
            m_bToOtherActivity = true;
            Intent intent = new Intent(getApplicationContext(),CreditsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        getValuesFromPrefs();
    }

    private void getValuesFromPrefs()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String s = sharedPreferences.getString("updateFrequency", "30000");
        m_nPollTime = Integer.parseInt(s);
        boolean b = sharedPreferences.getBoolean("offscreenStreaming", true);
        m_bDoRadio = b;
    }

    //Async Class for getting data from stream
    private class getDataAsyncTask extends AsyncTask<String, String, Boolean>
    {
        @Override
        protected Boolean doInBackground(String ... sURL)
        {
            //Initializations
            URL mWVUM_URL = null;
            BufferedReader mIn = null;

            try
            {
                mWVUM_URL = new URL(sURL[0]);
                mIn = new BufferedReader(new InputStreamReader(mWVUM_URL.openStream()));
                String inputLine;
                while ((inputLine = mIn.readLine()) != null)
                {
                    if(inputLine.contains(m_sMetaDataStart))
                    {
                        inputLine = trimString(inputLine);
                        publishProgress(inputLine);
                        break;
                    }
                }
                mIn.close();
            }
            catch (MalformedURLException e){}
            catch (IOException e){}
            return true;
        }

        protected void onProgressUpdate(String ... progress)
        {
            super.onProgressUpdate(progress);
            m_SongData.setSongInfo(progress[0]);
        }

        private String trimString(String sInput)
        {
            String sResult = "";
            //+4 is to make up for the two escaped quotation marks
            int nLowerBound = m_sMetaDataStart.length() + 4;

            while(sInput.charAt(nLowerBound) != '<')
            {
                sResult = sResult + sInput.charAt(nLowerBound);
                nLowerBound++;
            }
            return sResult;
        }
    }
}
