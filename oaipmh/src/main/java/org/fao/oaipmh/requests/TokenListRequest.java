package org.fao.oaipmh.requests;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.utils.GeonetHttpRequestFactory;


public abstract class TokenListRequest extends ListRequest {
	

	protected ISODate from;
	protected ISODate until;
	protected String  mdPrefix;
	protected String  set;

    public TokenListRequest(GeonetHttpRequestFactory transport) {
        super(transport);
    }

    //---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------
	
	public ISODate getFrom()  { return from;  }
	public ISODate getUntil() { return until; }

	public String getMetadataPrefix() { return mdPrefix; }
	public String getSet()            { return set;      }
	
	//---------------------------------------------------------------------------

	public void setFrom(ISODate date)
	{
		from = date;
	}

	//---------------------------------------------------------------------------

	public void setUntil(ISODate date)
	{
		until = date;
	}

	//---------------------------------------------------------------------------

	public void setMetadataPrefix(String mdPrefix)
	{
		this.mdPrefix = mdPrefix;
	}

	//---------------------------------------------------------------------------

	public void setSet(String set)
	{
		this.set = set;
	}
	
}
