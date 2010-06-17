package org.fao.geonet.kernel.oaipmh;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.oaipmh.util.ISODate;
import org.fao.oaipmh.responses.ResumptionToken;

public class ResumptionTokenCache extends Thread {

	//public final static int CACHE_TIMEOUT = 200 ; // 200 sec 
	public final static int CACHE_EXPUNGE_DELAY = 10*1000; // 10 seconds

	private Map<String,ResumptionToken> map ; 
	private boolean running = true;
	private long timeout;
	private int cachemaxsize;

	public ResumptionTokenCache(int timeout, int cachesize) {
		Log.debug(Geonet.OAI_HARVESTER,"OAI cache ::init timout:"+timeout);
		this.timeout=timeout*1000;
		this.cachemaxsize=cachesize;
		map = Collections.synchronizedMap( new HashMap<String,ResumptionToken>()  );

		this.setDaemon(true);
		this.setName("Cached Search Session Expiry Thread");
		this.start();

	}

	public void run() {

		while(running) {
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

		for (String key : map.keySet() ) {
			if ( map.get(key).getExpirDate().getSeconds() < (now.getTime()/1000)  ) {
				map.remove(key);
				Log.debug(Geonet.OAI_HARVESTER,"OAI cache ::expunge removing:"+key);
			}
		}
	}
	
	// remove oldest token from cache
	private void removeLast() {
		Log.debug(Geonet.OAI_HARVESTER,"OAI cache ::removeLast" );

		
		long oldest=Long.MAX_VALUE;
		String oldkey="";
		
		for (String key : map.keySet() ) {
			
			if ( map.get(key).getExpirDate().getSeconds() < oldest   ) {
				oldkey = key;
				oldest = map.get(key).getExpirDate().getSeconds();
			}
		}
		
		map.remove(oldkey);
		Log.debug(Geonet.OAI_HARVESTER,"OAI cache ::removeLast removing:"+oldkey);

		
	}


	public synchronized ResumptionToken getResumptionToken(String str) {
		return map.get(str);
	}
	public synchronized void storeResumptionToken(ResumptionToken resumptionToken) {
		Log.debug(Geonet.OAI_HARVESTER,"OAI cache ::store "+resumptionToken.getKey() + " size: "+map.size() );
		
		if ( map.size() == cachemaxsize ) {
			removeLast();
		}
		
		resumptionToken.setExpirDate(new ISODate( getUTCTime().getTime() + timeout  ));
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

}
