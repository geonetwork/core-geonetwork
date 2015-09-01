package org.fao.geonet.kernel.oaipmh;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.fao.geonet.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.oaipmh.responses.GeonetworkResumptionToken;

public class ResumptionTokenCache extends Thread {

	public final static int CACHE_EXPUNGE_DELAY = 10*1000; // 10 seconds

	private Map<String,GeonetworkResumptionToken> map ; 
	private boolean running = true;
	private SettingManager settingMan;

	/**
	 * @return the timeout
	 */
	public long getTimeout() {
		return settingMan.getValueAsInt("system/oai/tokentimeout");
	}

	/**
	 * @return the cachemaxsize
	 */
	public int getCachemaxsize() {
	    return settingMan.getValueAsInt("system/oai/cachesize");
	}

	/**
	 * Constructor
	 * @param sm
	 */
	public ResumptionTokenCache(SettingManager sm) {
		
		this.settingMan=sm;
        if(Log.isDebugEnabled(Geonet.OAI_HARVESTER))
            Log.debug(Geonet.OAI_HARVESTER,"OAI cache ::init timout:"+getTimeout());
		
		map = Collections.synchronizedMap( new HashMap<String,GeonetworkResumptionToken>()  );

		this.setDaemon(true);
		this.setName("Cached Search Session Expiry Thread");
		this.start();

	}

	public void run() {

		while(running && !isInterrupted()) {
			try {
				Thread.sleep(CACHE_EXPUNGE_DELAY);
				expunge();
			}
			catch ( java.lang.InterruptedException ie ) {
				ie.printStackTrace();
			}
		}
	}

	private synchronized void expunge() {

		Date now = getUTCTime();

		for (Map.Entry entry : map.entrySet() ) {
			if ( ((GeonetworkResumptionToken)entry.getValue()).getExpirDate().toDate().getTime()/1000 < (now.getTime()/1000)  ) {
				map.remove(entry.getKey());
                if(Log.isDebugEnabled(Geonet.OAI_HARVESTER))
                    Log.debug(Geonet.OAI_HARVESTER,"OAI cache ::expunge removing:"+entry.getKey());
			}
		}
	}
	
	// remove oldest token from cache
	private void removeLast() {
        if(Log.isDebugEnabled(Geonet.OAI_HARVESTER)) Log.debug(Geonet.OAI_HARVESTER,"OAI cache ::removeLast" );

		
		long oldest=Long.MAX_VALUE;
		Object oldkey="";
		
		for (Map.Entry entry : map.entrySet() ) {
			
			if ( ((GeonetworkResumptionToken)entry.getValue()).getExpirDate().getSeconds() < oldest   ) {
				oldkey = entry.getKey();
				oldest = ((GeonetworkResumptionToken)entry.getValue()).getExpirDate().getSeconds();
			}
		}
		
		map.remove(oldkey);
        if(Log.isDebugEnabled(Geonet.OAI_HARVESTER))
            Log.debug(Geonet.OAI_HARVESTER,"OAI cache ::removeLast removing:"+oldkey);

		
	}


	public synchronized GeonetworkResumptionToken getResumptionToken(String str) {
		return map.get(str);
	}
	public synchronized void storeResumptionToken(GeonetworkResumptionToken resumptionToken) {
        if(Log.isDebugEnabled(Geonet.OAI_HARVESTER))
            Log.debug(Geonet.OAI_HARVESTER,"OAI cache ::store "+resumptionToken.getKey() + " size: "+map.size() );
		
		if ( map.size() == getCachemaxsize() ) {
			removeLast();
		}
		
		resumptionToken.setExpirDate(new ISODate( getUTCTime().getTime() + getTimeout()*1000, false));
		map.put(resumptionToken.getKey(), resumptionToken);
	}

	private static Date getUTCTime()
	{
		Date date = new Date();
		TimeZone tz = TimeZone.getDefault();
		Date ret = new Date( date.getTime() - tz.getRawOffset() );

		if ( tz.inDaylightTime( ret ))
		{
			Date dstDate = new Date( ret.getTime() - tz.getDSTSavings() );

			// check to make sure we have not crossed back into standard time
			// this happens when we are on the cusp of DST (7pm the day before the change for PDT)
			if ( tz.inDaylightTime( dstDate ))
			{
				ret = dstDate;
			}
		}
		return ret;
	}
	
	public void stopRunning() {
        this.running = false;
    }

}
