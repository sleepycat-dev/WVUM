package sleepycat.com.wvumplayer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Davis on 3/10/2015.
 * Intended to show dev credits and(?) WVUM staff
 */
public class CreditsActivity extends ActionBarActivity
{
    //members
    private static final String ABOUT_URL = "http://www.wvum.org/index.php/info/aboutus/";
    private static final String PROFILE_URL = "http://www.wvum.org/index.php/wvum/profile/";
    Button m_DevButt;
    //Button m_NameButt;
    Button m_ComeHangButt;
    Button m_PhoneCallButt;
    //TextView m_Staff;
    //List<String> m_StaffNames;
    String[] m_Positions = {"General Manager", "Program Director", "Training Director", "Underwriting Director",
            "Campus Affairs Director", "Sports Director", "Engineer", "PSA Director", "Music Director", "Music Director",
            "Public Relations/Promotions Director", "Webmaster", "Office Manager", "Digital Music Supervisor"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        m_DevButt = (Button)findViewById(R.id.devButt);
        //m_NameButt = (Button)findViewById(R.id.nameButt);
        m_ComeHangButt = (Button)findViewById(R.id.comeHangButton);
        m_PhoneCallButt = (Button)findViewById(R.id.phoneNumber);
        //m_StaffNames = new ArrayList<String>();
        //m_Staff = (TextView)findViewById(R.id.currentStaff);
        //new getDataAsyncTask().execute("");

        m_PhoneCallButt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String uri = String.format("tel:786-309-8861");
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                phoneIntent.setData(Uri.parse(uri));
                if(phoneIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(phoneIntent);
            }
        });

        m_DevButt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.davissprague.com"));
                startActivity(browserIntent);
            }
        });

        m_ComeHangButt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String uri = String.format(Locale.ENGLISH, "geo:0,0?q=WVUM-FM+Coral+Gables");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(mapIntent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManage = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManage.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

//    private class getDataAsyncTask extends AsyncTask<String, String, Boolean>
//    {
//        @Override
//        protected Boolean doInBackground(String ... s)
//        {
//            //Initializations
//            URL mWVUM_URL = null;
//            BufferedReader mIn = null;
//
//            try
//            {
//                if(isNetworkAvailable())
//                {
//                    mWVUM_URL = new URL(ABOUT_URL);
//                    mIn = new BufferedReader(new InputStreamReader(mWVUM_URL.openStream()));
//                    String inputLine;
//                    while ((inputLine = mIn.readLine()) != null) {
//                        if (inputLine.contains(PROFILE_URL)) {
//                            inputLine = trimString(inputLine);
//                            Log.d("parsing URL ... ", inputLine);
//                            if (inputLine.length() > 1)
//                                m_StaffNames.add(inputLine);
//                        }
//                    }
//                    mIn.close();
//                }
//                else
//                    Toast.makeText(getApplicationContext(), "Please connect to the Internet.", Toast.LENGTH_LONG).show();
//            }
//            catch (MalformedURLException e){}
//            catch (IOException e){}
//            String sResult = "";
//            for(int i = 0; i < m_StaffNames.size(); i++)
//                sResult = sResult + "\n" + m_Positions[i] + "\n" + m_StaffNames.get(i) + "\n";
//            publishProgress(sResult);
//            return true;
//        }
//
//        protected void onProgressUpdate(String ... progress)
//        {
//            super.onProgressUpdate(progress);
//            m_Staff.setText(progress[0]);
//        }
//
//        private String trimString(String sInput)
//        {
//            int nBegin = 0, nEnd = 0;
//            for(nBegin = 2; nBegin < sInput.length(); nBegin++)
//            {
//                if(sInput.charAt(nBegin) == '>' && sInput.charAt(nBegin-1) == '"' && sInput.charAt(nBegin-2) == '/')
//                    break;
//            }
//            for(nEnd = nBegin; nEnd < sInput.length(); nEnd++)
//            {
//                if(sInput.charAt(nEnd) == '<')
//                    break;
//            }
//            sInput = sInput.substring(nBegin+1, nEnd);
//            return sInput;
//        }
//    }
}
