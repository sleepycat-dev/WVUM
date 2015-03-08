package sleepycat.com.wvumplayer;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;


public class MainActivity extends ActionBarActivity
{
    //member variables
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

        //initialize stream
        String sUrl = "http://www.wvum.org/listen.pls";
        m_WVUMStream = new MediaPlayer();
        m_WVUMStream.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try
        {m_WVUMStream.setDataSource(sUrl);}
        catch(IOException e)
        {}
        try
        {m_WVUMStream.prepare();}
        catch(IOException e)
        {}

        //initialize GUI elements
        m_PlayButton = (Button)findViewById(R.id.playButton);
        m_StopButton = (Button)findViewById(R.id.stopButton);

        //Listeners
        m_PlayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_WVUMStream.start();
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
