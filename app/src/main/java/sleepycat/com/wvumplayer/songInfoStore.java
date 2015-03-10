package sleepycat.com.wvumplayer;

/**
 * Created by Davis on 3/10/2015.
 * wrapper class for song information
 * so text field can be updated independently
 * of play/stop buttons.
 */
public class songInfoStore
{
    //member variables
    String m_sSongInfo;
    private songInfoStoreListener m_Listener;

    //methods
    public songInfoStore(String initVal)
    {
        m_sSongInfo = initVal;
    }

    //Sets the listener on the store. Listener will be
    //modified when the value changes
    public void setListener(songInfoStoreListener listener)
    {
        m_Listener = listener;
    }

    //callback (this is what is called when the value changes)
    public static interface songInfoStoreListener
    {
        void onInfoChanged(String songinfo);
    }

    //getters and setters
    public void setSongInfo(String songinfo)
    {
        m_sSongInfo = songinfo;
        if(m_Listener != null)
            m_Listener.onInfoChanged(m_sSongInfo);
    }

    public String getM_sSongInfo()
    {return m_sSongInfo;}
}
