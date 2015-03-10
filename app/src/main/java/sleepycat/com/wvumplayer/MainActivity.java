package sleepycat.com.wvumplayer;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.ImageButton;
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


public class MainActivity extends ActionBarActivity
{
    //member variables
    //data
    String m_sMetaDataStart;
    String m_sMetaDataEnd;
    String m_sSongData;
    boolean m_bIsReady;
    //objects
    MediaPlayer m_WVUMStream;
    ImageButton m_PlayButton;
    ImageButton m_StopButton;
    TextView m_SongDisplayLabel;

    //methods
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize POD
        m_sMetaDataStart = "<font face=\"calibri\"><!--includeThisInApp-->";
        m_sMetaDataEnd = "</body></html></font>";
        m_sSongData = "";
        m_bIsReady = false;

        //initialize streams
        initAudioStream();
        new getDataAsyncTask().execute("http://wvum.org/index.php/wvum/stream/");

        //initialize GUI elements
        m_PlayButton = (ImageButton)findViewById(R.id.playButton);
        m_StopButton = (ImageButton)findViewById(R.id.stopButton);
        m_SongDisplayLabel = (TextView)findViewById(R.id.songDataLabel);

        //GUI Listeners
        m_PlayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(m_bIsReady)
                {
                    if (isNetworkAvailable())
                    {
                        m_WVUMStream.start();
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Please connect to the Internet and try again", Toast.LENGTH_LONG);
                }
                else
                    Log.d("m_PlayButton", "STREAM NOT READY");
            }
        });

        m_StopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_WVUMStream.stop();
            }
        });
    }

    private void initAudioStream()
    {
        if(isNetworkAvailable())
        {
            //Address of wvum stream. Parse into uri and hand to media player
            String sAddr = "http://wvum.org:9010/";
            Uri myUri = Uri.parse(sAddr);

            m_WVUMStream = new MediaPlayer();
            m_WVUMStream.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try
            {
                m_WVUMStream.setDataSource(getApplicationContext(), myUri);
            }
            catch (IOException e)
            {
                Log.d("IOEXCEPTION", "WHY IS THIS BROKEN", e);
            }
        }
        //set listener so play/stop functionality can check for valid stream
        m_WVUMStream.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            public void onPrepared(MediaPlayer mp)
            {
                m_bIsReady = true;
            }
        });
        m_WVUMStream.prepareAsync();
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManage = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManage.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Async Class for getting data
    private class getDataAsyncTask extends AsyncTask<String, Void, Boolean>
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
                        //+4 is because of escaped characters
                        m_sSongData = trimString(inputLine);
                        break;
                    }
                }
                mIn.close();
            }
            catch (MalformedURLException e){}
            catch (IOException e){}
            return true;
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
