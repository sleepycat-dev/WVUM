package sleepycat.com.wvumplayer;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;


public class MainActivity extends ActionBarActivity
{
    //member variables
    //data
    boolean m_bIsReady;
    //objects
    MediaPlayer m_WVUMStream;
    Button m_PlayButton;
    Button m_StopButton;

    //methods
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize streams
        initAudioStream();
        initDataStream();

        //initialize GUI elements
        m_PlayButton = (Button)findViewById(R.id.playButton);
        m_StopButton = (Button)findViewById(R.id.stopButton);

        //GUI Listeners
        m_PlayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(m_bIsReady)
                {
                    if (isNetworkAvailable())
                        m_WVUMStream.start();
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

    private void initDataStream()
    {
        //Initializations
        String sAddr = "http://wvum.org:9010/";
        String sUrl = "http://wvum.org/index.php/wvum/stream/";
        URL dataURL = null;
        URLConnection conn = null;
        InputStream is = null;
        int nMetaDataLength = 0;

        try
        {dataURL = new URL(sAddr);}
        catch(MalformedURLException e)
        {
            //Do something...
        }
        try
        {conn = dataURL.openConnection();}
        catch(IOException e)
        {
            //Do something
        }
        conn.setRequestProperty("Icy-MetaData", "1");
        int interval = Integer.valueOf(conn.getHeaderField("icy-metaint"));
        try
        {is = conn.getInputStream();}
        catch(IOException e)
        {
            //do something
        }

        int skipped = 0;
        while (skipped < interval)
        {
            try{skipped += is.skip(interval - skipped);}
            catch(IOException e)
            {/*do something*/}
        }

        try{nMetaDataLength = is.read() * 16;}
        catch(IOException e)
        {}

        int bytesRead = 0;
        int offset = 0;
        byte[] bytes = new byte[nMetaDataLength];

        while (bytesRead < nMetaDataLength && bytesRead != -1)
        {
            try{bytesRead = is.read(bytes, offset, nMetaDataLength);}
            catch(IOException e){/*do something*/}
            offset = bytesRead;
        }

        String metaData = new String(bytes).trim();
        String title = metaData.substring(metaData.indexOf("StreamTitle='") + 13, metaData.indexOf(" / ", metaData.indexOf("StreamTitle='"))).trim();
        String djName = metaData.substring(metaData.indexOf(" / ", metaData.indexOf("StreamTitle='")) + 3, metaData.indexOf("';", metaData.indexOf("StreamTitle='"))).trim();
        Log.w("metadata", metaData);
        try{is.close();}
        catch(IOException e){}
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
}
